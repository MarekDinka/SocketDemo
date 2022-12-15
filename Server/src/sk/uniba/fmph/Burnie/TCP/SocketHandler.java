package sk.uniba.fmph.Burnie.TCP;

import sk.uniba.fmph.Burnie.Server;
import sk.uniba.fmph.Burnie.xml.FileReceiver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 *  a Communication thread created for each client -> allows client->server and server->client communication
 */
public class SocketHandler extends Thread {
//    private final static byte END_OF_MESSAGE = 4;
    private final Socket socket;
    private final BufferedOutputStream out;
    private final BufferedInputStream in;
    /**
     * A 'password' server will send so client can recognize it, or try different port if wrong server is running on this one
     */
    private final static byte[] SERVER_PASSWORD = new byte[]{'a', 'b', 'c', 'd'}; //open to suggestions

    /**
     * Constructor, receive socket, create in and out communication streams, send SERVER_PASSWORD to client
     * @param s a socket to which my client is connected
     * @throws IOException when something goes wrong with socket
     */
    public SocketHandler(Socket s) throws IOException {
        socket = s;
//            out =  new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)), true);
        out = new BufferedOutputStream(socket.getOutputStream());
        in = new BufferedInputStream(socket.getInputStream());
        System.out.println("Password send");
        writeMessage(new Message(SERVER_PASSWORD));
    }

    /**
     * Stop this socket
     * @throws IOException when something goes wrong
     */
    public void stopSocket() throws IOException {
        Server.getInstance().removeGUI(this);
        socket.close();
        out.close();
        in.close();
    }

    public boolean isActive() {
        return socket.isConnected();
    }

    /**
     * close connection if client does not respond with accepted message, if he does, handle the message
     * accepted messages:
     *      INITIALIZE_FILE_TRANSFER_MESSAGE -> xml file will shortly be sent from client
     *      END_OF_SEGMENT_MESSAGE -> EXE is informing us that segment has come to an end
     *      Message from Controller informing us of its IP address for http communication
     */

    private void writeMessage(Message msg) throws IOException {
        out.write(msg.getMessage());
        out.flush();
    }

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

    public void sendException(String className, byte[] exception) {
        try {
            writeMessage(new Message(className.getBytes(StandardCharsets.UTF_8)));
            writeMessage(new Message(exception));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        byte[] message = new byte[0];
        try {
            message = readLine();
        } catch (IOException e) {
            System.err.println("Client did not respond, disconnecting client");
        }
        try {
            if (MessageBuilder.EXE.FileTransfer.equals(message)) {
                FileReceiver.acceptFile(in, readStringLine());
                stopSocket();
            } else if (MessageBuilder.EXE.EndOfSegment.equals(message)) {
                String segmentName = readStringLine(), id = readStringLine();
                System.out.println("Segment named " + segmentName + " ended, id = " + id);
                stopSocket();
            } else if (MessageBuilder.GUI.equals(message)) {
                Server.getInstance().addGUI(this);
                System.out.println("GUI connected!");
            } else if (MessageBuilder.Controller.equals(message)) {
                System.out.println("Controller connected!");
            } else {
                System.err.println("Unrecognized message = " + Arrays.toString(message));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            int a = 2/0;
        } catch (Exception e) {
            Server.getInstance().sendExceptionToAllActiveGUIs(e);
        }
    }
}
