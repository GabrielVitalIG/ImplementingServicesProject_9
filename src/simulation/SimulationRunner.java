package simulation;

import concurrency.BookingTask;
import domain.service.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SimulationRunner {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Initializing Urban Tourism Platform Simulation...");

        // Setup the threading infrastructure
        ExecutorService executor = Executors.newFixedThreadPool(Configuration.THREAD_POOL_SIZE);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(Configuration.TOTAL_TOURISTS);

        // Initialize the centralized booking service
        BookingService bookingService = new BookingService();

        System.out.println("Launching 100,000 tourists...");

        for (int i = 0; i < Configuration.TOTAL_TOURISTS; i++) {
            int touristId = i;
            executor.submit(new BookingTask(touristId, bookingService, startGate, endGate));
        }

        long startTime = System.currentTimeMillis();

        // Release all threads at once to simulate high concurrency
        startGate.countDown();

        // Wait for all tourists to finish their attempts
        endGate.await(1, TimeUnit.MINUTES);

        long duration = System.currentTimeMillis() - startTime;

        System.out.println("--- Simulation Complete ---");
        System.out.println("Total Time: " + duration + "ms");

        // Final report from the Sales Log [cite: 11]
        bookingService.printFinancialReport();

        executor.shutdown();
    }
}
