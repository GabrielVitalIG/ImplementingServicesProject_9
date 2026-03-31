package patterns;

public interface ServiceObserver {
    void onServiceUpdate(int sessionId, String message, boolean isCancelled);
}
