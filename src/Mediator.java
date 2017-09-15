import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by Bhuvanesh Rajakarthikeyan ID:1001410051 on 4/8/2017.
 */
//The Mediator performs serves as a relay for the Processes in the Ring.
public class Mediator {
    static int PORT = 12340;            //PORT Number
    static ServerSocket serverSocket = null;
    static ProcessThread processes[]=new ProcessThread[10];      //Thread Objs for Each Process
    static int processnum=0;
    public static int[] avlprocs={99,99,99,99,99,99,99,99,99,99};    //Array that maintains the list of Processes that are active or Crashed or unused.
    public static void main(String[] args)
    {


        try {
            serverSocket = new ServerSocket(PORT);       //Initialize the Server Socket
        } catch (IOException e) {
            e.printStackTrace();

        }
        while (true) {
            try {
                //socket = serverSocket.accept();
                for(int i=0;i<10;i++)
                {
                    if(processes[i]==null)
                    {
                        avlprocs[processnum]=processnum;
                        (processes[i]=new ProcessThread(serverSocket,processes,processnum,avlprocs)).start();           //Create an array of threads and initialize them as each Process are created
                        processnum++;                                 //Keep track of Process Number when they are created.
                        break;
                    }
                }
            } catch (Exception e) {
                System.out.println("I/O error: " + e);
            }

        }
    }
}

