package com.kabutar.gatekeeper.config.rateLimit;

public class FixedWindowConfig {
    private int timeWindow;
    private String timeUnit;
    private int counter;

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

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    @Override
    public String toString() {
        return "FicedWindowConfig{" +
                "timeWindow=" + timeWindow +
                ", timeUnit='" + timeUnit + '\'' +
                ", counter=" + counter +
                '}';
    }
}
