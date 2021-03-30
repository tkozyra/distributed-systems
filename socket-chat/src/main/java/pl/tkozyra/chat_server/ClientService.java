package pl.tkozyra.chat_server;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClientService {
    List<ClientConnectionTaskTcp> clients;

    public ClientService() {
        clients = new ArrayList<>();
    }

    public void removeClient(ClientConnectionTaskTcp client) {
        this.clients.remove(client);
    }

    public void addClient(ClientConnectionTaskTcp client) {
        this.clients.add(client);
    }

    public List<ClientConnectionTaskTcp> getClients() {
        return clients;
    }

    public List<String> getUsernames() {
        return clients.stream().map(ClientConnectionTaskTcp::getUsername).collect(Collectors.toList());
    }

    public boolean usernameTaken(String username) {
        return getUsernames().contains(username);
    }
}
