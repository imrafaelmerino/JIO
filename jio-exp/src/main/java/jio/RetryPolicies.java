package jio;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Class with factory methods to create different retry policies
 */
public final class RetryPolicies {

  private RetryPolicies() {
  }

  /**
   * Returns a policy that retries up to N times, with no delay between retries.
   *
   * @param maxAttempts number of attempts
   * @return a policy that retries up to N times, with no delay between retries
   * @throws IllegalArgumentException if maxAttempts is less than or equal to 0
   */
  public static RetryPolicy limitRetries(int maxAttempts) {
    if (maxAttempts <= 0) {
      throw new IllegalArgumentException("maxAttempts <= 0");
    }
    return new LimitRetries(maxAttempts);
  }

  /**
   * Creates a policy that increments the delay between retries by the specified base duration.
   *
   * @param base the time increment between delays
   * @return a policy that increments the delay between retries by the specified base duration
   * @throws NullPointerException if base is null
   */
  public static RetryPolicy incrementalDelay(final Duration base) {
    return new IncrementalDelay(requireNonNull(base));
  }

  /**
   * Creates a policy that retries forever, with a fixed delay between retries.
   *
   * @param delay the fixed delay duration between retries
   * @return a policy that retries forever, with a fixed delay between retries
   * @throws NullPointerException if delay is null
   */
  public static RetryPolicy constantDelay(final Duration delay) {
    Objects.requireNonNull(delay);
    return rs -> delay;
  }

  /**
   * Creates a policy that doubles the delay after each retry.
   *
   * <p>The retry delay is calculated as:
   * <pre>
   * {@code
   * delay = 2 * base * attempt
   * }
   * </pre>
   *
   * @param base the base amount of time between retries
   * @return a policy that doubles the delay after each retry
   * @throws NullPointerException if base is null
   */
  public static RetryPolicy exponentialBackoffDelay(final Duration base) {
    return new ExponentialBackoffDelay(Objects.requireNonNull(base));
  }

  /**
   * Creates a policy that adds some jitter to the backoff sequence, spreading out the spikes to achieve an
   * approximately constant rate of retries.
   *
   * <p>The retry delay is calculated as follows:
   * <pre>
   * {@code
   * delay = random_between(0, min(cap, base * 2 * attempt))
   * }
   * </pre>
   *
   * @param base the base amount of time between retries
   * @param cap  the maximum upper bound for the retry delay
   * @return a policy that adds jitter to the backoff sequence
   * @throws NullPointerException if base or cap is null
   */
  public static RetryPolicy fullJitter(final Duration base,
                                       final Duration cap
  ) {
    Objects.requireNonNull(base);
    Objects.requireNonNull(cap);

    return rs -> {
      Duration delay = exponentialBackoffDelay(base).capDelay(cap)
                                                    .apply(rs);
      if (delay != null) {
        return Duration.ofMillis(ThreadLocalRandom.current()
                                                  .nextLong(delay.toMillis()));
      }
      return null;
    };
  }

  /**
   * Creates a policy that introduces jitter to the backoff sequence, resulting in retries that are spread out more
   * evenly. The retry delay is calculated as follows:
   *
   * <pre>
   * {@code
   * temp = min(cap, base * 2^attempt)
   * delay = temp/2 + random_between(0, temp/2)
   * }
   * </pre>
   *
   * @param base the base amount of time between retries
   * @param cap  the maximum upper bound for the retry delay
   * @return a policy that introduces jitter to the backoff sequence
   * @throws NullPointerException if base or cap is null
   */
  public static RetryPolicy equalJitter(final Duration base,
                                        final Duration cap
  ) {
    Objects.requireNonNull(base);
    Objects.requireNonNull(cap);
    return rs -> {
      Duration delay = exponentialBackoffDelay(base
      ).capDelay(cap)
       .apply(rs);
      return delay != null ? Duration.ofMillis(ThreadLocalRandom.current()
                                                                .nextLong(delay.dividedBy(2)
                                                                               .toMillis()
                                                                )
      ) : null;

    };

  }

  /**
   * Creates a policy that adds some jitter to spread out the retries, resulting in an approximately constant rate.
   *
   * <p>The retry delay is calculated according to the following formula:
   * <pre>
   * {@code
   * delay = min(cap, random_between(base, delay * 3))
   * }
   * </pre>
   *
   * @param base the base amount of time between retries
   * @param cap  the maximum upper bound for retry delay
   * @return a retry policy that adds jitter to the backoff
   * @throws NullPointerException if base or cap is null
   */
  public static RetryPolicy decorrelatedJitter(final Duration base,
                                               final Duration cap
  ) {
    Objects.requireNonNull(base);
    Objects.requireNonNull(cap);
    return rs -> {
      if (rs.cumulativeDelay()
            .isZero()) {
        return base;
      }

      Duration upperBound = rs.previousDelay()
                              .multipliedBy(3);
      long l = ThreadLocalRandom.current()
                                .nextLong(base.toMillis(),
                                          upperBound.toMillis()
                                );
      return l < cap.toMillis() ? Duration.ofMillis(l) : cap;

    };

  }

  record LimitRetries(int maxAttempts) implements RetryPolicy {

    @Override
    public Duration apply(final RetryStatus retryStatus) {
      boolean retry = retryStatus.counter() < maxAttempts;
      return retry ? Duration.ZERO : null;
    }
  }

  record IncrementalDelay(Duration base) implements RetryPolicy {

    @Override
    public Duration apply(final RetryStatus retryStatus) {
      return base.multipliedBy(retryStatus.counter() + 1);
    }
  }

  record ExponentialBackoffDelay(Duration base) implements RetryPolicy {

    @Override
    public Duration apply(final RetryStatus rs) {
      int multiplicand = (int) Math.pow(2,
                                        rs.counter()
      );
      return rs.cumulativeDelay()
               .isZero() ? base : base.multipliedBy(multiplicand);
    }
  }

}