//Process Thread is created for each Process that is created
//This class performs only the relay operation so that a Ring formation can be achieved
//It also knows to differentiate Coord Process from Normal Process
class ProcessThread extends Thread
{
    protected Socket socket;
    Process obj;
    ProcessThread processes[];
    int totalprocs;
    static int avlprocs[];
    int procnum;
    static int coordprocnum=97;
    static int var=0;
    public ProcessThread(ServerSocket ss, ProcessThread[] processes,int procnum,int[] avlprocs) {
        try{
            this.socket = ss.accept();         //Accept the socket request for the Process
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF(""+procnum+":"+var);  //Send the Process Number to the Process for Identification
        }catch (Exception e){e.printStackTrace();}
        this.processes=processes;
        this.obj=obj;
        this.procnum=procnum;
        this.avlprocs=avlprocs;


    }
    public void run()
    {
        try
        {
            while (true)
            {


                DataInputStream dis=new DataInputStream(socket.getInputStream());//dis.available();
                String read=dis.readUTF();      //Read the message from the Process to send it to Next Process
                System.out.println("Read value: "+read);
                sleep(1500);              //Thread Sleep so that the tokens passed can be observed.
                totalprocs=0;
                int flag=0;
                for(int i=0;i<avlprocs.length;i++)               //Calculate the Total number of Processes.
                {
                    if(avlprocs[i]!=99&&avlprocs[i]!=88&&avlprocs[i]!=98)       //If in the array, the value is not Crashed(88 or 98) or Unused(99), increment the Total value
                    {totalprocs+=1;}
                    if(avlprocs[i]==88){                        //If value 88 is found, a Process is crashed, set the flag
                        flag=1;                                 //The flag is set to signal the Communication freeze.
                    }
                   // System.out.print(" "+avlprocs[i]);
                }

                totalprocs-=1;                               //Adjust the total number as avlprocs array contains extra value (Loop runs one extra time in Mediator).
                //System.out.print("\n Total Procs: "+totalprocs+"Procnum: "+procnum);
//                if(coordprocnum<procnum)                  //If Coord Proc Number is less than current Process Number, it means a new Process has been created as per assumption.
//                {
//                    System.out.println("Some new process has entered the arena.."+coordprocnum+" "+procnum);
//                    coordprocnum=97;
//                }
                 if(read.contains("ELEC")) {            //If the incoming message contains ELEC, just pass it on to next process
                    var=1;
                    int next = procnum + 1;
                    if(avlprocs[next]==77)          //If a Process that is not a Coord is crashed, just skip it and pass it to successive process
                    {
                        next=next+1;
                    }
                    if(avlprocs[next+1]==98||avlprocs[next+1]==88)         //When coord is crashed and a process is started, move the next pointer one place after 98
                    {
                        next=next+1;
                    }
                    if(avlprocs[procnum]==98)                              //Adjustment for the irregularity in the array structure
                    {
                        next=0;
                    }
                    if(avlprocs[next]==99)
                    {
                        next=0;
                    }
                    if (procnum == totalprocs-1) {         //This is done to form a Ring formation. If this is the last Process in the Ring, then the message should be passed to Next Process that is ZERO
                        next = 0;
                    }

                    //System.out.println("Next "+next+"T "+totalprocs);
                    DataOutputStream dos = new DataOutputStream(processes[next].socket.getOutputStream());
                    dos.writeUTF(read);
                    flag=0;
                    for(int i=0;i<avlprocs.length;i++)
                    {

                        if(avlprocs[i]==88 && procnum==totalprocs-1){               //When Coord crash, the freeze should occur only once. So reset the crashed process to 77 that is skip
                            avlprocs[i]=98;System.out.println("here i am");
                        }
                    }
                }
                else if(read.contains("COORDE")||read.contains("CONVEY")){         //Same process as before, but take note of who Coordinator is, in this section
                    int next = procnum + 1;
                    if(avlprocs[next]==77)
                    {
                        next=next+1;
                    }
                    if(avlprocs[next+1]==98||avlprocs[next+1]==88)         //When coord is crashed and a process is started, move the next pointer one place after 98
                    {
                        next=next+1;
                    }
                    if(avlprocs[procnum]==98)                              //Adjustment for the irregularity in the array structure
                    {
                        next=0;
                    }
                    if(avlprocs[next]==99)
                    {
                        next=0;
                    }
                    if (procnum == totalprocs-1) {
                        next = 0;
                    }

                    String[] linesplit=read.split(":");
                    if(read.contains("CONVEY"))
                    {
                        coordprocnum=Integer.parseInt(linesplit[2]);             //Retrieve the coord number from the CONVEY message
                    }
                    if(read.contains("COORDE"))
                    {
                        coordprocnum=Integer.parseInt(linesplit[1]);            //Retrieve the coord number from COORDE message
                    }
                    DataOutputStream dos = new DataOutputStream(processes[next].socket.getOutputStream());
                    dos.writeUTF(read);
                }
                else if(read.contains("ALIVETOKEN")){                   //Same process as before, but if the Flag is set to 1, the Communication is frozen here
                    //if(coordprocnum<procnum)                           //If Coord Proc Number is less than current Process Number, it means a new Process has been created as per assumption.
                    //{
                        //System.out.println("Some new process has entered the are.."+coordprocnum+" "+procnum);
                    //}
                    int next = procnum + 1;
                    if(avlprocs[procnum+1]==77)
                    {
                        next=next+1;

                    }
                    if(avlprocs[next+1]==98||avlprocs[next+1]==88)         //When coord is crashed and a process is started, move the next pointer one place after 98
                    {
                        next=next+1;
                    }
                    if(avlprocs[procnum]==98)                              //Adjustment for the irregularity in the array structure
                    {
                        next=0;
                    }
                    if(avlprocs[next]==99)
                    {
                        next=0;
                    }
                    if (procnum == totalprocs-1) {
                        next = 0;
                    }
                    //System.out.println("Next: "+next);
                    if(flag==0)                 //If flag is ZERO, pass on to next Process
                    {
//                        if(coordprocnum==97)                           //If Coord Proc Number is less than current Process Number, it means a new Process has been created as per assumption.
//                        {
//                            System.out.println("Some new process has entered the arena.."+coordprocnum+" "+procnum);
//                            coordprocnum=97;
//                        }
//                        else{
                        DataOutputStream dos = new DataOutputStream(processes[next].socket.getOutputStream());
                        dos.writeUTF(read);
                    }//}
                    else                        //If Flag is set to 1, then freeze the Communication, so that election may start again.
                    {

                    }

                }
                else
                {
                    System.out.println("The flow never reaches here.. " + procnum);
                }

            }
        }

        catch(SocketException se)
        {
           // se.printStackTrace();
            //System.out.println("Processnum while crashing.. "+procnum+"Next value: ");
            System.out.println("I'm crashing.." + procnum+"I . ."+coordprocnum);
            if(procnum==coordprocnum) {                              //If the process crashes, and if its a coordinator,
                System.out.println("I'm crashing.." + procnum);
                avlprocs[procnum + 1] = 88;                          //change its value in the array to 88, so that the communication is stopped.
            }
            else                                                     //else if its not a coordinator
            {
                avlprocs[procnum]=77;                                //change the value to 77, so that its skipped from ring.
            }
        }
        catch (IOException io)
        {
            //io.printStackTrace();
            System.out.println("I'm crashing.." + procnum+"I'm .."+coordprocnum);
            if(procnum==coordprocnum) {                              //If the process crashes, and if its a coordinator,
                System.out.println("I'm crashing.." + procnum);
                avlprocs[procnum + 1] = 88;                          //change its value in the array to 88, so that the communication is stopped.
            }
            else                                                     //else if its not a coordinator
            {
                avlprocs[procnum]=77;                                //change the value to 77, so that its skipped from ring.
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }
}
