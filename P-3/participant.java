import java.net.*;
import java.io.*;
import java.util.*;

class Participant extends Thread {
   private static Scanner sc = new Scanner(System.in);
    private static DataInputStream dis;
    private static DataOutputStream dos;
    private static Socket socket;
    private ServerSocket serverSockett;
    private Socket messageSocket;
    private DataInputStream dis_msg;
    private DataOutputStream dos_msg;
    private String cmd;
    private int pId;
    private String log;
    private String coordinatorIp;
    private int coordinatorPort;
    private static boolean disconnect = true;

    public Participant(int pId, String log, String coordinatorIp, int coordinatorPort) {
        try {
            this.pId = pId;
            this.log = log;
            this.coordinatorIp = coordinatorIp;
            this.coordinatorPort = coordinatorPort;
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
         
        }
    }
    
    public Participant(int myPort, String log) {
        try {
            this.serverSockett = new ServerSocket(myPort);
            this.log = log;
        } catch (Exception e) {
            
        }
    }
    public Participant() {

    }
    public static String getMyIpAddress() {
        try {
            InetAddress ip;
            ip = InetAddress.getLocalHost();
            String temp = String.valueOf(ip);
            return temp.split("/")[1];
        } catch (Exception e) {
          
        }
        return null;
    }

    public void run() {
        try {
            boolean isOnline = true;
            
            this.messageSocket = this.serverSockett.accept();
         
            this.dis_msg = new DataInputStream(messageSocket.getInputStream());
            this.dos_msg = new DataOutputStream(messageSocket.getOutputStream());
            if (!new File(this.log).exists()) {
                Writer temp = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.log), "utf-8"));
                temp.close();
            }
            while (isOnline) {
                if (disconnect) {
                    isOnline = false;
                } else {
                    List < String > messages = new ArrayList < String > ();

                    File file = new File(this.log);
                    Scanner scanner = new Scanner(file);
                    String message = this.dis_msg.readUTF();
                  

                    while (scanner.hasNext()) {
                        messages.add(scanner.nextLine());
                    }
                    Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.log), "utf-8"));
                    for (String s: messages) {
                        writer.write(s + "\n");
                    }
                    writer.write(message);
                    writer.close();
                }
            }
        } catch (Exception e) {
           
        }
    }
    public static void main(String args[]) {
        try {
            File file = new File(args[0]);
            Scanner scanner = new Scanner(file);
            int pId = Integer.parseInt(scanner.nextLine());
            String log = scanner.nextLine();
            String thirdLine = scanner.nextLine();
            String coordinatorIp = thirdLine.split(" ")[0];
            int coordinatorPort = Integer.parseInt(thirdLine.split(" ")[1]);

            socket = new Socket(coordinatorIp, coordinatorPort);
            Participant participant = new Participant(pId, log, coordinatorIp, coordinatorPort);
            String command = "Chat started!";

            while (true) {
                System.out.print("Participant> ");
                command = sc.nextLine();

                if (command.split(" ")[0].equalsIgnoreCase("register")) {
                    
                    disconnect = false;
                   
                    String myIp = new Participant().getMyIpAddress();
                    int myPort = Integer.parseInt(command.split(" ")[1]);
                    Participant messageThread = new Participant(myPort, log);
                    messageThread.start();
                    dos.writeUTF("register " + pId + " " + myIp + " " + String.valueOf(myPort));
                    String ret = dis.readUTF();
                    do {

                    } while (!ret.equalsIgnoreCase("connection made"));
                } else if (command.contains("msend") && disconnect == false) {
                    dos.writeUTF(command);
                } else if (command.contains("disconnect") && disconnect == false) {
                    dos.writeUTF(command);
                    String res = dis.readUTF();
                    if (res.equalsIgnoreCase("ok")) {
                        disconnect = true;
                    }

                } else if (command.equalsIgnoreCase("deregister") && disconnect == false) {
                    dos.writeUTF(command);
                    String res = dis.readUTF();
                    if (res.equalsIgnoreCase("ok")) {
                        disconnect = true;
                    }
                } else if (command.contains("reconnect")) {
                    disconnect = false;
                    String myIp = new Participant().getMyIpAddress();
                    int myPort = Integer.parseInt(command.split(" ")[1]);
                    Participant messageThread = new Participant(myPort, log);
                    messageThread.start();
                    dos.writeUTF("reconnect " + pId + " " + myIp + " " + String.valueOf(myPort));
                    String ret = dis.readUTF();
                    do {

                    } while (!ret.equalsIgnoreCase("connected again"));
                } else {

                }
            }
        } catch (Exception e) {
            
            System.out.println("Error" + ": " + e);
        }
    }
}