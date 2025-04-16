import java.net.Socket;

public class Client extends Thread{
    private Socket socket;
    private int port = 0;
    private String hostName = "localhost";
    private String filePath = "";

    public Client(String hostName, int port, String filePath) {
        this.hostName = hostName;
        this.port = port;
        this.filePath = filePath;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(hostname, port);
            System.out.println("Connected to server");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}