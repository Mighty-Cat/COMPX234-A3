import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Client class that connects to a server, sends requests from a file, and processes responses.
 */
public class Client {
    private final String hostName;
    private final int port;
    private final String filePath;

    /**
     * Constructs a Client with the specified server hostname, port, and request file path.
     *
     * @param hostName the server hostname (e.g., "localhost")
     * @param port     the server port (50000 ≤ port ≤ 59999)
     * @param filePath the path to the file containing requests
     */
    public Client(String hostName, int port, String filePath) {
        this.hostName = hostName;
        this.port = port;
        this.filePath = filePath;
    }

    /**
     * Processes requests by connecting to the server, sending requests, and printing responses.
     */
    public void processRequests() {
        try (Socket socket = new Socket(hostName, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            System.out.println("Connected to server at " + hostName + ":" + port);

            List<String> requests = obtainRequests(filePath);
            for (String request : requests) {
                out.println(request);
                String response = in.readLine();
                if (response != null) {
                    System.out.println(parseRequestForOutput(request) + ": " + parseResponseForOutput(response));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads and parses requests from the specified file.
     *
     * @param requestFilePath the path to the file containing requests
     * @return a list of formatted request strings
     */
    private List<String> obtainRequests(String requestFilePath) {
        List<String> requests = new ArrayList<>();
        try (Stream<String> lines = Files.lines(Paths.get(requestFilePath))) {
            lines.forEach(line -> {
                line = line.trim();
                if (line.isEmpty()) {
                    return;
                }
                String[] parts = line.split("\\s+", 3);
                if (parts.length < 2 || !List.of("PUT", "READ", "GET").contains(parts[0])) {
                    System.out.printf("Error: Invalid command in line: %s%n", line);
                    return;
                }
                String command = parts[0];
                String key = parts[1];
                String value = parts.length == 3 ? parts[2] : "";

                if (key.length() > 999) {
                    System.out.printf("Error: Key in %s exceeds 999 characters. Ignoring.%n", line);
                    return;
                }
                if (command.equals("PUT")) {
                    if (value.length() > 999) {
                        System.out.printf("Error: Value in %s exceeds 999 characters. Ignoring.%n", line);
                        return;
                    }
                    String combined = key + " " + value;
                    if (combined.length() > 970) {
                        System.out.printf("Error: %s exceeds the maximum length of 970. Ignoring.%n", line);
                        return;
                    }
                    String message = String.format("P %s %s", key, value);
                    String lengthCode = String.format("%03d", message.length());
                    requests.add(lengthCode + message);
                } else {
                    String message = String.format("%s %s", command.charAt(0), key);
                    String lengthCode = String.format("%03d", message.length());
                    requests.add(lengthCode + message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return requests;
    }

    /**
     * Parses a request string for output, removing the length code.
     *
     * @param request the request string
     * @return the request without the length code
     */
    private String parseRequestForOutput(String request) {
        return request.substring(3).replaceFirst("P", "PUT").replaceFirst("R", "READ").replaceFirst("G", "GET");
    }

    /**
     * Parses a response string for output, removing the length code.
     *
     * @param response the response string
     * @return the response without the length code
     */
    private String parseResponseForOutput(String response) {
        return response.substring(3).replaceFirst("OK", "OK ");
    }
}