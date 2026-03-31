package simulation;

/**
 * Global settings for the urban tourism platform simulation.
 */
public class Configuration {
    // Total number of simultaneous requests
    public static final int TOTAL_TOURISTS = 100_000;

    // Number of available guides (Shared Resources) [cite: 6]
    public static final int GUIDE_COUNT = 500;

    // Number of available tour sessions [cite: 9]
    public static final int SESSION_COUNT = 50;

    // Expiration time for the Reservation Cart (in milliseconds)
    public static final long CART_EXPIRATION_MS = 30_000;

    // Thread pool size - adjusted for local simulation performance
    public static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
}
