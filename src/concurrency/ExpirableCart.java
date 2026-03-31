package concurrency;

import domain.model.Session;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ExpirableCart {
    private final DelayQueue<CartItem> queue = new DelayQueue<>();                         // Queue to manage expiring reservations
    private final ConcurrentHashMap<String, CartReservation> activeReservations = new ConcurrentHashMap<>();    // Map to track active reservations
    private final AtomicInteger expiredReservations = new AtomicInteger(0);     // Counter for expired reservations

    /**
     * Adds a reservation to the cart with an expiration time.
     * @param touristId ID of the tourist making the reservation
     * @param session Session for which the reservation is being made
     * @param groupSize Number of spots reserved for the group
     * @param delayMs Time in milliseconds after which the reservation should expire if not confirmed
     * @return A unique reservation ID for the added reservation
     */
    public String addReservation(int touristId, Session session, int groupSize, long delayMs) {
        String reservationId = touristId + "-" + session.getId() + "-" + System.nanoTime();
        CartReservation reservation = new CartReservation(touristId, session, groupSize, delayMs);

        activeReservations.put(reservationId, reservation);
        queue.put(new CartItem(reservationId, reservation.getExpireAt()));

        return reservationId;
    }


    /**
     * Confirms a reservation, preventing it from expiring and releasing the reserved spots.
     * @param reservationId The unique ID of the reservation to confirm
     * @return true if the reservation was successfully confirmed, false if the reservation ID is invalid or the reservation has already expired
     */
    public boolean confirmReservation(String reservationId) {
        CartReservation reservation = activeReservations.get(reservationId);
        if (reservation == null) {
            return false;
        }

        reservation.confirm();
        activeReservations.remove(reservationId);
        return true;
    }


    /**
     * Removes a reservation from the cart, typically used when a reservation is confirmed or manually removed by the user.
     * @param reservationId The unique ID of the reservation to remove
     */
    public void removeReservation(String reservationId) {
        activeReservations.remove(reservationId);
    }


    /**
     * Cleans up expired reservations by polling the DelayQueue. For each expired reservation, it releases the reserved spots and increments the expired reservations counter.
     */
    public void cleanupExpired() {
        CartItem item;

        while ((item = queue.poll()) != null) {
            CartReservation reservation = activeReservations.remove(item.reservationId);

            if (reservation != null && !reservation.isConfirmed()) {
                reservation.getSession().releaseSpots(reservation.getGroupSize());
                expiredReservations.incrementAndGet();

                System.out.println(
                        "Expired: Released " + reservation.getGroupSize()
                                + " spots for " + reservation.getSession().getTitle()
                                + " (tourist " + reservation.getTouristId() + ")"
                );
            }
        }
    }

    /**
     * Returns the number of reservations that have expired and been cleaned up.
     * @return The count of expired reservations
     */
    public int getExpiredReservationsCount() {
        return expiredReservations.get();
    }


    /**
     * Inner class representing a reservation in the cart, implementing the Delayed interface to be used in the DelayQueue. Each CartItem has a unique reservation ID and an expiration time.
     */
    private static class CartItem implements Delayed {
        private final String reservationId;     // Unique ID for the reservation
        private final long expireTime;          // Time at which the reservation expires (in milliseconds since epoch)

        public CartItem(String reservationId, long expireTime) {
            this.reservationId = reservationId;
            this.expireTime = expireTime;
        }


        /**
         * Calculates the remaining delay for this CartItem based on the current time and the expiration time. The delay is returned in the specified time unit.
         * @param unit the time unit
         * @return the remaining delay for this CartItem in the specified time unit
         */
        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(expireTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }


        /**
         * Compares this CartItem with another Delayed object based on their expiration times. This method is used by the DelayQueue to order the items by their expiration time.
         * @param other the object to be compared.
         * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
         */
        @Override
        public int compareTo(Delayed other) {
            CartItem otherItem = (CartItem) other;
            return Long.compare(this.expireTime, otherItem.expireTime);
        }
    }
}