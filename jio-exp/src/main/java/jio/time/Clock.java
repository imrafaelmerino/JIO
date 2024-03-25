package jio.time;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a clock, which is modeled with a lazy computation that returns a {@code long}. The returned {@code long}
 * output typically represents time or time-related information, depending on the specific clock type.
 */
public sealed interface Clock extends Supplier<Long> permits Monotonic, CustomClock, RealTime {

  /**
   * Creates a monotonic clock, appropriate for time measurements. When invoked, it returns the current output of the
   * running Java Virtual Machine's high-resolution time source, in nanoseconds. This {@code long} output represents a
   * time measurement. It uses the {@link System#nanoTime} method.
   *
   * @see System#nanoTime
   */
  Clock monotonic = new Monotonic();

  /**
   * Creates a real-time or wall-clock watch. It produces the current time, as a Unix timestamp in milliseconds (number
   * of time units since the Unix epoch). This {@code long} output represents a Unix timestamp. This clock is not
   * appropriate for measuring the duration of intervals (use {@link Clock#monotonic} instead). It uses the
   * {@link System#currentTimeMillis} method.
   *
   * @see System#currentTimeMillis
   */
  Clock realTime = new RealTime();

  /**
   * Function that takes a {@code long} supplier as the clock tick generator and returns a Clock. The provided
   * {@code long} values typically represent time or time-related information, depending on the specific clock type.
   */
  Function<Supplier<Long>, Clock> custom = CustomClock::new;

}
