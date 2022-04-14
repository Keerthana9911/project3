import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

class Instance extends Thread {
    private Socket portSocket;
    private long thresholdTime;
    private int participatorId;
    private DataOutputStream dos;
    private DataInputStream dis;
    private Socket msgSocket;
    private DataOutputStream dos_msg;
    private DataInputStream dis_msg;
    private boolean isMessageThread = false;

    public long getThresholdTime() {
        return this.thresholdTime;
    }

    private static Map<Integer, Boolean> multicastMap = new HashMap<Integer, Boolean>();

    public static Map<Integer, Boolean> getmulticastMap() {
        return multicastMap;
    }

    public static void addParticipant(int pId, boolean active) {
        multicastMap.put(pId, active);
    }

    public static void removeParticipant(int pId) {
        multicastMap.remove(pId);
    }

    public static void modifyParticipant(int pId, boolean flag) {
        multicastMap.put(pId, flag);
    }

    public static Boolean getParticipant(int pId) {
        return multicastMap.get(pId);
    }

    private static Map<Integer, String> multicastMessageMap = new HashMap<Integer, String>();

    public static Map<Integer, String> getMulticastMessageMap() {
        return multicastMessageMap;
    }

    public static void setParticipantMessage(int pId, String message) {
        multicastMessageMap.put(pId, message);
    }

    public static void removeParticipantMessage(int pId) {
        multicastMessageMap.remove(pId);
    }

    public static String getParticipantMessage(int pId) {
        return multicastMessageMap.get(pId);
    }

    private static Map<Integer, Map<Calendar, String>> persistantPartiMap = new HashMap<Integer, Map<Calendar, String>>();

    public static Map<Integer, Map<Calendar, String>> getPersistantPartiMap() {
        return persistantPartiMap;
    }

    public static void setPersistantPartiMap(Map<Integer, Map<Calendar, String>> m) {
        persistantPartiMap = m;
    }

    public Instance(Socket portSocket, long thresholdTime) {
        try {

            System.out.println("In Instance constructor");
            this.portSocket = portSocket;
            this.thresholdTime = thresholdTime;

        } catch (Exception e) {

        }
    }

    public Instance(DataOutputStream dos_msg, int participatorId, long thresholdTime) {
        try {
            this.isMessageThread = true;
            this.dos_msg = dos_msg;
            this.participatorId = participatorId;
            this.thresholdTime = thresholdTime;
        } catch (Exception e) {

        }
    }

    public Instance() {

    }

    public void register(int participatorId, String IpAddress, int messagePortNumber) {
        try {
            this.participatorId = participatorId;
            this.msgSocket = new Socket(IpAddress, messagePortNumber);
            this.dis_msg = new DataInputStream(this.msgSocket.getInputStream());
            this.dos_msg = new DataOutputStream(this.msgSocket.getOutputStream());
            new Instance().addParticipant(participatorId, true);
        } catch (Exception e) {

        }
    }

    public void deregister(int participatorId) {
        try {
            new Instance().removeParticipant(participatorId);
            Map<Integer, Map<Calendar, String>> tempPersistantPartiMap = new Instance()
                    .getPersistantPartiMap();
            tempPersistantPartiMap.remove(participatorId);
        } catch (Exception e) {

        }
    }

    public void disconnect(int participatorId) {
        new Instance().modifyParticipant(participatorId, false);

    }

    public void multicastSend(String message) {
        try {
            Map<Integer, Boolean> tempmulticastMap = new Instance().getmulticastMap();
            for (Integer participatorId : tempmulticastMap.keySet()) {

                if (tempmulticastMap.get(participatorId)) {
                    new Instance().setParticipantMessage(participatorId, message);
                } else {

                    Calendar nowCal = Calendar.getInstance();
                    nowCal.setTime(new Date());

                    Map<Integer, Map<Calendar, String>> tempPersistantPartiMap = new Instance()
                            .getPersistantPartiMap();
                    Map<Calendar, String> tempMessageMap;
                    if (!(null == tempPersistantPartiMap.get(participatorId))) {
                        tempMessageMap = tempPersistantPartiMap.get(participatorId);
                    } else {
                        tempMessageMap = new HashMap<Calendar, String>();
                    }
                    tempMessageMap.put(nowCal, message);
                    tempPersistantPartiMap.put(participatorId, tempMessageMap);
                    new Instance().setPersistantPartiMap(tempPersistantPartiMap);

                }
            }
        } catch (Exception e) {

        }
    }

