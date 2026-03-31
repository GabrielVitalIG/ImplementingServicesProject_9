package concurrency;

import domain.model.Session;

import java.util.concurrent.atomic.AtomicBoolean;

public class CartReservation {
    private final int touristId;
    private final Session session;
    private final int groupSize;
    private final long expireAt;
    private final AtomicBoolean confirmed;

    public CartReservation(int touristId, Session session, int groupSize, long delayMs) {
        this.touristId = touristId;
        this.session = session;
        this.groupSize = groupSize;
        this.expireAt = System.currentTimeMillis() + delayMs;
        this.confirmed = new AtomicBoolean(false);
    }

    public int getTouristId() {
        return touristId;
    }

    public Session getSession() {
        return session;
    }

    public int getGroupSize() {
        return groupSize;
    }

    public long getExpireAt() {
        return expireAt;
    }

    public boolean isConfirmed() {
        return confirmed.get();
    }

    public void confirm() {
        confirmed.set(true);
    }
}