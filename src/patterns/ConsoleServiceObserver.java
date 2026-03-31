package patterns;

public class ConsoleServiceObserver implements ServiceObserver {
    @Override
    public void onServiceUpdate(int sessionId, String message, boolean isCancelled) {
        System.out.println("[Organizer Update] Session " + sessionId +
                " | message=" + message +
                " | cancelled=" + isCancelled);
    }
}