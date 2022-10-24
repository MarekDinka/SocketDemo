import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;

public class Client {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private final int[] listOfFreePorts = {4002, 4003, 4004, 4005, 4006, 4007, 4008, 4009, 4010, 4011};
    private final String IP = "127.0.0.1", SERVER_RECOGNITION_MESSAGE = "abcd";

    public Client() throws ConnectException {
        boolean serverFound = false;
        for (int port : listOfFreePorts) {
            try {
                clientSocket = new Socket(IP, port);
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                if (!SERVER_RECOGNITION_MESSAGE.equals(in.readLine())) {
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
        String resp;
        do {
            out.println(msg);
            resp = in.readLine();
        } while (!"ack".equals(resp));
        System.out.println("Message delivered!");
        stopConnection();
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
                client.sendMessage(args[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Usage: java -jar SocketClient.jar \"your message\"");
        }
    }
}