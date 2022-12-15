package sk.uniba.fmph.Burnie;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Client extends Thread {
    private Socket clientSocket;
    private BufferedOutputStream out;
    private BufferedInputStream in;
    private final static int PORT = 4002;
    private final static byte[] SERVER_PASSWORD = new byte[]{'a', 'b', 'c', 'd'};
//    private final static byte END_OF_MESSAGE = 4;
    private final String SERVER_IP;

    /**
     * Connect to server using differentIp
     * @param differentIp server ip
     * @throws ConnectException if connecting goes wrong
     */
    public Client(String differentIp) throws ConnectException { //TODO -> check if differentIp is an IP
        SERVER_IP = differentIp;
        connectToServer(SERVER_IP);
    }

    /**
     * Use UDP to find server ip
     * @throws ConnectException if connecting goes wrong
     */
    public Client() throws ConnectException {
        String IP = "";
        try {
            connectToServer("127.0.0.1"); // we need to try localhost first because 2 different applications(server and this) cannot be listening for UPD on one ip at the same time
            IP = "127.0.0.1";
        } catch (ConnectException e) {
            IP = UDPCommunicationHandler.findServerIp();
            connectToServer(IP);
        } finally {
            SERVER_IP = IP;
        }
    }

    private void writeMessage(Message msg) throws IOException {
        out.write(msg.getMessage());
        out.flush();
    }

    /**
     * read message from server
     * @return message from server in form of byte[]
     * @throws IOException TODO
     */
    private byte[] readLine() throws IOException {
        byte[] msgLength = new byte[4];
        for (int i = 0; i < 4; i++) {
            msgLength[i] = (byte) in.read();
        }
        int len = ByteBuffer.wrap(msgLength).getInt();
        byte[] res = new byte[len];
        int count = in.read(res);
        if (count != len) {
            throw new SocketException("Bad packet received, expected length " + len + "got " + count);
        }
        return res;
    }

    /**
     * read message from server and convert it to string
     * @return message from server in string form
     * @throws IOException TODO
     */
    private String readStringLine() throws IOException {
        byte[] msgLength = new byte[4];
        for (int i = 0; i < 4; i++) {
            msgLength[i] = (byte) in.read();
        }
        int len = ByteBuffer.wrap(msgLength).getInt();
        byte[] res = new byte[len];
        int count = in.read(res);
        if (count != len) {
            throw new SocketException("Bad packet received, expected length " + len + "got " + count);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(res);
        return out.toString();
    }

    private void connectToServer(String ip) throws ConnectException {
        byte[] password = new byte[0];
        try {
            clientSocket = new Socket(ip, PORT);
            clientSocket.setSoTimeout(10000);
            out = new BufferedOutputStream(clientSocket.getOutputStream());
            in = new BufferedInputStream(clientSocket.getInputStream());
            password = readLine();
        } catch (ConnectException e) {
            throw new ConnectException("No server found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!Arrays.equals(SERVER_PASSWORD, password)) {
            try {
                clientSocket.close();
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            throw new ConnectException("No server found!");
        }

        try {
            sendMessage(MessageBuilder.GUI.build());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send message to server
     * @param msg message to be sent
     * @param stayConnected if true, socket will remain connected to server
     * @throws IOException TODO
     */
    public void sendMessage(byte[] msg, boolean stayConnected) throws IOException {
        writeMessage(new Message(msg));
        System.out.println("Message delivered!");
        if (!stayConnected) {
            stopConnection();
        }
    }
    public void sendMessage(byte[] msg) throws IOException {sendMessage(msg, true);}
    public void sendMessage(String msg) throws IOException {sendMessage(msg.getBytes(StandardCharsets.UTF_8), true);}
    public void sendMessage(String msg, boolean stayConnected) throws IOException {sendMessage(msg.getBytes(StandardCharsets.UTF_8), stayConnected);}

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    /**
     * Await exceptions and resolve them
     */
    @Override
    public void run() {
        try {
            clientSocket.setSoTimeout(0);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        while (clientSocket.isConnected()) {
            try {
                String className = readStringLine();
                Class<? extends Exception> c = (Class<? extends Exception>) Class.forName(className);
                Exception e = c.getConstructor().newInstance();
                byte[] stackTrace = readLine();
//                System.out.println(Arrays.toString(stackTrace));
                try (ByteArrayInputStream bin = new ByteArrayInputStream(stackTrace); ObjectInput in = new ObjectInputStream(bin)) {
                    e.setStackTrace((StackTraceElement[]) in.readObject());
                    throw e;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
