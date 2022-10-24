import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public Client(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
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
            Client client = new Client("127.0.0.1", 6666);
            try {
                client.sendMessage(args[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Usage: java -jar SocketClient.jar \"your message\"");
        }
    }
}