package sk.uniba.fmph.TCP;

import sk.uniba.fmph.Server;
import sk.uniba.fmph.xml.FileReceiver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

/**
 *  a Communication thread created for each client -> allows client->server and server->client communication
 */
public class SocketHandler extends Thread { //TODO -> communicate in bytes
//    private final static byte[] RECOGNIZE_EXE_MESSAGE = {69, 88, 69};
//    private final static byte[] INITIALIZE_FILE_TRANSFER_MESSAGE = {70, 73, 76, 69};
//    private final static byte[] END_OF_SEGMENT_MESSAGE = {69, 78, 68};
//    private final static byte[] CONTROLLER_RECOGNIZE_ME_MESSAGE = {67, 79, 78, 84, 82, 79, 76};
    private final static byte END_OF_MESSAGE = 4;
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
        writeBytes(SERVER_PASSWORD);
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

    private void writeBytes(byte[] msg) throws IOException {
        out.write(msg);
        out.write(END_OF_MESSAGE);
        out.flush();
    }
    private byte[] readLine() throws IOException {
        byte[] buffer = new byte[4096];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte b = 0;
        int count = 0;
        for (; count < 4096; count++) {
            b = (byte) in.read();
            if (b == END_OF_MESSAGE || b == -1) {
                break;
            }
            buffer[count] = b;
        }
        out.write(buffer, 0, count);
        return out.toByteArray();
    }

    private String readStringLine() throws IOException {
        byte[] buffer = new byte[4096];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte b = 0;
        int count = 0;
        for (; count < 4096; count++) {
            b = (byte) in.read();
            if (b == END_OF_MESSAGE || b == -1) {
                break;
            }
            buffer[count] = b;
        }
        out.write(buffer, 0, count);
        return out.toString();
    }

    public void sendException(byte[] exception) {
        try {
            writeBytes(MessageBuilder.GUI.Exception.build());
            writeBytes(exception);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() { // TODO -> remove commented stuff
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
//            if (Arrays.equals(RECOGNIZE_EXE_MESSAGE, message)) {
//                message = readLine();
//                if (Arrays.equals(END_OF_SEGMENT_MESSAGE, message)) {
//                    String segmentName = readStringLine(), id = readStringLine();
//                    System.out.println("Segment named " + segmentName + " ended, id = " + id);
//                } else if (Arrays.equals(INITIALIZE_FILE_TRANSFER_MESSAGE, message)) {
//                    FileReceiver.acceptFile(in, readStringLine());
//                } else {
//                    System.out.println("Wrong message received, disconnecting");
//                }
//                stopSocket();
//                return;
//            }
//            if (Arrays.equals(CONTROLLER_RECOGNIZE_ME_MESSAGE, message)) {
//                System.out.println("Found controller");
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
