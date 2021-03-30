package pl.tkozyra.chat_client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.util.Arrays;

public class ClientReadingTaskUdpMulticast implements Runnable {

    private final MulticastSocket socketMulticast;
    private final DatagramSocket socketUdp;

    public ClientReadingTaskUdpMulticast(MulticastSocket socketMulticast, DatagramSocket socketUdp) {
        this.socketMulticast = socketMulticast;
        this.socketUdp = socketUdp;
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[1024];
            while (true) {
                Arrays.fill(buffer, (byte) 0);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socketMulticast.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                if (packet.getPort() != socketUdp.getLocalPort()) {
                    System.out.println(message);
                }
            }
        } catch (IOException e) {
            socketMulticast.close();
            e.printStackTrace();
        }
    }
}
