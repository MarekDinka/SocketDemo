import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Arrays;

public class Client {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private final int[] listOfFreePorts = {4002, 4003, 4004, 4005, 4006, 4007, 4008, 4009, 4010, 4011};
    private final String IP = "127.0.0.1", SERVER_PASSWORD = "abcd";

    public Client() throws ConnectException { // could serve as a base class for all communication with server
        boolean serverFound = false;
        for (int port : listOfFreePorts) {
            try {
                clientSocket = new Socket(IP, port);
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                if (!SERVER_PASSWORD.equals(in.readLine())) { // not my server
                    clientSocket.close();
                    out.close();
                    in.close();
                    continue;
                }
                out.println("found you!");
                serverFound = true;
                break;
            } catch (IOException e) {
                 System.out.printf("Port %d occupied, trying next one\n", port);
            }
        }
        if (!serverFound) {
            throw new ConnectException("No server found!");
        }
    }

    public void sendMessage(String msg) throws IOException {
        sendMessage(msg, false);
    }
    public void sendMessage(String msg, boolean stayConnected) throws IOException {
        String resp;
        do {
            out.println(msg);
            resp = in.readLine();
        } while (!"ack".equals(resp));
        System.out.println("Message delivered!");
        if (!stayConnected) {
            stopConnection();
        }
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                Client client = new Client();
//                while (true) {
//                    client.sendMessage("aaaaaaaa", true);
//                    Thread.sleep(1000);
//                }
                client.sendMessage(args[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else { //test udp
//            System.out.println("Usage: java -jar SocketClient.jar \"your message\"");
            try {
                DatagramSocket socket = new DatagramSocket(4002);
                byte[] buff = new byte[256];

                DatagramPacket packet = new DatagramPacket(buff, buff.length);
                socket.receive(packet);
                socket.close();

                InetAddress address = packet.getAddress();
                String message = Arrays.toString(buff);
                if ("Hear me!".equals(message)) {
                    Client client = new Client();
                    client.sendMessage("Arduino here!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}