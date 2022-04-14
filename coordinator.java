import java.io.*;
import java.util.*;
import java.net.*;
import java.io.FileNotFoundException;
class Coordinator extends Thread {

    private ServerSocket portServer;
    private int portNumber;
    private long thresholdTime;

    public Coordinator(int portNumber, long thresholdTime) {
        this.portNumber = portNumber;
        this.thresholdTime = thresholdTime;
    }

    public void run() {
        try {
            this.portServer = new ServerSocket(portNumber);

            while (true) {
                Socket portParticipantSocket = null;
                try {
                    System.out.println("Server waiting...." + this.portServer);
                    portParticipantSocket = this.portServer.accept();
                } catch (Exception e) {
                    System.out.println("Error Connecting to the server");
                }
                System.out.println("Connected participants" + portParticipantSocket);
                Instance participant = new Instance(portParticipantSocket, thresholdTime);
                participant.start();
            }
        } catch (Exception e) {
        }
    }

    public static void main(String args[]) throws Exception {
        try {
            File file = new File(args[0]);
            Scanner sc = new Scanner(file);
            int portNumber = Integer.parseInt(sc.nextLine());
            long thresholdTime = Long.parseLong(sc.nextLine());
            Coordinator particiManager = new Coordinator(portNumber, thresholdTime);
            particiManager.start();
        } catch (Exception e) {
            System.out.println("Error" + ": " + e);
        }
    }
}
