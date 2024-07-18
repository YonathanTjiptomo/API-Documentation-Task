package org.example;

import java.util.concurrent.TimeUnit;

public class TokenBucket {
    private final long capacity;
    private final long tokensPerInterval;
    private final long intervalInMillis;
    private long availableTokens;
    private long lastRefillTimestamp;

    public TokenBucket(long capacity, long tokensPerInterval, long interval, TimeUnit unit) {
        this.capacity = capacity;
        this.tokensPerInterval = tokensPerInterval;
        this.intervalInMillis = unit.toMillis(interval);
        this.availableTokens = capacity;
        this.lastRefillTimestamp = System.currentTimeMillis();
    }

    public synchronized boolean tryConsume() {
        refill();

        if (availableTokens > 0) {
            availableTokens--;
            return true;
        } else {
            return false;
        }
    }

    private void refill() {
        long now = System.currentTimeMillis();
        long elapsedTime = now - lastRefillTimestamp;

        if (elapsedTime > intervalInMillis) {
            long tokensToAdd = (elapsedTime / intervalInMillis) * tokensPerInterval;
            availableTokens = Math.min(capacity, availableTokens + tokensToAdd);
            lastRefillTimestamp = now;
        }
    }
}
