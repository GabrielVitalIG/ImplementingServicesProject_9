package logger;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A thread-safe log for recording completed transactions.
 */
public class SalesLog {
    private final ConcurrentLinkedQueue<String> transactions = new ConcurrentLinkedQueue<>();   // Thread-safe queue to store transaction logs

    // Adds a new transaction entry to the log
    public void addEntry(String entry) {
        transactions.add(entry);
    }

    // Returns the total number of transactions recorded in the log
    public int getLogCount() {
        return transactions.size();
    }

        // Prints all transaction entries in the log
    public void printAll() {
        transactions.forEach(System.out::println);
    }
}