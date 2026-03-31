package domain.model;

import java.time.LocalDateTime;

public class Reservation {
    private final int touristId;
    private final int sessionId;
    private final int guideId;
    private final int groupSize;
    private final LocalDateTime timestamp;

    public Reservation(int touristId, int sessionId, int guideId, int groupSize) {
        this.touristId = touristId;
        this.sessionId = sessionId;
        this.guideId = guideId;
        this.groupSize = groupSize;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("[%s] Tourist %d booked Session %d with Guide %d for %d people",
                timestamp, touristId, sessionId, guideId, groupSize);
    }
}