package com.kabutar.gatekeeper.config.rateLimit;

public class LeakyBucketConfig{
    private int capacity;
    private int outFlowPerSec;

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getOutFlowPerSec() {
        return outFlowPerSec;
    }

    public void setOutFlowPerSec(int outFlowPerSec) {
        this.outFlowPerSec = outFlowPerSec;
    }

    @Override
    public String toString() {
        return "LeakyBucket{" +
                "capacity=" + capacity +
                ", outFlowPerSec=" + outFlowPerSec +
                '}';
    }
}
