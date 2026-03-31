package domain.model;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a guide who can be assigned to sessions.
 */
public class Guide {
    private final int id;                       // Unique identifier for the guide
    private final Set<String> busySchedule;     // Set of time slots when the guide is busy

    public Guide(int id) {
        this.id = id;
        this.busySchedule = ConcurrentHashMap.newKeySet();
    }

    // Checks if the guide is available for a given time slot
    public boolean isAvailable(String timeSlot) {
        return !busySchedule.contains(timeSlot);
    }

    // Books a time slot for the guide if they are available
    public boolean bookTimeSlot(String timeSlot) {
        return busySchedule.add(timeSlot);
    }

    // Releases a time slot, making the guide available again
    public void releaseTimeSlot(String timeSlot) {
        busySchedule.remove(timeSlot);
    }

    // Getters for guide properties
    public int getId() {
        return id;
    }

}