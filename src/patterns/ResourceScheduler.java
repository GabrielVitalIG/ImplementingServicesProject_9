package patterns;

import domain.model.*;
import simulation.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;
import java.util.List;

public class ResourceScheduler {
    private volatile static ResourceScheduler instance;
    private final List<Guide> guides;
    private final Map<Integer, ReentrantLock> guideLocks = new ConcurrentHashMap<>();

    private ResourceScheduler() {
        this.guides = new ArrayList<>();
        for (int i = 1; i <= Configuration.GUIDE_COUNT; i++) {
            guides.add(new Guide(i, "Guide-" + i));
            guideLocks.put(i, new ReentrantLock());
        }
    }

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

    public Guide assignGuideToSession(int sessionId, String timeSlot) {
        for (Guide guide : guides) {
            ReentrantLock lock = guideLocks.get(guide.getId());

            // tryLock prevents the "100k tourist" herd from deadlocking
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
        return null; // No guides available at this time
    }
}
