package concurrency;

import domain.model.*;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class ExpirableCart {
    private final DelayQueue<CartItem> queue = new DelayQueue<>();

    public void addReservation(Session session, long delayMs) {
        queue.put(new CartItem(session, delayMs));
    }

    /**
     * A background cleanup process or the main service should call this
     * to release expired spots.
     */
    public void cleanupExpired() {
        CartItem item;
        while ((item = queue.poll()) != null) {
            item.session.releaseSpot();
            System.out.println("Expired: Spot released for " + item.session.getTitle());
        }
    }

    private static class CartItem implements Delayed {
        private final Session session;
        private final long expireTime;

        public CartItem(Session session, long delayMs) {
            this.session = session;
            this.expireTime = System.currentTimeMillis() + delayMs;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(expireTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            return Long.compare(this.expireTime, ((CartItem) o).expireTime);
        }
    }
}