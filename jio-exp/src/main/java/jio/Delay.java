package jio;


import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;


/**
 * Represents a delay that is modeled with an IO effect that is reduced to null after the specified time.
 * It's implemented using {@link CompletableFuture#delayedExecutor}.
 *
 * <p>It's widely used by the {@link RetryPolicy} function to implement different policies.
 * For example, to delay an effect for 1 second:
 *
 * <pre>
 * {@code
 * IO<O> effect = ...
 * IO<O> delayedEffect = Delay.of(Duration.ofSeconds(1),
 *                                ForkJoinPool.commonPool())
 *                            .then(nill -> effect);
 * }
 * </pre>
 *
 * @see RetryPolicy
 */
public final class Delay extends IO<Void> {

    private final Duration duration;
    private final Executor executor;

    private Delay(final Duration duration, final Executor executor) {
        this.duration = duration;
        this.executor = executor;
    }

    /**
     * Creates a delay of the specified duration with the given base executor, which
     * means a CompletableFuture will be completed with null asynchronously by a thread
     * from the executor.
     *
     * @param duration the duration
     * @param executor the executor
     * @return a Delay
     */
    public static Delay of(final Duration duration, final Executor executor) {
        return new Delay(requireNonNull(duration), executor);
    }

    /**
     * Returns a CompletableFuture completed with null after the specified delay
     * by a thread from the executor. If the duration is zero, the future is completed
     * with the caller thread.
     *
     * @return a CompletableFuture
     */
    @Override
    public CompletableFuture<Void> get() {
        return duration.isZero() ?
                CompletableFuture.completedFuture(null) :
                CompletableFuture.supplyAsync(() -> null,
                                              CompletableFuture.delayedExecutor(duration.toMillis(), MILLISECONDS, executor));
    }
}
