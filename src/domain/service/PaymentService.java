package domain.service;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Simulates payment validation.
 */
public class PaymentService {

    /**
     * Simulates payment validation for a tourist. In this example, it randomly returns true 90% of the time to indicate a successful payment.
     * @param touristId the ID of the tourist making the payment
     * @param amount the amount to be paid
     * @return true if the payment is successful, false otherwise
     */
    public boolean validatePayment(int touristId, double amount) {
        return ThreadLocalRandom.current().nextInt(100) < 90;
    }
}