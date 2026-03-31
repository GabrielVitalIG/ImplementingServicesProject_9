import config.Configuration;
import concurrency.BookingTask;
import domain.service.BookingService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SimulationRunner {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Initializing Urban Tourism Platform Simulation...");

        ExecutorService executor = Executors.newFixedThreadPool(Configuration.THREAD_POOL_SIZE);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(Configuration.TOTAL_TOURISTS);

        BookingService bookingService = new BookingService();

        System.out.println("Launching " + Configuration.TOTAL_TOURISTS + " tourists across multiple sessions...");

        for (int i = 0; i < Configuration.TOTAL_TOURISTS; i++) {
            executor.submit(new BookingTask(i, bookingService, startGate, endGate));
        }

        long startTime = System.currentTimeMillis();

        Thread cleanupThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    bookingService.cleanupExpiredReservations();
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        cleanupThread.start();
        startGate.countDown();

        Thread organizerThread = new Thread(() -> {
            try {
                Thread.sleep(200);
                bookingService.cancelRandomSession("Cancelled due to bad weather");

                Thread.sleep(200);
                bookingService.updateRandomSessionCapacity("Capacity adjusted due to operational constraints");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        organizerThread.start();

        endGate.await(2, TimeUnit.MINUTES);

        cleanupThread.interrupt();
        cleanupThread.join();

        long duration = System.currentTimeMillis() - startTime;

        System.out.println("--- Simulation Complete ---");
        System.out.println("Total Time: " + duration + " ms");

        bookingService.cleanupExpiredReservations();
        bookingService.printFinancialReport();

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
    }
}