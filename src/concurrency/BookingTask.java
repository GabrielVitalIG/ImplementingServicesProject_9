package concurrency;

import domain.service.BookingService;

import java.util.concurrent.CountDownLatch;

public class BookingTask implements Runnable {
    private final int touristId;
    private final BookingService bookingService;
    private final CountDownLatch startGate;
    private final CountDownLatch endGate;

    public BookingTask(int touristId, BookingService service, CountDownLatch start, CountDownLatch end) {
        this.touristId = touristId;
        this.bookingService = service;
        this.startGate = start;
        this.endGate = end;
    }

    @Override
    public void run() {
        try {
            // Wait for the signal to start the simulation
            startGate.await();

            // Attempt to browse and book
            boolean success = bookingService.attemptBooking(touristId);

            if (success) {
                // Optional: verbose logging for small tests
                // System.out.println("Tourist " + touristId + " secured a spot!");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // Signal that this tourist thread has finished its attempt
            endGate.countDown();
        }
    }
}
