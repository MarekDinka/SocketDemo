import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {
    private ServerSocket serverSocket;
    private final int[] listOfFreePorts = {4002, 4003, 4004, 4005, 4006, 4007, 4008, 4009, 4010, 4011};

    public Server() throws ConnectException {
        boolean portFound = false;
        for (int port : listOfFreePorts) {
            try {
                serverSocket = new ServerSocket(port);
                portFound = true;
                break;
            } catch (BindException e) {
                System.out.printf("Port %d occupied, trying next one\n", port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!portFound) {
            throw new ConnectException("No free port found!");
        }
        System.out.println("Connection established!");
    }

    public void run() {
        while (!serverSocket.isClosed()) {
            try {
                new SocketHandler(serverSocket.accept()).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void exit() throws IOException {
        serverSocket.close();
    }
    public static void main(String[] args) {
        try {
            Server server=new Server();
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class SocketHandler extends Thread {
        private final Socket socket;
        private final PrintWriter out;
        private final BufferedReader in;
        private final String recognizeMeMessage = "abcd"; //open to suggestions

        public SocketHandler(Socket s) throws IOException {
            socket = s;
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println(recognizeMeMessage);
            if (!"found you!".equals(in.readLine())) {
                s.close();
                out.close();
                in.close();
                throw new ConnectException("Client did not respond");
            }
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