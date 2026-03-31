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
            startGate.await();
            bookingService.attemptBooking(touristId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            endGate.countDown();
        }
    }
}