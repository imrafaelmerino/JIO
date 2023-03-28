package jio;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;


/**
 * A RetryPolicy is a function that takes a RetryStatus and possibly returns the duration of the delay to wait
 * before the next try.
 * Iteration numbers start at zero and increase by one on each retry. An <code>Optional.empty()</code>
 * return value from the function implies we have reached the retry limit.
 * You can collapse multiple strategies into one using the {@link #append(RetryPolicy) append} method.
 * There are a number of policies available in
 * {@link RetryPolicies}. There are also a few combinators to transform policies, including:
 * {@link #capDelay(Duration)} (Timer)}, {@link #limitRetriesByDelay(Duration)} and
 * {@link #limitRetriesByCumulativeDelay(Duration)}.
 * Always simulate any policy you define with {@link #simulate(int)} to check it's behaved as expected
 */
public interface RetryPolicy extends Function<RetryStatus, Optional<Duration>> {

    /**
     * The semantics of this combination is as follows:
     * If either policy (this or other) returns Optional.empty(), the combined policy returns Optional.empty().
     * This can be used to inhibit after a number of retries, for example.
     * If both policies return a delay, the larger delay will be used. This is quite natural
     * when combining multiple policies to achieve a certain effect. For an example of composing policies like this,
     * we can use join to create a policy that retries up to 5 times, starting with a 10 ms delay and increasing
     * exponentially.
     *
     * @param other the other retry policy to be appended
     * @return a new retry policy
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
     * There is also an operator followedBy to sequentially compose policies, i.e. if the first one wants to give up,
     * use the second one. As an example, we can retry with a 100ms delay 5 times and then retry every minute.
     *
     * @param other the other policy to be applied after this policy gives up
     * @return a new retry policy
     */
    default RetryPolicy followedBy(final RetryPolicy other) {
        Objects.requireNonNull(other);
        return rs -> {
            Optional<Duration> delay = this.apply(rs);
            return delay.isEmpty() ? other.apply(rs) : delay;
        };
    }

    /**
     * set an upper bound on the delay between retries
     *
     * @param cap the upper bound
     * @return a new policy
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
     * give up when the delay between retries reaches a certain limit
     *
     * @param max the limit
     * @return a new policy
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
     * give up when the total delay reaches a certain limit
     *
     * @param max the limit
     * @return a new policy
     */
    default RetryPolicy limitRetriesByCumulativeDelay(final Duration max) {
        Objects.requireNonNull(max);
        return rs -> rs.cumulativeDelay().compareTo(max) <= 0 ? this.apply(rs) : Optional.empty();
    }

    /**
     * runs this policy up to N iterations and gather results
     *
     * @param iterations the number of iterations
     * @return a list of {@link RetryStatus} that represents a simulation
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
            } else {break;}
        }
        return simulation;
    }
}
