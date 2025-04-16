import java.util.HashMap;
import java.util.Map;

public class Server extends Thread{
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

    @Override
    public void run() {
        // Start the periodic printing thread
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
        }else{
            v = null;
            errorCount++;
        }
        return v;
    }
}