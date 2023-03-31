package jio.time;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a clock, which is modeled with a lazy computation that returns a long.
 */
public sealed interface Clock extends Supplier<Long> permits Monotonic, MyClock, RealTime {

    /**
     * Creates a monotonic clock, appropriate for time measurements.
     * When invoked, it returns the current value of the running Java
     * Virtual Machine's high-resolution time source, in nanoseconds
     * It uses the {@link System#nanoTime } method.
     *
     * @see System#nanoTime
     */
    Clock monotonic = new Monotonic();

    /**
     * Creates a realtime or wall-clock watch. It produces the current time,
     * as a Unix timestamp in milliseconds (number of time units since the Unix epoch).
     * This clock is not appropriate for measuring duration of intervals
     * ( use {@link Clock#monotonic} instead ).
     * It uses the {@link System#currentTimeMillis } method.
     *
     * @see System#currentTimeMillis
     */
    Clock realTime = new RealTime();

    /**
     *
     * Function that takes a long supplier as the clock tick generator and returns a Clock.
     *
     */
    Function<Supplier<Long>, Clock> custom = MyClock::new;


}
