package pl.tkozyra.chat_client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class ClientReadingTaskUdp implements Runnable {
    private final DatagramSocket socketUdp;

    public ClientReadingTaskUdp(DatagramSocket socketUdp) {
        this.socketUdp = socketUdp;
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[1024];
            while (true) {
                Arrays.fill(buffer, (byte) 0);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socketUdp.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println(message);
            }
        } catch (IOException e) {
            socketUdp.close();
            e.printStackTrace();
        }
    }
}
