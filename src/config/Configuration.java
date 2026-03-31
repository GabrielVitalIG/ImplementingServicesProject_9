package config;

/**
 * Global settings for the urban tourism platform simulation.
 */
public class Configuration {
    public static final int TOTAL_TOURISTS = 100_000;
    public static final int GUIDE_COUNT = 500;
    public static final long CART_EXPIRATION_MS = 300;
    public static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
}