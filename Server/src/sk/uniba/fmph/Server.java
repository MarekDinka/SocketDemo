package sk.uniba.fmph;

import sk.uniba.fmph.TCP.SocketHandler;

import java.io.*;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.List;

/**
 * Main server class
 */
public class Server { //TODO -> sort out exceptions
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        th.printStackTrace(new PrintStream(baos));
        byte[] exception = baos.toByteArray();
        List<SocketHandler> toRemove = new LinkedList<>();
        for (SocketHandler gui : activeGUIs) {
            if (!gui.isActive()) {
                toRemove.add(gui);
                continue;
            }
            gui.sendException(exception);
        }
        for (SocketHandler gui : toRemove) {
            removeGUI(gui);
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