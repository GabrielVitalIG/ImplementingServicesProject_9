package patterns;

public interface ServiceObserver {
    // Method to be called when the service updates its status
    void onServiceUpdate(int sessionId, String message, boolean isCancelled);
}
