package patterns;

import domain.model.Guide;
import simulation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ResourceScheduler {
    private volatile static ResourceScheduler instance;                                     // Singleton instance
    private final List<Guide> guides;                                                       // List of guides available for assignment
    private final Map<Integer, ReentrantLock> guideLocks = new ConcurrentHashMap<>();       // Locks for each guide to ensure thread-safe access

    private ResourceScheduler() {
        this.guides = new ArrayList<>();
        for (int i = 1; i <= Configuration.GUIDE_COUNT; i++) {
            guides.add(new Guide(i));
            guideLocks.put(i, new ReentrantLock());
        }
    }

    /**
     * Provides a thread-safe way to get the singleton instance of ResourceScheduler.
     * @return the singleton instance of ResourceScheduler
     */
    public static ResourceScheduler getInstance() {
        if (instance == null) {
            synchronized (ResourceScheduler.class) {
                if (instance == null) {
                    instance = new ResourceScheduler();
                }
            }
        }
        return instance;
    }


    /**
     * Assigns an available guide to a session based on the provided session ID and time slot.
     * @param sessionId the ID of the session for which a guide is being assigned
     * @param timeSlot the time slot for which the guide needs to be assigned
     * @return the assigned Guide object if a guide is available, or null if no guide is available for the given time slot
     */
    public Guide assignGuideToSession(int sessionId, String timeSlot) {
        for (Guide guide : guides) {
            ReentrantLock lock = guideLocks.get(guide.getId());

            if (lock.tryLock()) {
                try {
                    if (guide.isAvailable(timeSlot)) {
                        guide.bookTimeSlot(timeSlot);
                        return guide;
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
        return null;
    }


    /**
     * Releases a guide from a session for a specific time slot, making the guide available for other sessions.
     * @param guide the Guide object that is being released from the session
     * @param timeSlot the time slot for which the guide is being released
     */
    public void releaseGuide(Guide guide, String timeSlot) {
        if (guide == null) {
            return;
        }

        ReentrantLock lock = guideLocks.get(guide.getId());
        lock.lock();
        try {
            guide.releaseTimeSlot(timeSlot);
        } finally {
            lock.unlock();
        }
    }
}