package domain.model;

import java.time.LocalDateTime;

public class Reservation {
    private final int touristId;                // Unique identifier for the tourist making the reservation
    private final int sessionId;                // Unique identifier for the session being reserved
    private final int guideId;                  // Unique identifier for the guide assigned to the session
    private final int groupSize;                // Number of people in the tourist's group for the reservation
    private final LocalDateTime timestamp;      // Timestamp of when the reservation was made

    public Reservation(int touristId, int sessionId, int guideId, int groupSize) {
        this.touristId = touristId;
        this.sessionId = sessionId;
        this.guideId = guideId;
        this.groupSize = groupSize;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Returns a string representation of the reservation, including the timestamp, tourist ID, session ID, guide ID, and group size.
     * @return A string representation of the reservation details.
     */
    @Override
    public String toString() {
        return String.format("[%s] Tourist %d booked Session %d with Guide %d for %d people",
                timestamp, touristId, sessionId, guideId, groupSize);
    }
}