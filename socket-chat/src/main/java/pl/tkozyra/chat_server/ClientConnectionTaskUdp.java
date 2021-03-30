package pl.tkozyra.chat_server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

public class ClientConnectionTaskUdp implements Runnable {
    private final DatagramSocket socketUdp;
    private final ClientService clientService;

    public ClientConnectionTaskUdp(DatagramSocket socketUdp, ClientService clientService) {
        this.socketUdp = socketUdp;
        this.clientService = clientService;
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[1024];
            while (true) {
                Arrays.fill(buffer, (byte) 0);
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                socketUdp.receive(receivePacket);
                String message = new String(receivePacket.getData());
                sendToAllClients(message, receivePacket.getPort());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socketUdp != null) {
                socketUdp.close();
            }
        }
    }

    /**
     * Handles sending UDP packet to all clients connected to the chat, except the client connected on selected port
     *
     * @param message message to send
     * @param port    port to which we don't want to send the message
     */
    private void sendToAllClients(String message, int port) {
        clientService.getClients().stream()
                .filter(client -> client.getClientSocket().getPort() != port)
                .forEach(client -> {
                    try {
                        Socket clientSocket = client.getClientSocket();
                        byte[] buffer = message.getBytes();
                        InetAddress clientAddress = clientSocket.getInetAddress();
                        int clientPortNumber = clientSocket.getPort();
                        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length, clientAddress, clientPortNumber);
                        socketUdp.send(responsePacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

}
