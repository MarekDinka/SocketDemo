package sk.uniba.fmph.Burnie;

import java.io.IOException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

/**
 * Class for handling all communication between GUI and Server
 */
public class ClientHandler {
    private final Client client;

    /**
     * Use provided IP to connect to server
     * @param IP server IP
     */
    public ClientHandler(String IP) throws IOException { //TODO -> if there is no server on IP, use UPD discovery
        client = new Client(IP);
        client.start();
        Runtime.getRuntime().addShutdownHook(new ExitProcess());
    }

    /**
     * Use UDP discovery to find server
     */
    public ClientHandler() throws IOException {
        client = new Client();
        client.start();
        Runtime.getRuntime().addShutdownHook(new ExitProcess());
    }

    /**
     * change ID of a specific controller
     * @param oldID current ID of said controller
     * @param newID new ID to replace it
     */
    public void changeControllerID(String oldID, String newID) throws IOException {
        if (!newID.matches("\\A\\p{ASCII}*\\z")) {
            throw new SocketException("Non ascii characters found!");
        }
        client.writeMessage(new Message(MessageBuilder.GUI.Request.ChangeControllerID.build()));
        client.writeMessage(new Message(oldID.getBytes(StandardCharsets.US_ASCII)));
        client.writeMessage(new Message(newID.getBytes(StandardCharsets.US_ASCII)));
    }

    /**
     * @return number of active projects
     */
    public int getNumberOfProjects() throws IOException, InterruptedException {
        client.writeMessage(new Message(MessageBuilder.GUI.Request.NumberOfProjects.build()));
        int result;
        synchronized (RequestResult.getInstance()) {
            RequestResult rr = RequestResult.getInstance();
            rr.wait();
            result = rr.getIntData();
        }
        return result;
    }

    /**
     * @return number of connected controllers
     */
    public int getNumberOfControllers() throws IOException, InterruptedException {
        client.writeMessage(new Message(MessageBuilder.GUI.Request.NumberOfControllers.build()));
        int result;
        synchronized (RequestResult.getInstance()) {
            RequestResult rr = RequestResult.getInstance();
            rr.wait();
            result = rr.getIntData();
        }
        return result;
    }

    private class ExitProcess extends Thread {
        @Override
        public void run() {
            try {
                client.stopConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
