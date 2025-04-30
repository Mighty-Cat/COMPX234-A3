import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server{
    private static final int PORT = 50000;
    private static final long PTINT_INTERVAL = 10000;// 10 seconds
    private static int tupleCount = 0;
    private static int operationCount = 0;
    private static int readCount = 0;
    private static int getCount = 0;
    private static int putCount = 0;
    private static int errorCount = 0;
    private static int clientCount = 0;

    private static Map<String, String> tupleSpace = new HashMap<>();//A key-value pair mapping relationship,a global variable that holds key-value pairs 
    private String Response;//It is used to record the response output by the server to the client

     public void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(PTINT_INTERVAL);
                        print();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientCount++;
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void print() {
        int totalSize = 0,keySize = 0,valueSize = 0;
        // Calculate the total size, key size, and value size of all tuples in the tuple space
         for (Map.Entry<String, String> _tuple : tupleSpace.entrySet()) {
            totalSize += _tuple.getKey().length() + _tuple.getValue().length();
            keySize += _tuple.getKey().length();
            valueSize += _tuple.getValue().length();
        }
        // Calculate the average tuple size, key size, and value size
        double avgTupleSize = tupleSpace.isEmpty() ? 0 : (double) totalSize / tupleSpace.size();//if ture space is empty, return 0
        double avgKeySize = tupleSpace.isEmpty() ? 0 : (double) keySize / tupleSpace.size();
        double avgValueSize = tupleSpace.isEmpty() ? 0 : (double) valueSize / tupleSpace.size();
        // Print the summary
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


    public String Read(String k){
        String v;
        if(tupleSpace.containsKey(k)){
            v = tupleSpace.get(k);
            readCount++;
            Response = String.format("RAED %s: OK (%s, %s) read",k, k, v);
        }else{
            v = null;
            errorCount++;
            Response = String.format("READ %s:ERR (%s) does not exist",k, k);
        }
        return v;
    }

    public String Get(String k){
        String v;
        if(tupleSpace.containsKey(k)){
            v = tupleSpace.remove(k);
            getCount++;
            Response = String.format("GET %s: OK (%s, %s) removed",k, k, v);
        }else{
            v = null;
            errorCount++;
            Response = String.format("GET %s: ERR (%s) does not exist",k, k);
        }
        return v;
    }

    public int Put(String k, String v){
        int e;
        if(tupleSpace.containsKey(k)){
            e = 1;
            errorCount++;
            Response = String.format("PUT %s: ERR (%s) already exists",k, k);
        }else{
            e = 0;
            tupleSpace.put(k, v);
            putCount++;
            Response = String.format("PUT %s: OK (%s, %s) added",k, k, v);
        }
        return e;
    }

    // This method handles the client request and sends the response back to the client
    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String request;
            while ((request = in.readLine()) != null) {
                operationCount++;
                String command = request.substring(3, 4);
                String key = request.substring(5, request.contains(" ") ? request.indexOf(" ") : request.length());
                String value = request.contains(" ") && command.equals("P")? request.substring(request.indexOf(" ") + 1) : "";

                switch (command) {
                    case "R":
                        String readValue = Read(key);
                        out.println(readValue != null? String.format("0%d OK (%s, %s) read", ("OK (" + key + ", " + readValue + ") read").length(), key, readValue) : "024 ERR " + key + " does not exist");
                        break;
                    case "G":
                        String getValue = Get(key);
                        out.println(getValue != null? String.format("0%d OK (%s, %s) removed", ("OK (" + key + ", " + getValue + ") removed").length(), key, getValue) : "024 ERR " + key + " does not exist");
                        break;
                    case "P":
                        int putResult = Put(key, value);
                        out.println(putResult == 0? String.format("0%d OK (%s, %s) added", ("OK (" + key + ", " + value + ") added").length(), key, value) : "024 ERR " + key + " already exists");
                        break;
                }
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