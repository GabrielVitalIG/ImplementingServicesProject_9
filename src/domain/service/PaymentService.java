package domain.service;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Simulates payment validation.
 */
public class PaymentService {

    // Example: 90% success rate
    public boolean validatePayment(int touristId, double amount) {
        return ThreadLocalRandom.current().nextInt(100) < 90;
    }
}