import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.TimerTask;import java.util.Timer;


/**
 * Created by Bhuvanesh Rajakarthikeyan ID:1001410051 on 4/6/2017.
 */
public class Process extends JFrame implements Runnable {
    private JFrame mainFrame;
    private JPanel controlPanel;
    private JScrollPane scrollPane;
    private JTextArea taMsg;
    private JButton bCrash;
    private JButton bStartElec;
//    private JButton bRespawn;
    private JLabel jlbl;
    public int coordprocno=0;

    final int PORT = 12340;
    Socket socket = null;
    String serverAddress = "127.0.0.1";
    int procnum=0;
    Thread t;
    Timer timer;
    TimerTask timertask;
    int c1;                                //Counter variable that is used to calculate the delay in receiving the messages.
    public static void main(String[] args)
    {
        Process obj =  new Process();
    }

    Process()
    {

        c1=0;
        try {
            socket = new Socket(serverAddress, PORT);
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            String r=dis.readUTF();
            this.procnum=Integer.parseInt(r.split(":")[0]);          //Get this Process's Number from the Mediator
            prepareGUI();                            //Draw the GUI Frame and components
            if(Integer.parseInt(r.split(":")[1])==1)
            {
                bStartElec.doClick();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        t = new Thread(this, "Process thread");                   //Start this instance as a thread
        t.start();
        taMsg.append("Process "+procnum+" started..");
        timer=new Timer();
        timertask=new TimerTask() {
            @Override
            public void run() {

                if(c1>15)                                          //If this counter variable has reached value 15, then it means the communication
                {                                                   //has stopped due to process crash or new process created.
                    try{                                            //So initiate the election again to find Coord.
                        System.out.println("C1: "+c1);
                        c1=0;
                        System.out.println("C1 After: "+c1);
                        taMsg.append("\nReStarting the Election..\n");
                        DataOutputStream dos=new DataOutputStream(socket.getOutputStream());
                        dos.writeUTF("ELEC : "+procnum);

                    }catch(Exception e){}

                }
                c1++;
            }
        };

        timer.scheduleAtFixedRate(timertask,25000,2000);
    }
    public void run() {
        try {
            while (true) {
                t.sleep(1500);

                System.out.println("C1: " + c1);
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                String read = dis.readUTF();                                         //Read the incoming message


                if (read.contains("ELEC")) {                                        //Perform operation based on the token received. Adds the current process num if token does not contain it, else the new coord is found.
                    taMsg.append("\nIncoming Token: " + read.toString());
                    if (read.contains("" + procnum)) {                              //If the current process num is found inside the ELEC token, then the token has passed through complete ring
                                                                                    //hence find the greatest proc num in the token
                        System.out.println("Elec Msg:" + read);                     //and then Convey the Coord num to other processes
                        String[] linesplit = read.split(":");
                        String list = linesplit[1];
                        int greatest = 0;
                        System.out.println("Greatest: " + list);
                        for (int i = 0; i < list.length(); i++) {
                            if (Character.getNumericValue(list.charAt(i)) > greatest)
                                greatest = Character.getNumericValue(list.charAt(i));
                        }
                        taMsg.append("\nNew Coordinator is : " + greatest);
                        coordprocno = greatest;
                        jlbl.setText("Coordinator: " + coordprocno);
                        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                        dos.writeUTF("COORDE New Coordinator is :" + greatest);                 //The new coord is announced
                    } else {                                                            //If the proc num is not inside the token,
                        System.out.println("Elec Msg:" + read);                         //the token is passed after appending the current proc num
                        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                        dos.writeUTF(read + " " + procnum);
                    }
                } else if (read.contains("COORDE") || read.contains("CONVEY")) {        //If the token contains COORDE or CONVEY
                    System.out.println("COCON INSIDE: " + read);                        //it means the new coordinator is announced
                    if (read.contains("CONVEY")) {
                        coordprocno = Integer.parseInt(read.split(":")[2]);         //Coordinator Process Number is extracted from the token and stored
                        jlbl.setText("Coordinator: " + coordprocno);                      //and set the value in the GUI
                        taMsg.append("\nIncoming Token: CONVEY: New Coordinator is: " + read.split(":")[2]);
//                        System.out.println("\n\n\n\nI am the coord:" + procnum);
                    }
                    if (read.contains("COORDE")) {
                        coordprocno = Integer.parseInt(read.split(":")[1]);         //Retrieve the coord number from COORDE message
                        jlbl.setText("Coordinator: " + coordprocno);                        //and set the value in the GUI
                        taMsg.append("\nIncoming Token: " + read);
                    }
                    if (read.contains("" + procnum) && (procnum != coordprocno)) {          //If the current process num is found in the token
                        //System.out.println(read);                                           //it means all the process knows the new coordinator
//                        String[] linesplit = read.split(":");
//                        taMsg.append("\nConveyed to All: " + linesplit[1]);
                        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                        dos.writeUTF("ALIVETOKEN  : " + procnum);                               //hence start sending the Check whether Coord is Alive token in the ring.

                    } else {                                                            //If the current process num is not found in the token
                        //System.out.println("COORDE Inside " + read);                       //continue passing the token after appending the current process num.
//                        taMsg.append("\nIncoming Token: " + read);
                        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                        dos.writeUTF("CONVEY" + ":" + read.split(":")[1] + " " + procnum + " " + ":" + coordprocno);
                    }

                } else if (read.contains("ALIVETOKEN")) {                               //If the incoming token contains ALIVETOKEN, the communication is to check the integrity of the ring structure.
                   if(coordprocno!=0) {
                       taMsg.append("\nIncoming Token: " + read.split(":")[0]);
                       DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                       dos.writeUTF(read.split(":")[0] + ":" + read.split(":")[1] + " " + procnum);   //Pass the token to next process
                       c1 = 0;                                                             //Counter is set to zero, hence the token is received successfully
                   }                                                                   //If no token is received, the counter will not be set to zero
                                                                                                //causing the same counter variable in the timer to achieve the threshold, leading to new election.
                } else {
                    System.out.println("Nothing received!!Hopefully the flow wont reach here..I'm " + procnum);
                }

            }
        } catch (IllegalStateException e) {
            System.out.println("Timer already there");
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }
//This method initializes the Jframe and then adds the component to it. Also a listener is added to the Crash and Start Election button.
    private void prepareGUI() {
        mainFrame = new JFrame("Process "+procnum);
        mainFrame.setBounds(0,0,470, 400);  // Overall Size

        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
            //When the GUI window is closed, the socket is closed and timer is cancelled similating crash scenario.
                mainFrame.dispose();
                try{
                    socket.close();timer.cancel();
                }
                catch(Exception e){e.printStackTrace();}
                System.exit(0);
            }
        });
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        mainFrame.setLocation(dim.width/2-mainFrame.getSize().width/2, dim.height/2-mainFrame.getSize().height/2-100);
        controlPanel = new JPanel();
        controlPanel.setLayout(null);

        taMsg = new JTextArea();
        taMsg.setEditable(false);
        scrollPane = new JScrollPane();
        scrollPane.setViewportView(taMsg);
        scrollPane.setBounds(30, 65, 400, 180);   //Control Text Area Size through this
        mainFrame.add(controlPanel);
        controlPanel.add(scrollPane);
        bStartElec = new JButton("Start Election");
        bStartElec.setBounds(30, 280, 110, 25);
        controlPanel.add(bStartElec);
        bCrash = new JButton("Crash");
        bCrash.setBounds(150, 280, 95, 25);
        controlPanel.add(bCrash);
//        bRespawn = new JButton("Respawn");
//        bRespawn.setBounds(255,280,100,25);
//        controlPanel.add(bRespawn);
        jlbl=new JLabel("Coordinator: ");
        jlbl.setBounds(30, 20, 149, 25);
        controlPanel.add(jlbl);
        mainFrame.setVisible(true);
        //When Crash button is clicked, close the socket, resulting an exception in ProcessThread in Mediator.java
        //where the exception is caught and the Communication is adjusted accordingly
        bCrash.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    System.out.println("Process Crashing: "+procnum);
                        taMsg.append("Process Crashing..");
                        socket.close();
                        timer.cancel();
                        System.exit(0);
                }catch(Exception ex){}
            }
        });
        //The election is initiated using this button
        //it sends ELEC token out, by appending the process number to the token.
        bStartElec.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {

                    taMsg.append("\nStarting the Election...");
                    DataOutputStream dos=new DataOutputStream(socket.getOutputStream());
                    dos.writeUTF("ELEC : "+procnum);

                }catch(Exception ex){}
            }
        });
    }

}


