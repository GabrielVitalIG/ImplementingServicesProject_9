package patterns;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class OrganizerSubject {
    private final List<ServiceObserver> observers = new CopyOnWriteArrayList<>();

    public void addObserver(ServiceObserver observer) {
        observers.add(observer);
    }

    public void notifyUpdate(int sessionId, String reason, boolean cancel) {
        for (ServiceObserver observer : observers) {
            observer.onServiceUpdate(sessionId, reason, cancel);
        }
    }
}