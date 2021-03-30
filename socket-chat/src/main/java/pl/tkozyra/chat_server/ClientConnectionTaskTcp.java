package pl.tkozyra.chat_server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientConnectionTaskTcp implements Runnable {

    private final Socket socket;
    private final ClientService clientService;
    private PrintWriter out;
    private String username;

    public ClientConnectionTaskTcp(Socket socket, ClientService clientService) {
        this.socket = socket;
        this.clientService = clientService;
    }

    @Override
    public void run() {
        try {

            //set input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            //get username from the user
            out.println("--- Enter your username: ");
            String userInput = "";
            while (username == null) {
                userInput = in.readLine();
                if (!clientService.usernameTaken(userInput)) {
                    username = userInput;
                    out.println("--- You can now chat with other users ---");
                } else {
                    out.println("--- Username already taken. Enter another username: ");
                }
            }

            //register user
            clientService.addClient(this);

            String message = "--- User " + "[" + username + "]" + " has entered the chat ---";
            System.out.println(message);
            sendToAllClients(message, this);

            while (!userInput.equals("exit")) {
                userInput = in.readLine();
                if (userInput.equals("exit")) {
                    closeConnection();
                } else {
                    sendToAllClients("[" + username + "]: " + userInput, this);
                }
            }

        } catch (IOException e) {
            closeConnection();
        }
    }

    /**
     * Handles sending TCP message to all connected clients, except selected client
     *
     * @param message                 message to send
     * @param clientConnectionTaskTcp the client we don't want to send the message to
     */
    private void sendToAllClients(String message, ClientConnectionTaskTcp clientConnectionTaskTcp) {
        clientService.getClients().stream()
                .filter(client -> !client.equals(clientConnectionTaskTcp))
                .forEach(client -> client.out.println(message));
    }

    private void closeConnection() {
        String message = "--- User " + "[" + username + "]" + " has left the chat ---";
        clientService.removeClient(this);
        sendToAllClients(message, this);
        System.out.println(message);
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getClientSocket() {
        return socket;
    }

    public String getUsername() {
        return username;
    }
}
