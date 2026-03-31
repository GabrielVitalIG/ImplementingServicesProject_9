package logger;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A thread-safe log for recording completed transactions.
 */
public class SalesLog {
    // ConcurrentLinkedQueue is lock-free and highly performant for many writers
    private final ConcurrentLinkedQueue<String> transactions = new ConcurrentLinkedQueue<>();

    public void addEntry(String entry) {
        transactions.add(entry);
    }

    public int getLogCount() {
        return transactions.size();
    }

    public void printAll() {
        transactions.forEach(System.out::println);
    }
}