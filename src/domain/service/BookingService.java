package domain.service;

import concurrency.ExpirableCart;
import domain.model.Guide;
import domain.model.Reservation;
import domain.model.Session;
import logger.SalesLog;
import patterns.ConsoleServiceObserver;
import patterns.OrganizerSubject;
import patterns.ResourceScheduler;
import simulation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class BookingService {
    private final ResourceScheduler scheduler = ResourceScheduler.getInstance();
    private final SalesLog salesLog = new SalesLog();
    private final PaymentService paymentService = new PaymentService();
    private final ExpirableCart expirableCart = new ExpirableCart();

    private final OrganizerSubject organizerSubject = new OrganizerSubject();
    private final Map<Integer, Session> sessions = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<Reservation> reservations = new ConcurrentLinkedQueue<>();

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
        initializeSessions();
    }

    private void initializeSessions() {
        sessions.put(1, new Session(1, "Urban Exploration", 50.0, 2000, "10:00 AM", true));
        sessions.put(2, new Session(2, "Museum Ticket", 25.0, 1500, "11:00 AM", false));
        sessions.put(3, new Session(3, "Bus Tour", 40.0, 1000, "10:00 AM", true));
        sessions.put(4, new Session(4, "Historic Center Guided Tour", 35.0, 800, "02:00 PM", true));
        sessions.put(5, new Session(5, "Night City Walk", 30.0, 600, "08:00 PM", true));
    }

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

    public void cancelRandomSession(String reason) {
        int sessionId = ThreadLocalRandom.current().nextInt(1, sessions.size() + 1);
        OrganizerService organizerService = new OrganizerService(sessions, organizerSubject);
        organizerService.cancelSession(sessionId, reason);
    }

    public void updateRandomSessionCapacity(String reason) {
        int sessionId = ThreadLocalRandom.current().nextInt(1, sessions.size() + 1);
        int newCapacity = ThreadLocalRandom.current().nextInt(500, 2501);

        OrganizerService organizerService = new OrganizerService(sessions, organizerSubject);
        boolean updated = organizerService.updateSessionCapacity(sessionId, newCapacity, reason);

        if (updated) {
            organizerCapacityUpdates.incrementAndGet();
        }
    }

    public void cleanupExpiredReservations() {
        expirableCart.cleanupExpired();
    }

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