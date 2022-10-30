package sk.uniba.fmph.Arduino;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

public class CommunicationHandler {
    private static final CommunicationHandler INSTANCE = new CommunicationHandler();
    private CommunicationHandler() {}
    public static CommunicationHandler getInstance() {return INSTANCE;}

    private final List<Arduino> arduinos = new LinkedList<>();

    private List<InetAddress> getBroadcastAddresses() {
        List<InetAddress> broadcastList = new LinkedList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback())
                    continue;
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast != null) {
                        broadcastList.add(broadcast);
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return broadcastList;
    }

    /**
     * Send UDP broadcast to local network and await response from all arduinos
     */
    public void requestArduinoIps() {
        List<InetAddress> broadcasts = getBroadcastAddresses();
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] buff = "Hear me!".getBytes();
            for (InetAddress b : broadcasts) {
                socket.send(new DatagramPacket(buff, buff.length, b, 4002));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addArduinoToList(Arduino a) {
        System.out.println(a.IP);
        System.out.println("Arduino added!");
        arduinos.add(a);
    }
}
