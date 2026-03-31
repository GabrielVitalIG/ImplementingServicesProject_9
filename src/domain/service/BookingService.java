package domain.service;

import concurrency.ExpirableCart;
import domain.model.Guide;
import domain.model.Reservation;
import domain.model.Session;
import logger.SalesLog;
import observerLocalOrganizer.ConsoleServiceObserver;
import observerLocalOrganizer.OrganizerSubject;
import SingletonResourceScheduler.ResourceScheduler;
import config.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class BookingService {
    private final ResourceScheduler scheduler = ResourceScheduler.getInstance();                        // Singleton scheduler for guide management
    private final SalesLog salesLog = new SalesLog();                                                   // Centralized sales log for recording all transactions
    private final PaymentService paymentService = new PaymentService();                                 // Service for validating payments
    private final ExpirableCart expirableCart = new ExpirableCart();                                    // Cart for managing temporary reservations with expiration

    private final OrganizerSubject organizerSubject = new OrganizerSubject();                           // Subject for notifying observers about session cancellations and capacity updates
    private final OrganizerService organizerService;
    private final Map<Integer, Session> sessions = new ConcurrentHashMap<>();                           // Thread-safe map to store session information, allowing concurrent access and modifications
    private final ConcurrentLinkedQueue<Reservation> reservations = new ConcurrentLinkedQueue<>();      // Thread-safe queue to store confirmed reservations, allowing concurrent additions without locking

    private final AtomicInteger totalRequests = new AtomicInteger(0);
    private final AtomicInteger successfulBookings = new AtomicInteger(0);
    private final AtomicInteger failedCapacity = new AtomicInteger(0);
    private final AtomicInteger failedGuide = new AtomicInteger(0);
    private final AtomicInteger failedPayment = new AtomicInteger(0);
    private final AtomicInteger failedCancelled = new AtomicInteger(0);
    private final AtomicInteger totalPeopleBooked = new AtomicInteger(0);
    private final AtomicInteger organizerCapacityUpdates = new AtomicInteger(0);


    public BookingService() {
        organizerSubject.addObserver(new ConsoleServiceObserver());
        organizerService = new OrganizerService(sessions, organizerSubject);
        initializeSessions();
    }

    /**
     * Initializes the sessions with predefined data. In a real application, this would likely come from a database or external service.
     */
    private void initializeSessions() {
        sessions.put(1, new Session(1, "Urban Exploration", 50.0, 2000, "10:00 AM", true));
        sessions.put(2, new Session(2, "Museum Ticket", 25.0, 1500, "11:00 AM", false));
        sessions.put(3, new Session(3, "Bus Tour", 40.0, 1000, "10:00 AM", true));
        sessions.put(4, new Session(4, "Historic Center Guided Tour", 35.0, 800, "02:00 PM", true));
        sessions.put(5, new Session(5, "Night City Walk", 30.0, 600, "08:00 PM", true));
    }


    /**
     * Attempts to book a session for a tourist. This method simulates the entire booking process,
     * including checking session availability, reserving spots, assigning guides if necessary, validating payment,
     * and confirming the reservation. It also handles various failure scenarios and updates the corresponding metrics.
     * @param touristId The ID of the tourist attempting to make a booking.
     * @return true if the booking was successful, false otherwise.
     */
    public boolean attemptBooking(int touristId) {
        totalRequests.incrementAndGet();

        int sessionId = ThreadLocalRandom.current().nextInt(1, sessions.size() + 1);
        int groupSize = ThreadLocalRandom.current().nextInt(1, 5);

        Session session = sessions.get(sessionId);
        if (session == null) {
            return false;
        }

        if (session.isCancelled()) {
            failedCancelled.incrementAndGet();
            return false;
        }

        if (!session.reserveSpots(groupSize)) {
            failedCapacity.incrementAndGet();
            return false;
        }

        String cartReservationId = expirableCart.addReservation(
                touristId,
                session,
                groupSize,
                Configuration.CART_EXPIRATION_MS
        );

        Guide assignedGuide = null;

        try {
            if (session.requiresGuide()) {
                assignedGuide = scheduler.assignGuideToSession(session.getId(), session.getTimeSlot());

                if (assignedGuide == null) {
                    session.releaseSpots(groupSize);
                    expirableCart.removeReservation(cartReservationId);
                    failedGuide.incrementAndGet();
                    return false;
                }
            }

            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(20, 80));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            boolean paymentOk = paymentService.validatePayment(touristId, session.getPrice() * groupSize);

            if (!paymentOk) {
                session.releaseSpots(groupSize);
                expirableCart.removeReservation(cartReservationId);

                if (assignedGuide != null) {
                    scheduler.releaseGuide(assignedGuide, session.getTimeSlot());
                }

                failedPayment.incrementAndGet();
                return false;
            }

            boolean confirmed = expirableCart.confirmReservation(cartReservationId);
            if (!confirmed) {
                if (assignedGuide != null) {
                    scheduler.releaseGuide(assignedGuide, session.getTimeSlot());
                }
                return false;
            }

            Reservation reservation = new Reservation(
                    touristId,
                    session.getId(),
                    assignedGuide != null ? assignedGuide.getId() : 0,
                    groupSize
            );

            reservations.add(reservation);
            successfulBookings.incrementAndGet();
            totalPeopleBooked.addAndGet(groupSize);

            salesLog.addEntry(
                    "Tourist " + touristId +
                    " booked " + session.getTitle() +
                    " for " + groupSize + " people" +
                    (assignedGuide != null ? " with Guide " + assignedGuide.getId() : " without guide")
            );

            return true;

        } catch (Exception e) {
            session.releaseSpots(groupSize);
            expirableCart.removeReservation(cartReservationId);

            if (assignedGuide != null) {
                scheduler.releaseGuide(assignedGuide, session.getTimeSlot());
            }

            return false;
        }
    }

    /**
     * Simulates the cancellation of a random session by an organizer. This method randomly selects a session and cancels it,
     * @param reason The reason for the cancellation, which will be logged and notified to observers.
     */
    public void cancelRandomSession(String reason) {
        int sessionId = ThreadLocalRandom.current().nextInt(1, sessions.size() + 1);
        organizerService.cancelSession(sessionId, reason);
    }


    /**
     * Simulates an organizer updating the capacity of a random session. This method randomly selects a session and updates its capacity,
     * @param reason The reason for the capacity update, which will be logged and notified to observers.
     */
    public void updateRandomSessionCapacity(String reason) {
        int sessionId = ThreadLocalRandom.current().nextInt(1, sessions.size() + 1);
        int newCapacity = ThreadLocalRandom.current().nextInt(500, 2501);

        boolean updated = organizerService.updateSessionCapacity(sessionId, newCapacity, reason);

        if (updated) {
            organizerCapacityUpdates.incrementAndGet();
        }
    }


    /// Cleans up expired reservations from the expirable cart. This method should be called periodically to ensure that expired reservations are removed and resources are freed up.
    public void cleanupExpiredReservations() {
        expirableCart.cleanupExpired();
    }

    /**
     * Prints a comprehensive financial report summarizing the booking activity, including total requests,
     * successful bookings, various failure reasons, total people booked, and details of each session.
     * This method is useful for analyzing the performance of the booking service and identifying areas for improvement.
     */
    public void printFinancialReport() {
        System.out.println("===== FINAL REPORT =====");
        System.out.println("Total requests: " + totalRequests.get());
        System.out.println("Successful bookings: " + successfulBookings.get());
        System.out.println("Failed - capacity reached: " + failedCapacity.get());
        System.out.println("Failed - no guide available: " + failedGuide.get());
        System.out.println("Failed - payment rejected: " + failedPayment.get());
        System.out.println("Failed - session cancelled: " + failedCancelled.get());
        System.out.println("Organizer capacity updates: " + organizerCapacityUpdates.get());
        System.out.println("Expired cart reservations: " + expirableCart.getExpiredReservationsCount());
        System.out.println("Total people booked: " + totalPeopleBooked.get());
        System.out.println("Stored reservations: " + reservations.size());
        System.out.println("Sales log entries: " + salesLog.getLogCount());

        System.out.println("----- Session Details -----");
        for (Session session : sessions.values()) {
            System.out.println(
                    "Session " + session.getId() +
                    " | " + session.getTitle() +
                    " | cancelled=" + session.isCancelled() +
                    " | reserved=" + session.getReservedSeats() +
                    "/" + session.getMaxCapacity() +
                    " | available=" + session.getAvailableSpots()
            );
        }
    }
}