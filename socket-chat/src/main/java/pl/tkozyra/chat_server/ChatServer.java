package pl.tkozyra.chat_server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    private static final int portNumber = 12345;
    private final ClientService clientService;

    public ChatServer() {
        this.clientService = new ClientService();
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.start();
    }

    public void start() {
        try {
            //create executor service with thread pool
            ExecutorService executorService = Executors.newCachedThreadPool();

            System.out.println("--- CHAT SERVER ---");

            ServerSocket serverSocket = new ServerSocket(portNumber);
            DatagramSocket clientSocketUdp = new DatagramSocket(portNumber);

            ClientConnectionTaskUdp clientConnectionTaskUdp = new ClientConnectionTaskUdp(clientSocketUdp, clientService);
            executorService.submit(clientConnectionTaskUdp);

            while (true) {

                Socket clientSocket = serverSocket.accept();

                //create and run TCP client task
                ClientConnectionTaskTcp clientConnectionTaskTcp = new ClientConnectionTaskTcp(clientSocket, clientService);
                executorService.submit(clientConnectionTaskTcp);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
