package domain.model;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a bookable activity with a strictly limited capacity.
 */
public class Session {
    private final int id;                           // Unique identifier for the session
    private final String title;                     // Name of the session
    private final double price;                     // Cost of attending the session
    private final AtomicInteger maxCapacity;        // Maximum number of participants allowed
    private final String timeSlot;                  // Scheduled time for the session
    private final boolean requiresGuide;            // Indicates if the session requires a guide

    private final AtomicInteger reservedSeats;      // Tracks the number of currently reserved seats
    private final AtomicBoolean cancelled;          // Indicates if the session has been cancelled

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


    /**
     * Attempts to reserve a specified number of spots for the session.
     * @param count The number of spots to reserve.
     * @return true if the reservation was successful,
     * false if the session is cancelled or if there are not enough available spots.
     */
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


    /**
     * Releases a specified number of reserved spots, making them available again.
     * @param count The number of spots to release. The method ensures that the number of reserved seats does not go below zero.
     */
    public void releaseSpots(int count) {
        while (true) {
            int currentReserved = reservedSeats.get();
            int newValue = Math.max(0, currentReserved - count);

            if (reservedSeats.compareAndSet(currentReserved, newValue)) {
                return;
            }
        }
    }

    // Cancels the session, preventing any further reservations.
    public void cancel() {
        cancelled.set(true);
    }

    // Checks if the session has been cancelled.
    public boolean isCancelled() {
        return cancelled.get();
    }


    /**
     * Updates the maximum capacity of the session. The new capacity must be greater than or equal to the number of currently reserved seats.
     * @param newCapacity The new maximum capacity for the session.
     * @return true if the capacity was successfully updated, false if the new capacity
     * is less than the number of reserved seats.
     */
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


    // Getters for session properties
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