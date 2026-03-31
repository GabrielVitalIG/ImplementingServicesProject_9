package observerLocalOrganizer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class OrganizerSubject {
    private final List<ServiceObserver> observers = new CopyOnWriteArrayList<>();   // Thread-safe list for observers

    // Method to add an observer to the list
    public void addObserver(ServiceObserver observer) {
        observers.add(observer);
    }

    public void  removeObserver(ServiceObserver observer) {
        observers.remove(observer);
    }

    public void notifyUpdate(int sessionId, String reason, boolean cancel) {
        for (ServiceObserver observer : observers) {
            observer.onServiceUpdate(sessionId, reason, cancel);
        }
    }
}