    public void reconnect(int participatorId, String IpAddress, int messagePortNumber) {
        try {
            this.msgSocket = new Socket(IpAddress, messagePortNumber);
            this.dis_msg = new DataInputStream(this.msgSocket.getInputStream());
            this.dos_msg = new DataOutputStream(this.msgSocket.getOutputStream());
            new Instance().modifyParticipant(participatorId, true);
        } catch (Exception e) {

        }
    }

    public void run() {
        try {
            boolean temp = true;

            if (this.isMessageThread) {

                System.out.println("In the message thread");
                while (temp) {
                    if ((null == new Instance().getParticipant(this.participatorId))) {
                        temp = false;
                        ;
                    } else if (!new Instance().getParticipant(this.participatorId)) {
                        temp = false;
                    } else {
                        if (!(null == new Instance().getParticipantMessage(this.participatorId))) {
                            System.out.println(new Instance().getParticipantMessage(this.participatorId));
                            this.dos_msg
                                    .writeUTF(new Instance().getParticipantMessage(this.participatorId));
                            new Instance().removeParticipantMessage(this.participatorId);
                        }
                        Map<Integer, Map<Calendar, String>> tempPersistantPartiMap = new Instance()
                                .getPersistantPartiMap();
                        if (!(null == tempPersistantPartiMap.get(this.participatorId))) {
                            System.out.println("Persistance successful");
                            Map<Calendar, String> tempMessageMap = tempPersistantPartiMap.get(participatorId);
                            for (Calendar cal : tempMessageMap.keySet()) {
                                Calendar nowCal = Calendar.getInstance();
                                nowCal.setTime(new Date());
                                long secondsBetween = ChronoUnit.SECONDS.between(cal.toInstant(), nowCal.toInstant());
                                System.out.println(
                                        "Time diff is " + secondsBetween + " and td is " + this.getThresholdTime());
                                if (secondsBetween <= this.getThresholdTime()) {
                                    System.out.println(tempMessageMap.get(cal));
                                    this.dos_msg.writeUTF(tempMessageMap.get(cal));
                                }
                            }

                            tempPersistantPartiMap.remove(this.participatorId);
                        }
                        new Instance().setPersistantPartiMap(tempPersistantPartiMap);
                    }
                }
            } else {
                String message = "Chat started!";
                System.out.println("Connected port " + this.portSocket);
                dos = new DataOutputStream(this.portSocket.getOutputStream());
                dis = new DataInputStream(this.portSocket.getInputStream());
                String command = "";

                while (message != "exit") {
                    System.out.println("Waiting for Waiting");
                    command = this.dis.readUTF();
                    System.out.println("Command called: " + command);
                    if (command != null && command.split(" ")[0].equalsIgnoreCase("register")) {
                        register(Integer.parseInt(command.split(" ")[1]), command.split(" ")[2],
                                Integer.parseInt(command.split(" ")[3]));
                        dos.writeUTF("connection made");
                        Instance messageThread = new Instance(this.dos_msg,
                                this.participatorId, this.thresholdTime);
                        messageThread.start();
                    } else if (command != null && command.contains("msend")) {
                        multicastSend(command.substring(6));
                    } else if (command != null && command.contains("disconnect")) {
                        disconnect(this.participatorId);
                        dos.writeUTF("ok");
                    } else if (command != null && command.contains("reconnect")) {
                        reconnect(Integer.parseInt(command.split(" ")[1]), command.split(" ")[2],
                                Integer.parseInt(command.split(" ")[3]));
                        dos.writeUTF("Reconnected");
                        Instance messageThread = new Instance(this.dos_msg,
                                this.participatorId, this.thresholdTime);
                        messageThread.start();
                    } else if (command != null && command.contains("deregister")) {
                        deregister(this.participatorId);
                        dos.writeUTF("Deregistered");
                    } else {
                        dos.writeUTF("Invalid Command");
                    }

                }
                System.out.println("Coordinator has ended session");

            }
        } catch (Exception e) {
        }
    }
}