package patterns;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class OrganizerSubject {
    private final List<ServiceObserver> observers = new CopyOnWriteArrayList<>();   // Thread-safe list for observers

    // Method to add an observer to the list
    public void addObserver(ServiceObserver observer) {
        observers.add(observer);
    }

    // Method to remove an observer from the list
    public void notifyUpdate(int sessionId, String reason, boolean cancel) {
        for (ServiceObserver observer : observers) {
            observer.onServiceUpdate(sessionId, reason, cancel);
        }
    }
}