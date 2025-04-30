public class Main {
    public static void main(String[] args) {
        Server server = new Server();
        new Thread(server::start).start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int clientCount = 10;
        for (int i = 1; i <= clientCount; i++) {
            String filePath = "client_" + i + ".txt";
            Client client = new Client("localhost", 50000, filePath);
            new Thread(() -> client.main(new String[]{})).start();
        }
    }
}