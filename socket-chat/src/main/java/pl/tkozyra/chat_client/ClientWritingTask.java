package pl.tkozyra.chat_client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class ClientWritingTask implements Runnable {
    private final Socket socket;
    private final DatagramSocket socketUdp;
    private PrintWriter out;

    public ClientWritingTask(Socket socket, DatagramSocket socketUdp) {
        this.socket = socket;
        this.socketUdp = socketUdp;

        try {
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            Scanner in = new Scanner(System.in);

            String userInput;
            userInput = in.nextLine();
            out.println(userInput);

            while (!userInput.equals("exit")) {
                userInput = in.nextLine();

                if (userInput.equals("U")) {
                    sendUdpData();
                } else if (userInput.equals("M")) {
                    sendUdpDataMulticast();
                } else {
                    out.println(userInput);
                }
            }

            socket.close();
            socketUdp.close();
            System.exit(-1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles sending UDP data (ASCII art) to the server
     *
     * @throws IOException
     */
    private void sendUdpData() throws IOException {
        String message = "[UDP channel]: \n" +
                "      _          _          _          _          _\n" +
                "    >(')____,  >(')____,  >(')____,  >(')____,  >(') ___,\n" +
                "      (` =~~/    (` =~~/    (` =~~/    (` =~~/    (` =~~/\n" +
                "---~^~^`---'~^~^~^`---'~^~^~^`---'~^~^~^`---'~^~^~^`---'~^~^~";
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, socket.getInetAddress(), socket.getPort());
        socketUdp.send(packet);
    }

    /**
     * Handles sending UDP data (ASCII art) to the multicast channel
     *
     * @throws IOException
     */
    private void sendUdpDataMulticast() throws IOException {
        String message = "[Multicast channel]:\n" +
                "                 ,-.\n" +
                "         ,      ( {o\\\n" +
                "         {`\"=,___) (`~\n" +
                "          \\  ,_.-   )\n" +
                "      ~^~^~^`- ~^ ~^ '~^~^~^~";
        InetAddress multicastGroupAddress = InetAddress.getByName("225.0.0.0");
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, multicastGroupAddress, 4000);
        socketUdp.send(packet);
    }
}
