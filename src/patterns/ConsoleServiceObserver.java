package patterns;

public class ConsoleServiceObserver implements ServiceObserver {
    /**
     * Prints the service update to the console.
     * @param sessionId the ID of the session that was updated
     * @param message the message describing the update
     * @param isCancelled indicates whether the session was cancelled or not
     */
    @Override
    public void onServiceUpdate(int sessionId, String message, boolean isCancelled) {
        System.out.println("[Organizer Update] Session " + sessionId +
                " | message=" + message +
                " | cancelled=" + isCancelled);
    }
}