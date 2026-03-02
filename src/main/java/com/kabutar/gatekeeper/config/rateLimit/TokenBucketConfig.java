package com.kabutar.gatekeeper.config.rateLimit;

public class TokenBucketConfig {
    private long capacity;
    private long refillRate;
    private String refillUnit;

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public long getRefillRate() {
        return refillRate;
    }

    public void setRefillRate(long refillRate) {
        this.refillRate = refillRate;
    }

    public String getRefillUnit() {
        return refillUnit;
    }

    public void setRefillUnit(String refillUnit) {
        this.refillUnit = refillUnit;
    }

    @Override
    public String toString() {
        return "TokenBucket{" +
                "capacity=" + capacity +
                ", refillRate=" + refillRate +
                ", refillUnit='" + refillUnit + '\'' +
                '}';
    }
}
