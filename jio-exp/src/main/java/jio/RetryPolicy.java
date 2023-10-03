package jio;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;


/**
 * A RetryPolicy is a function that takes a RetryStatus and possibly returns the duration of the delay to wait before
 * the next try.
 *
 * <p>Iteration numbers start at zero and increase by one on each retry. An {@code Optional.empty()} return value
 * from the function implies we have reached the retry limit.
 *
 * <p>You can collapse multiple strategies into one using the {@link #append(RetryPolicy) append} method. There are
 * also several predefined policies available in {@link RetryPolicies}. Additionally, you can use combinators like
 * {@link #capDelay(Duration)}, {@link #limitRetriesByDelay(Duration)}, and
 * {@link #limitRetriesByCumulativeDelay(Duration)} to transform policies.
 *
 * <p>Always simulate any policy you define with {@link #simulate(int)} to check it's behaved as expected.
 *
 * @see RetryPolicies
 */
public interface RetryPolicy extends Function<RetryStatus, Optional<Duration>> {

    /**
     * Combines this policy with another policy. If either policy (this or other) returns {@code Optional.empty()}, the
     * combined policy returns {@code Optional.empty()}. This can be used to inhibit retries after a certain number of
     * attempts. If both policies return a delay, the larger delay will be used.
     *
     * @param other the other retry policy to be appended
     * @return a new retry policy combining this and the other policy
     * @throws NullPointerException if the other policy is null
     */
    default RetryPolicy append(final RetryPolicy other) {
        Objects.requireNonNull(other);
        return retryStatus -> {
            Optional<Duration> aOpt = RetryPolicy.this.apply(retryStatus);
            if (aOpt.isEmpty()) return aOpt;
            Optional<Duration> bOpt = other.apply(retryStatus);
            if (bOpt.isEmpty()) return bOpt;
            return Optional.of(aOpt.get().compareTo(bOpt.get()) >= 0 ?
                                       aOpt.get() :
                                       bOpt.get()
                              );
        };
    }

    /**
     * Sequentially composes this policy with another policy. If this policy returns a delay, it will be used.
     * Otherwise, the other policy will be applied.
     *
     * @param other the other policy to be applied after this policy gives up
     * @return a new retry policy combining this policy followed by the other policy
     * @throws NullPointerException if the other policy is null
     */
    default RetryPolicy followedBy(final RetryPolicy other) {
        Objects.requireNonNull(other);
        return rs -> {
            Optional<Duration> delay = this.apply(rs);
            return delay.isEmpty() ? other.apply(rs) : delay;
        };
    }

    /**
     * Sets an upper bound on the delay between retries.
     *
     * @param cap the upper bound for retry delay
     * @return a new policy with the delay capped at the specified upper bound
     * @throws NullPointerException if the cap duration is null
     */
    default RetryPolicy capDelay(final Duration cap) {
        Objects.requireNonNull(cap);
        return rs -> {
            Optional<Duration> delay = this.apply(rs);
            if (delay.isEmpty()) return delay;
            return delay.get().compareTo(cap) >= 0 ?
                    Optional.of(cap) :
                    delay;
        };

    }

    /**
     * Gives up when the delay between retries reaches a certain limit.
     *
     * @param max the limit for retry delay
     * @return a new policy that gives up when the delay exceeds the specified limit
     * @throws NullPointerException if the max duration is null
     */
    default RetryPolicy limitRetriesByDelay(final Duration max) {
        Objects.requireNonNull(max);
        return rs -> {
            Optional<Duration> delay = this.apply(rs);
            if (delay.isEmpty()) return delay;
            return delay.get().compareTo(max) >= 0 ?
                    Optional.empty() :
                    delay;
        };
    }

    /**
     * Gives up when the total cumulative delay reaches a certain limit.
     *
     * @param max the limit for cumulative delay
     * @return a new policy that gives up when the cumulative delay exceeds the specified limit
     * @throws NullPointerException if the max duration is null
     */
    default RetryPolicy limitRetriesByCumulativeDelay(final Duration max) {
        Objects.requireNonNull(max);
        return rs -> rs.cumulativeDelay().compareTo(max) <= 0 ? this.apply(rs) : Optional.empty();
    }

    /**
     * Simulates the behavior of this retry policy for a given number of iterations and returns a list of
     * {@link RetryStatus} objects representing the simulation.
     *
     * @param iterations the number of iterations to simulate
     * @return a list of {@link RetryStatus} objects representing the simulated behavior
     * @throws IllegalArgumentException if iterations is less than or equal to zero
     */
    default List<RetryStatus> simulate(int iterations) {
        if (iterations <= 0) throw new IllegalArgumentException("iterations <= 0");
        List<RetryStatus> simulation = new ArrayList<>();
        RetryStatus next = new RetryStatus(0, Duration.ZERO, Duration.ZERO);
        for (int i = 1; i <= iterations; i++) {
            Optional<Duration> opt = this.apply(next);
            if (opt.isPresent()) {
                simulation.add(next);
                Duration delay = opt.get();
                next = new RetryStatus(next.counter() + 1,
                                       next.cumulativeDelay().plus(delay),
                                       delay);
            } else {
                break;
            }
        }
        return simulation;
    }
}
