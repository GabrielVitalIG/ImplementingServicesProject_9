package domain.model;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a bookable activity with a strictly limited capacity.
 */
public class Session {
    private final int id;
    private final String title;
    private final double price;
    private final int maxCapacity;
    private final AtomicInteger reservedSeats;

    public Session(int id, String title, double price, int maxCapacity) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.maxCapacity = maxCapacity;
        this.reservedSeats = new AtomicInteger(0);
    }

    /**
     * Attempts to increment the reserved seat counter.
     * @return true if a spot was successfully secured.
     */
    public boolean reserveSpot() {
        while (true) {
            int currentReserved = reservedSeats.get();
            if (currentReserved >= maxCapacity) {
                return false; // Capacity reached
            }
            // Compare and Set ensures atomicity under high contention
            if (reservedSeats.compareAndSet(currentReserved, currentReserved + 1)) {
                return true;
            }
        }
    }

    /**
     * Rolls back a reservation (e.g., if guide assignment fails).
     */
    public void releaseSpot() {
        reservedSeats.decrementAndGet();
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public double getPrice() { return price; }
    public int getAvailableSpots() { return maxCapacity - reservedSeats.get(); }
}
