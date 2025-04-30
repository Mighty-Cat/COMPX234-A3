import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Client{
    private static Socket socket;
    private static int port = 0;
    private static String hostName = "localhost";
    private static String filePath = "";

    public Client(String hostName, int port, String filePath) {
        this.hostName = hostName;
        this.port = port;
        this.filePath = filePath;
    }

    public void main(String[] args) {
        try {
            socket = new Socket(hostName, port);
            System.out.println("Connected to server");

            List<String> requests = obtainRequest(filePath);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            for (String request : requests) {
                out.println(request);
                String response = in.readLine();
                System.out.println(request + ": " + response);
            }

            out.close();
            in.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Obtain the request from the request file
    public List<String> obtainRequest(String requestFilePath){
        List<String> request = new ArrayList<>();
        try (Stream<String> lines = Files.lines(Paths.get(requestFilePath))) {
            lines.forEach(line -> {
                line = line.trim();
                    if (!line.isEmpty()) {
                        String[] massages = line.split(" ");               
                        if ("PUT".equals(massages[0])) {
                            String value = String.join(" ", java.util.Arrays.copyOfRange(massages, 2, massages.length));//Get the value of the request
                            //Check if the length of the value is less than or equal to 970
                            if (massages[1].length() + value.length() > 970) {
                                System.out.printf("Error: %s exceeds the maximum length. Ignoring.%n", line);
                                return;
                            }
                            String lengthCode = String.format("%03d", String.format("000 P %s %s", massages[1], value).length());//Get the length of the request
                            request.add(String.format("%s P %s %s", lengthCode, massages[1], value));
                        } else if ("READ".equals(massages[0])) {
                            String lengthCode = String.format("%03d", String.format("000 R %s", massages[1]).length());
                            request.add(String.format("%s R %s", lengthCode, massages[1]));
                        } else if ("GET".equals(massages[0])) {
                            String lengthCode = String.format("%03d", String.format("000 G %s", massages[1]).length());
                            request.add(String.format("%s G %s", lengthCode, massages[1]));
                        } else {
                            System.out.printf("Error: Invalid command in line: %s%n", line);
                            return;
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        return request;
    }
}