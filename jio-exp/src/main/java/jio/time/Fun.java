package jio.time;

import java.time.Duration;
import java.util.Objects;

/**
 * Class with handy functions related to time
 */
public final class Fun {

  private Fun() {
  }

  /**
   * Formats a given time duration into a human-readable string.
   *
   * @param duration The duration
   * @return A formatted string representing the time duration.
   */
  public static String formatTime(Duration duration) {
    return formatTime(Objects.requireNonNull(duration)
                             .toNanos());
  }

  /**
   * Formats a given time duration in nanoseconds into a human-readable string.
   *
   * @param time The time in nanoseconds.
   * @return A formatted string representing the time duration.
   */
  public static String formatTime(long time) {
    if (time < 0) {
      throw new IllegalArgumentException("time < 0");
    }
    if (time >= 1000_000_000) {
      return "%.3f sg".formatted(time / 1000_000_000d);
    }
    if (time >= 1000_000) {
      return "%.3f ms".formatted(time / 1000_000d);
    }
    if (time >= 1000) {
      return "%.3f µs".formatted(time / 1000d);
    }
    return "%d ns".formatted(time);
  }
}
