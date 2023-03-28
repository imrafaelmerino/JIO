package jio.time;

import java.util.function.Supplier;

record MyClock(Supplier<Long> time) implements Clock {
    @Override
    public Long get() {
        return time.get();
    }
}