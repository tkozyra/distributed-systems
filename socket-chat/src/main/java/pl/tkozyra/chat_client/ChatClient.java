package pl.tkozyra.chat_client;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatClient {
    private final String hostname;
    private final int portNumber;
    private final int portNumberMulticast = 4000;
    private final String addressMulticast = "225.0.0.0";

    public ChatClient(String hostname, int portNumber) {
        this.hostname = hostname;
        this.portNumber = portNumber;
    }

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient("localhost", 12345);
        chatClient.connect();
    }

    public void connect() {
        try {
            System.out.println("--- CHAT CLIENT ---");

            //create executor service with thread pool
            ExecutorService executorService = Executors.newFixedThreadPool(4);

            // create TCP and UDP socket
            Socket socket = new Socket(hostname, portNumber);
            DatagramSocket socketUdp = new DatagramSocket(socket.getLocalPort());

            //execute writing task (TCP and UDP)
            ClientWritingTask clientWritingTask = new ClientWritingTask(socket, socketUdp);
            executorService.submit(clientWritingTask);

            //execute reading task (TCP)
            ClientReadingTask clientReadingTask = new ClientReadingTask(socket);
            executorService.submit(clientReadingTask);

            //execute reading task (UDP)
            ClientReadingTaskUdp clientReadingTaskUdp = new ClientReadingTaskUdp(socketUdp);
            executorService.submit(clientReadingTaskUdp);

            //execute reading task (UDP Multicast)
            InetAddress multicastGroupAddress = InetAddress.getByName(addressMulticast);
            MulticastSocket socketMulticast = new MulticastSocket(portNumberMulticast);
            socketMulticast.joinGroup(multicastGroupAddress);
            ClientReadingTaskUdpMulticast clientReadingTaskUdpMulticast = new ClientReadingTaskUdpMulticast(socketMulticast, socketUdp);
            executorService.submit(clientReadingTaskUdpMulticast);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
