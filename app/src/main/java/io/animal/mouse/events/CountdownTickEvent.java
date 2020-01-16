package io.animal.mouse.events;

public class CountdownTickEvent {

    private final long milliseconds;

    public CountdownTickEvent(long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public long getMilliseconds() {
        return milliseconds;
    }
}
