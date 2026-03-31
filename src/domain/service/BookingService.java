package domain.service;

import domain.model.*;
import patterns.*;
import logger.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BookingService {
    private final ResourceScheduler scheduler = ResourceScheduler.getInstance();
    private final SalesLog salesLog = new SalesLog();

    // Mock data for simulation
    private final Session exampleSession = new Session(1, "Urban Exploration", 50, 2000);

    public boolean attemptBooking(int touristId) {
        // 1. Check and decrement capacity atomically [cite: 3, 9]
        if (exampleSession.reserveSpot()) {

            // 2. Coordinate guide assignment [cite: 3, 8]
            // We use a specific time slot to check for conflicts [cite: 4]
            Guide assignedGuide = scheduler.assignGuideToSession(exampleSession.getId(), "10:00 AM");

            if (assignedGuide != null) {
                // 3. Validate payment/completion and log [cite: 3, 11]
                salesLog.addEntry("Tourist " + touristId + " booked " + exampleSession.getTitle()
                        + " with Guide " + assignedGuide.getId());
                return true;
            } else {
                // Rollback capacity if no guide is available [cite: 7]
                exampleSession.releaseSpot();
            }
        }
        return false;
    }

    public void printFinancialReport() {
        System.out.println("Total Successful Bookings: " + salesLog.getLogCount());
    }
}