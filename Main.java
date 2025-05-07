/**
 * Main class to start the server and multiple clients for the tuple space system.
 */
public class Main {
    public static void main(String[] args) {
        // Start the server in a separate thread
        Server server = new Server();
        new Thread(server::start).start();

        // Wait for the server to initialize
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Start 10 clients, each with a unique input file
        int clientCount = 10;
        for (int i = 1; i <= clientCount; i++) {
            String filePath = "client_" + i + ".txt";
            Client client = new Client("localhost", 50000, filePath);
            new Thread(() -> client.processRequests()).start();
        }
    }
}