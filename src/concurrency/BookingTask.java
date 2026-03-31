package concurrency;

import domain.service.BookingService;

import java.util.concurrent.CountDownLatch;

public class BookingTask implements Runnable {
    private final int touristId;                    // Tourist ID attempting to book
    private final BookingService bookingService;    // Reference to the booking service
    private final CountDownLatch startGate;         // Latch to synchronize the start of all threads
    private final CountDownLatch endGate;           // Latch to signal the completion of all threads

    public BookingTask(int touristId, BookingService service, CountDownLatch start, CountDownLatch end) {
        this.touristId = touristId;
        this.bookingService = service;
        this.startGate = start;
        this.endGate = end;
    }


    // The run method is executed by each thread. It waits for the start gate to open, attempts to book a tour, and then signals completion.
    @Override
    public void run() {
        try {
            startGate.await();
            bookingService.attemptBooking(touristId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            endGate.countDown();
        }
    }
}