package jio.time;

import jio.EventBuilder;

final class Monotonic implements Clock {
    @Override
    public Long get() {
        return System.nanoTime();
    }
}
