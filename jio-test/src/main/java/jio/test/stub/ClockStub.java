package jio.test.stub;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Function;
import jio.time.Clock;

/**
 * Class to create different kinds of stubs that stand in for {@link Clock clocks}. These clock stubs are useful for
 * controlling time-related behavior in your applications during testing and development.
 */
public final class ClockStub {

  private final long lastTick;
  private final Function<Integer, Long> tickCounter;
  final Clock clock;
  private volatile int counter;

  private ClockStub(final Instant base) {
    Objects.requireNonNull(base);
    lastTick = System.nanoTime();
    tickCounter = n -> n == 1 ? base.toEpochMilli() : base.plus(Duration.ofNanos(System.nanoTime() - lastTick))
                                                          .toEpochMilli();
    clock = Clock.custom.apply(() -> {
      synchronized (this) {
        counter += 1;
        return tickCounter.apply(counter);
      }
    });
  }

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
   * Static factory method to create a clock stub from a reference time.
   * <p>
   * This method creates a clock stub that starts ticking from the provided reference time. You can use this to simulate
   * time-based scenarios where you want the clock to behave as if it started at a specific instant.
   * </p>
   *
   * @param reference The instant from which the clock starts ticking.
   * @return A clock stub.
   */
  public static Clock fromReference(final Instant reference) {
    return new ClockStub(Objects.requireNonNull(reference)).clock;
  }

  /**
   * Static factory method to create a clock stub from a function that takes the call's counter and returns a long
   * representing the time.
   * <p>
   * This method allows you to create a clock stub where you can control the ticking time based on the number of calls
   * made to the clock. You can use this for more dynamic time simulation.
   * </p>
   * <pre>
   * {@code
   *
   * Function<Integer, Long> timeFunction = n -> {
   *     // Simulate time progressing by 1 hour with each call
   *     return Instant.now().plus(Duration.ofHours(n)).toEpochMilli();
   * };
   * Clock dynamicClock = ClockStub.fromCalls(timeFunction);
   *
   * }
   * </pre>
   *
   * @param callsFn Function that takes the call number and returns the time.
   * @return A clock stub.
   */
  public static Clock fromSeqCalls(final Function<Integer, Long> callsFn) {
    return new ClockStub(Objects.requireNonNull(callsFn)).clock;
  }

}
