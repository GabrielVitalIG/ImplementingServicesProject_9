package domain.model;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a bookable activity with a strictly limited capacity.
 */
public class Session {
    private final int id;
    private final String title;
    private final double price;
    private final AtomicInteger maxCapacity;
    private final String timeSlot;
    private final boolean requiresGuide;

    private final AtomicInteger reservedSeats;
    private final AtomicBoolean cancelled;

    public Session(int id, String title, double price, int maxCapacity, String timeSlot, boolean requiresGuide) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.maxCapacity = new AtomicInteger(maxCapacity);
        this.timeSlot = timeSlot;
        this.requiresGuide = requiresGuide;
        this.reservedSeats = new AtomicInteger(0);
        this.cancelled = new AtomicBoolean(false);
    }

    public boolean reserveSpots(int count) {
        while (true) {
            int currentReserved = reservedSeats.get();

            if (cancelled.get()) {
                return false;
            }

            if (currentReserved + count > maxCapacity.get()) {
                return false;
            }

            if (reservedSeats.compareAndSet(currentReserved, currentReserved + count)) {
                return true;
            }
        }
    }

    public void releaseSpots(int count) {
        while (true) {
            int currentReserved = reservedSeats.get();
            int newValue = Math.max(0, currentReserved - count);

            if (reservedSeats.compareAndSet(currentReserved, newValue)) {
                return;
            }
        }
    }

    public void cancel() {
        cancelled.set(true);
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public boolean updateCapacity(int newCapacity) {
        while (true) {
            int currentCapacity = maxCapacity.get();

            if (newCapacity < reservedSeats.get()) {
                return false;
            }

            if (maxCapacity.compareAndSet(currentCapacity, newCapacity)) {
                return true;
            }
        }
    }

    public int getReservedSeats() {
        return reservedSeats.get();
    }

    public int getAvailableSpots() {
        return maxCapacity.get() - reservedSeats.get();
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public double getPrice() {
        return price;
    }

    public int getMaxCapacity() {
        return maxCapacity.get();
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public boolean requiresGuide() {
        return requiresGuide;
    }
}