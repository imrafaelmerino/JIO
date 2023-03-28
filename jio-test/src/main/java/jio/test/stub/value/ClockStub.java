package jio.test.stub.value;

import jio.time.Clock;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Class to create different kind of stubs that stand in for {@link Clock clocks}
 */
public final class ClockStub implements Supplier<Clock> {

    private final long lastTick;
    private volatile int counter;

    private final Function<Integer, Long> tickCounter;

    /**
     * Static factory method to create a clock stub from a reference time.
     * @param reference the instant from witch the clock start ticking
     * @return a clock stub
     */
    public static ClockStub fromReference(final Instant reference) {
        return new ClockStub(Objects.requireNonNull(reference));
    }

    /**
     * Static factory method to create a clock stub from a function that takes the calls counter and
     * returns a long representing the time.
     * @param callsFn function that takes the call number and returns the time
     * @return a clock stub
     */
    public static ClockStub fromCalls(final Function<Integer, Long> callsFn) {
        return new ClockStub(Objects.requireNonNull(callsFn));
    }

    private ClockStub(final Instant base) {
        Objects.requireNonNull(base);
        lastTick = System.nanoTime();
        tickCounter = n -> n == 1 ?
                base.toEpochMilli() :
                base.plus(Duration.ofNanos(System.nanoTime() - lastTick))
                    .toEpochMilli();
        clock = Clock.custom.apply(() -> {
            synchronized (this) {
                counter += 1;
                return tickCounter.apply(counter);
            }
        });
    }

    Clock clock;

    private ClockStub(final Function<Integer, Long> tickCounter) {
        lastTick = System.nanoTime();
        this.tickCounter = Objects.requireNonNull(tickCounter);
        clock = Clock.custom.apply(() -> {
            synchronized (this) {
                counter += 1;
                return tickCounter.apply(counter);
            }
        });
    }

    /**
     * returns a brand-new clock
     * @return a clock
     */
    @Override
    public Clock get() {
        return new ClockStub(tickCounter).clock;
    }
}
