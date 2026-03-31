package concurrency;

import domain.model.Session;

import java.util.concurrent.atomic.AtomicBoolean;

public class CartReservation {
    private final int touristId;                // Unique identifier for the tourist making the reservation
    private final Session session;              // The session for which the reservation is made
    private final int groupSize;                // The number of people in the tourist's group
    private final long expireAt;                // Timestamp indicating when the reservation expires
    private final AtomicBoolean confirmed;      // Atomic boolean to track if the reservation has been confirmed

    public CartReservation(int touristId, Session session, int groupSize, long delayMs) {
        this.touristId = touristId;
        this.session = session;
        this.groupSize = groupSize;
        this.expireAt = System.currentTimeMillis() + delayMs;
        this.confirmed = new AtomicBoolean(false);
    }

    // Getters for the reservation details
    public int getTouristId() {
        return touristId;
    }

    // Returns the session associated with this reservation
    public Session getSession() {
        return session;
    }

    // Returns the size of the group for this reservation
    public int getGroupSize() {
        return groupSize;
    }

    // Returns the expiration timestamp for this reservation
    public long getExpireAt() {
        return expireAt;
    }

    // Checks if the reservation has been confirmed
    public boolean isConfirmed() {
        return confirmed.get();
    }

    // Marks the reservation as confirmed
    public void confirm() {
        confirmed.set(true);
    }
}