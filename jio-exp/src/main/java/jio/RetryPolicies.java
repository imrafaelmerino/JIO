package jio;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Objects.requireNonNull;

/**
 * Class with factory methods to create different retry policies
 */
public final class RetryPolicies {
    private RetryPolicies() {
    }

    record LimitRetries(int maxAttempts) implements RetryPolicy {

        @Override
        public Optional<Duration> apply(final RetryStatus retryStatus) {
            boolean retry = retryStatus.counter() < maxAttempts;
            return retry ?
                    Optional.of(Duration.ZERO) :
                    Optional.empty();
        }
    }

    record IncrementalDelay(Duration base) implements RetryPolicy {

        @Override
        public Optional<Duration> apply(final RetryStatus retryStatus) {
            return Optional.of(base.multipliedBy(retryStatus.counter() + 1));
        }
    }

    record ExponentialBackoffDelay(Duration base) implements RetryPolicy {

        @Override
        public Optional<Duration> apply(final RetryStatus rs) {
            int multiplicand = (int) Math.pow(2,
                                              rs.counter()
                                             );
            return Optional.of(rs.cumulativeDelay().isZero() ?
                                       base :
                                       base.multipliedBy(multiplicand)
                              );
        }
    }

    /**
     * returns a policy that retries up to N times, with no delay between retries
     *
     * @param maxAttempts number of attempts
     * @return a policy that retries up to N times, with no delay between retries
     */
    public static RetryPolicy limitRetries(int maxAttempts) {
        if (maxAttempts <= 0) throw new IllegalArgumentException("maxAttempts <= 0");
        return new LimitRetries(maxAttempts);
    }

    /**
     * creates a policy that increments by the specified base the delay between retries
     *
     * @param base the time incremented between delays
     * @return a policy that increments by the base the delay between retries
     */
    public static RetryPolicy incrementalDelay(final Duration base) {
        return new IncrementalDelay(requireNonNull(base));
    }

    /**
     * creates a policy that retries forever, with a fixed delay between retries
     *
     * @param delay the fixed delay
     * @return a policy that retries forever, with a fixed delay between retries
     */
    public static RetryPolicy constantDelay(final Duration delay) {
        Objects.requireNonNull(delay);
        return rs -> Optional.of(delay);
    }

    /**
     * creates a policy that doubles the delay after each retry:
     * <pre>
     * {@code
     *     delay = 2 * base * attempt
     * }
     *</pre>
     * @param base the base amount of time
     * @return a policy that doubles the delay after each retry
     */
    public static RetryPolicy exponentialBackoffDelay(final Duration base) {
        return new ExponentialBackoffDelay(Objects.requireNonNull(base));
    }


    /**
     * creates a policy that adds some jitter to spread out the spikes to an approximately constant rate.
     * <pre>
     * {@code
     *    delay = random_between(0,min(cap, base * 2 * attempt))
     * }
     * </pre>
     *
     * @param base the base amount of time
     * @param cap  the max upper bound
     * @return a policy that adds some jitter to the backoff
     */
    public static RetryPolicy fullJitter(final Duration base,
                                         final Duration cap
                                        ) {
        Objects.requireNonNull(base);
        Objects.requireNonNull(cap);

        return rs -> exponentialBackoffDelay(base).capDelay(cap)
                                                  .apply(rs)
                                                  .map(t -> Duration.ofMillis(ThreadLocalRandom.current()
                                                                                               .nextLong(t.toMillis())));
    }

    /**
     * creates a policy where the retry delay is calculated according to the following the formula:
     *
     * <pre>
     * {@code
     *    temp = min(cap,base * 2 ^ attempt)
     *    delay = temp/2 + random_between(0,temp/2)
     * }
     * </pre>
     *
     * @param base the base
     * @param cap  the cap
     * @return a retry policy
     */
    public static RetryPolicy equalJitter(final Duration base,
                                          final Duration cap
                                         ) {
        Objects.requireNonNull(base);
        Objects.requireNonNull(cap);
        return rs -> exponentialBackoffDelay(base
                                            ).capDelay(cap)
                                             .apply(rs)
                                             .map(t -> Duration.ofMillis(ThreadLocalRandom.current()
                                                                                          .nextLong(t.dividedBy(2)
                                                                                                     .toMillis()
                                                                                                   )
                                                                        ));

    }

    /**
     * <pre>
     * {@code
     *     delay = min(cap,random_between(base, delay * 3))
     * }
     * </pre>
     *
     * @param base the base
     * @param cap  the cap
     * @return a retry policy
     */
    public static RetryPolicy decorrelatedJitter(final Duration base,
                                                 final Duration cap
                                                ) {
        Objects.requireNonNull(base);
        Objects.requireNonNull(cap);
        return rs -> {
            if (rs.cumulativeDelay().isZero()) return Optional.of(base);

            Duration upperBound = rs.previousDelay().multipliedBy(3);
            long l = ThreadLocalRandom.current()
                                      .nextLong(base.toMillis(),
                                                upperBound.toMillis()
                                               );
            return l < cap.toMillis() ? Optional.of(Duration.ofMillis(l)) : Optional.of(cap);

        };

    }

}
