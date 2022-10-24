import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket;

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        while (!serverSocket.isClosed()) {
            try {
                new SocketHandler(serverSocket.accept()).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
    }
    public static void main(String[] args) {
        Server server=new Server();
        try {
            server.start(6666);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class SocketHandler extends Thread {
        private final Socket socket;
        private final PrintWriter out;
        private final BufferedReader in;

        public SocketHandler(Socket s) throws IOException {
            socket = s;
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        @Override
        public void run() {
            String message = null;
            while (true) {
                try {
                    message = in.readLine();
                    if (message == null) {
                        break;
                    }
                    System.out.println(message);
                    out.println("ack");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}