package pl.tkozyra.chat_client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientReadingTask implements Runnable {
    private final Socket socket;
    private BufferedReader in;

    public ClientReadingTask(Socket socket) {
        this.socket = socket;

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                System.out.println(in.readLine());
            }
        } catch (IOException ex) {
            try {
                socket.close();
                printExitMessage();
                System.exit(-1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void printExitMessage() {
        System.out.println("--- You have been disconnected from the server ---");
    }
}
