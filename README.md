# RingElection
Ring Election Algorithm
Running the Program:

1.	Import the project folder(bhuvanesh0051RE) into the IDE(Eclipse or IntelliJ Idea).
2.	Run Mediator.java as java application.
3.	The Mediator performs serves as a relay for the Processes in the Ring. 
4.	Run Process.java as java application for each process.
5.	When the Process.java is run multiple times, a GUI window opens for each process.
6.	GUI will have options to Start the Election and Crash the process. 
7.	When the Start Election is clicked, the process initiates the election.
8.	When the election is completed, the coordinator is determined and conveyed to all the processes. GUI shows the present coordinator in a label. After conveying, the process passes the token in a ring manner.
9.	When the Coordinator is crashed, the timer in each process detects the delay in incoming messages and initiates the election. As there are timers in all of the processes, all the processes initiate the election.
10.	When normal process is crashed, the token is passed normally by skipping it.
11.	When a new process is created, the communication is stopped and the election is started again.

System Requirement:
•	Windows (any version)
•	Java (minimum 1.7)
•	IntelliJ Idea IDE or Eclipse IDE


Additional Functionalities:

1.	The program is included with a GUI for better readability.

Limitations:

1.	Mediator should be running at all times. 

Assumptions:

1.	It is assumed that the processes are formed in a ring manner.
2.	It is assumed that the process with greater process number always wins the election.
