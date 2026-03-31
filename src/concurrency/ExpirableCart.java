package concurrency;

import domain.model.Session;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ExpirableCart {
    private final DelayQueue<CartItem> queue = new DelayQueue<>();
    private final ConcurrentHashMap<String, CartReservation> activeReservations = new ConcurrentHashMap<>();
    private final AtomicInteger expiredReservations = new AtomicInteger(0);

    public String addReservation(int touristId, Session session, int groupSize, long delayMs) {
        String reservationId = touristId + "-" + session.getId() + "-" + System.nanoTime();
        CartReservation reservation = new CartReservation(touristId, session, groupSize, delayMs);

        activeReservations.put(reservationId, reservation);
        queue.put(new CartItem(reservationId, reservation.getExpireAt()));

        return reservationId;
    }

    public boolean confirmReservation(String reservationId) {
        CartReservation reservation = activeReservations.get(reservationId);
        if (reservation == null) {
            return false;
        }

        reservation.confirm();
        activeReservations.remove(reservationId);
        return true;
    }

    public void removeReservation(String reservationId) {
        activeReservations.remove(reservationId);
    }

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

    public int getExpiredReservationsCount() {
        return expiredReservations.get();
    }

    private static class CartItem implements Delayed {
        private final String reservationId;
        private final long expireTime;

        public CartItem(String reservationId, long expireTime) {
            this.reservationId = reservationId;
            this.expireTime = expireTime;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(expireTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed other) {
            CartItem otherItem = (CartItem) other;
            return Long.compare(this.expireTime, otherItem.expireTime);
        }
    }
}