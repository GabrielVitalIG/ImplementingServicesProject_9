package domain.model;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a guide who can be assigned to sessions.
 */
public class Guide {
    private final int id;
    private final String name;
    private final Set<String> busySchedule;

    public Guide(int id, String name) {
        this.id = id;
        this.name = name;
        this.busySchedule = ConcurrentHashMap.newKeySet();
    }

    public boolean isAvailable(String timeSlot) {
        return !busySchedule.contains(timeSlot);
    }

    public boolean bookTimeSlot(String timeSlot) {
        return busySchedule.add(timeSlot);
    }

    public void releaseTimeSlot(String timeSlot) {
        busySchedule.remove(timeSlot);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}