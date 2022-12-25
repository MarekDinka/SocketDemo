package Burnie.Communication;

import Burnie.Controller.ControllerException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MockControllerHandler extends ControllerHandler {
//    private final SocketHandler socket;
//    private final Controller controller;
    private String ID;

    public MockControllerHandler(String id) {
        super(null, null);
        ID = id;
    }
//        Server.getInstance().addController(this);
//        socket = sh;
//        controller = new Controller(ip);
//    }

    private String resolveId(byte[] msg) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            if (msg[i] == 0) {
                break;
            }
            sb.append((char) msg[i]);
        }
        return sb.toString();
    }

    public String getControllerID() {return ID;}

    public void changeId(String newId) throws ControllerException, IOException {
        if (newId.length() > 15) {
            throw new ControllerException("new id too long");
        }
        if (!newId.matches("\\A\\p{ASCII}*\\z")) {
            throw new ControllerException("Non ascii characters found!");
        }
        ID = newId;
        byte[] id = newId.getBytes(StandardCharsets.US_ASCII);
        byte[] message = new byte[16];
        for (int i = 0; i < message.length; i++) {
            if (i == 15) {
                message[i] = 0b00000010;
            } else if (i < id.length) {
                message[i] = id[i];
            } else {
                message[i] = 0;
            }
        }
        System.out.println("[" + ID + "] Writing message = " + Arrays.toString(message));
//        socket.writeMessage(new Message(message, true));
    }

    public void changeControllerParameters(int temperature, short airFlow, long time) throws IOException {
        System.out.println("[" + ID + "] " + temperature + " " + airFlow + " " + time);
//        controller.setTime(time);
//        controller.setAirFlow(airFlow);
//        controller.setCurrentTemperature(temperature);

        ByteBuffer tempBuffer = ByteBuffer.allocate(4).putInt(temperature),
                   airFlowBuffer = ByteBuffer.allocate(2).putShort(airFlow),
                   timeBuffer = ByteBuffer.allocate(8).putLong(time);
        byte[] byteTemperature = tempBuffer.array(), byteAirFlow = airFlowBuffer.array(), byteTime = timeBuffer.array();
//        System.out.println("[MCH] " + Arrays.toString(byteTemperature));
        byte[] message = new byte[] {0, 0, 0, 0,
                byteTemperature[2], byteTemperature[3], //because java has no unsigned, for some wild reason
                byteAirFlow[1],
                byteTime[0],byteTime[1],byteTime[2],byteTime[3],byteTime[4],byteTime[5],byteTime[6],byteTime[7],
                (byte) 0b00000001};
        System.out.println("[" + ID + "] changing parameters, message = " + Arrays.toString(message));
//        socket.writeMessage(new Message(message, true));
    }

    /**
     * Stop controller after big red button has been pressed
     */
    public void bigRedButton() throws IOException {
        System.out.println("[" + ID + "] Big red button");
//        socket.writeMessage(new Message(new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0b10000000}, true));
    }

    /**
     * Await messages from controller, for detailed explanation, please refer to documentation
     */
    @Override
    public void run() {
//        byte[] msg;
//        while (socket.isActive()) {
//            try {
//                msg = socket.readMessage(true);
//                byte flags = msg[15];
//                if (flags == 0b0000010) {
//                    controller.setID(resolveId(msg));
//                } else if ((flags&0b00000001) == 1) {
//                    if ((flags&0b00000100) > 0) {
//                        throw new ControllerException("Temperature cannot be read");
//                    }
//                    if ((flags&0b00001000) > 0) {
//                        throw new ControllerException("DAC not found");
//                    }
//                    if ((flags&0b00010000) > 0) {
//                        throw new ControllerException(""); //TODO -> inform GUI that controller is still working
//                    }
//                    byte[] temp = new byte[] {0, 0, msg[13], msg[14]};
//                    controller.setCurrentTemperature(ByteBuffer.wrap(temp).getInt());
//                } else {
//                    throw new ControllerException("Unknown message" + Arrays.toString(msg));
//                }
//            } catch (SocketException e) {
//                socket.stopSocket();
//                Server.getInstance().sendExceptionToAllActiveGUIs(e);
//            } catch (Exception e) {
//                Server.getInstance().sendExceptionToAllActiveGUIs(e);
//            }
//        }
    }
}
