package com.kabutar.gatekeeper.config.rateLimit;


public class SlidingWindowConfig {
    private int counter;
    private int timeWindow;
    private String timeUnit;

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public int getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(int timeWindow) {
        this.timeWindow = timeWindow;
    }

    public String getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(String timeUnit) {
        this.timeUnit = timeUnit;
    }

    @Override
    public String toString() {
        return "SlidingWindowConfig{" +
                "counter=" + counter +
                ", timeWindow=" + timeWindow +
                ", timeUnit='" + timeUnit + '\'' +
                '}';
    }
}

