package sk.uniba.fmph.Burnie;

import sk.uniba.fmph.Burnie.TCP.SocketHandler;

import java.io.*;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.List;

/**
 * Main server class
 */
public class Server {
    private final static Server INSTANCE = new Server();
    public static Server getInstance() {return INSTANCE;}

    private ServerSocket serverSocket;
    public final static int PORT = 4002;

    private final List<SocketHandler> activeGUIs = new LinkedList<>();

    /**
     * Constructor, find port and initialize TCP socket
     */
    private Server() {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        UDPCommunicationHandler.getInstance().start();
        System.out.println("Connection established!");
    }

    /**
     * Start accepting clients
     */
    public void begin() {
        while (!serverSocket.isClosed()) {
            try {
                new SocketHandler(serverSocket.accept()).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * new GUI has connected to server
     * @param sh GUI
     */
    public void addGUI(SocketHandler sh) {
        synchronized (activeGUIs) {
            activeGUIs.add(sh);
        }
    }

    /**
     * A GUI has disconnected from server
     * @param sh GUI
     */
    public void removeGUI(SocketHandler sh) {
        synchronized (activeGUIs) {
            activeGUIs.remove(sh);
        }
    }

    /**
     * An exception has arrisen in server or other parts, and we will attempt to send it to any active GUI
     * @param th the exception
     */
    public synchronized void sendExceptionToAllActiveGUIs(Throwable th) {
        try {
            String c = th.getClass().getCanonicalName();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(baos);
            oo.writeObject(th.getStackTrace());
//            th.printStackTrace(new PrintStream(oo));
            byte[] exception = baos.toByteArray();
            List<SocketHandler> toRemove = new LinkedList<>();
            for (SocketHandler gui : activeGUIs) {
                if (!gui.isActive()) {
                    toRemove.add(gui);
                    continue;
                }
//                System.out.println(c);
                gui.sendException(c, exception);
            }
            for (SocketHandler gui : toRemove) {
                removeGUI(gui);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop the server
     * @throws IOException when something goes wrong
     */
    public void exit() throws IOException {
        serverSocket.close();
    }
}