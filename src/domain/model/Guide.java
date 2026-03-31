package domain.model;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a guide who can be assigned to sessions.
 */
public class Guide {
    private final int id;
    private final String name;
    // Stores busy time slots (e.g., "10:00 AM") to prevent double-booking [cite: 4, 9]
    private final Set<String> busySchedule;

    public Guide(int id, String name) {
        this.id = id;
        this.name = name;
        // Thread-safe set to handle concurrent schedule checks
        this.busySchedule = ConcurrentHashMap.newKeySet();
    }

    public boolean isAvailable(String timeSlot) {
        return !busySchedule.contains(timeSlot);
    }

    public boolean bookTimeSlot(String timeSlot) {
        // add() returns true if the element was not already present
        return busySchedule.add(timeSlot);
    }

    public int getId() { return id; }
    public String getName() { return name; }
}
