import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Server class that implements a multi-threaded tuple space with READ, GET, and PUT operations.
 */
public class Server {
    private static final int PORT = 50000;
    private static final long PRINT_INTERVAL = 10000; // 10 seconds
    private static int tupleCount = 0;
    private static int operationCount = 0;
    private static int readCount = 0;
    private static int getCount = 0;
    private static int putCount = 0;
    private static int errorCount = 0;
    private static int clientCount = 0;

    private static final Map<String, String> tupleSpace = new HashMap<>();

    /**
     * Starts the server, listening for client connections and spawning threads to handle them.
     */
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            // Start a thread to print statistics every 10 seconds
            new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(PRINT_INTERVAL);
                        printStatistics();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            // Accept client connections
            while (true) {
                Socket clientSocket = serverSocket.accept();
                synchronized (this) {
                    clientCount++;
                }
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prints statistics about the tuple space and server operations.
     */
    private void printStatistics() {
        synchronized (tupleSpace) {
            int totalSize = 0, keySize = 0, valueSize = 0;
            for (Map.Entry<String, String> tuple : tupleSpace.entrySet()) {
                totalSize += tuple.getKey().length() + tuple.getValue().length();
                keySize += tuple.getKey().length();
                valueSize += tuple.getValue().length();
            }
            double avgTupleSize = tupleSpace.isEmpty() ? 0 : (double) totalSize / tupleSpace.size();
            double avgKeySize = tupleSpace.isEmpty() ? 0 : (double) keySize / tupleSpace.size();
            double avgValueSize = tupleSpace.isEmpty() ? 0 : (double) valueSize / tupleSpace.size();

            System.out.println("Average tuple size: " + avgTupleSize);
            System.out.println("Average key size: " + avgKeySize);
            System.out.println("Average value size: " + avgValueSize);
            System.out.println("Total clients: " + clientCount);
            System.out.println("Total operations: " + operationCount);
            System.out.println("READ operations: " + readCount);
            System.out.println("GET operations: " + getCount);
            System.out.println("PUT operations: " + putCount);
            System.out.println("Error count: " + errorCount);
        }
    }

    /**
     * Reads the value associated with the key from the tuple space.
     *
     * @param key the key to read
     * @return the value if the key exists, null otherwise
     */
    private synchronized String read(String key) {
        if (tupleSpace.containsKey(key)) {
            readCount++;
            return tupleSpace.get(key);
        }
        errorCount++;
        return null;
    }

    /**
     * Removes and returns the value associated with the key from the tuple space.
     *
     * @param key the key to remove
     * @return the value if the key exists, null otherwise
     */
    private synchronized String get(String key) {
        if (tupleSpace.containsKey(key)) {
            getCount++;
            return tupleSpace.remove(key);
        }
        errorCount++;
        return null;
    }

    /**
     * Adds a key-value pair to the tuple space.
     *
     * @param key   the key to add
     * @param value the value to associate with the key
     * @return 0 if successful, 1 if the key already exists
     */
    private synchronized int put(String key, String value) {
        if (tupleSpace.containsKey(key)) {
            errorCount++;
            return 1;
        }
        tupleSpace.put(key, value);
        putCount++;
        return 0;
    }

    /**
     * Handles a client connection, processing requests and sending responses.
     *
     * @param clientSocket the socket connected to the client
     */
    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String request;
            while ((request = in.readLine()) != null) {
                synchronized (this) {
                    operationCount++;
                }
                // Parse request: NNN C k [v]
                if (request.length() < 4) {
                    out.println("024 ERR invalid request");
                    continue;
                }
                String lengthCode = request.substring(0, 3);
                String message = request.substring(3);
                if (!lengthCode.matches("\\d{3}") || Integer.parseInt(lengthCode) != message.length()) {
                    out.println("024 ERR invalid length");
                    continue;
                }
                String[] parts = message.split("\\s+", 3);
                if (parts.length < 2) {
                    out.println("024 ERR invalid format");
                    continue;
                }
                String command = parts[0];
                String key = parts[1];
                String value = parts.length == 3 ? parts[2] : "";

                String response;
                switch (command) {
                    case "R":
                        String readValue = read(key);
                        response = readValue != null
                                ? String.format("OK (%s, %s) read", key, readValue)
                                : String.format("ERR %s does not exist", key);
                        break;
                    case "G":
                        String getValue = get(key);
                        response = getValue != null
                                ? String.format("OK (%s, %s) removed", key, getValue)
                                : String.format("ERR %s does not exist", key);
                        break;
                    case "P":
                        int putResult = put(key, value);
                        response = putResult == 0
                                ? String.format("OK (%s, %s) added", key, value)
                                : String.format("ERR %s already exists", key);
                        break;
                    default:
                        response = "ERR invalid command";
                        break;
                }
                out.println(String.format("%03d %s", response.length(), response));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}