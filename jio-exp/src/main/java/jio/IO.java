package jio;


import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Represents a functional effect which is modeled with a supplier that returns a
 * {@link CompletableFuture} of type O, being O the type of the result the future
 * is completed with when the computation succeeds. From now on well talk about
 * effects that are reduced to values of certain type.
 * <p>
 * A computation can also fail, in which case the returned CompletableFuture is completed with
 * a CompletionException, holding in its cause the original exception.
 * <p>
 * The methods {@link IO#join()} and {@link IO#get()} trigger the execution of the computation.
 * Since join is blocking, is only used for testing purposes (nevertheless, if you are using Loom is
 * perfectly fine to block a fiber)
 * <p>
 * There are different static factory methods to create effects.
 *
 * @param <O> the type of the result returned by the computation when it succeeds
 */

@SuppressWarnings("JavadocReference")
public sealed abstract class IO<O> implements Supplier<CompletableFuture<O>> permits Delay, Exp, Val {


    /**
     * Effect that always succeed with true
     */
    public static final IO<Boolean> TRUE = fromValue(true);

    /**
     * Effect that always succeed with false
     */
    public static final IO<Boolean> FALSE = fromValue(false);

    /**
     * Creates an effect that is always reduced to null. We need a static generic method to capture the type
     * of the caller:
     * <pre>
     * {@code
     *
     *         IO<String> a =  NULL();
     *         IO<Integer> b = NULL();
     *
     * }
     * </pre>
     *
     * @param <O> the captured type
     * @return the null effect
     */
    public static <O> IO<O> NULL() {
        return IO.fromValue(null);
    }

    /**
     * Creates an effect from a lazy computation that returns a CompletableFuture.
     *
     * @param effect the lazy effect
     * @param <O>    the type of the effect
     * @return an IO effect
     */
    public static <O> IO<O> fromEffect(final Supplier<CompletableFuture<O>> effect) {
        return new Val<>(requireNonNull(effect));
    }


    /**
     * Creates an effect from a callable that returns a closable resource and maps it into a value.
     * <strong>It closes the resource to avoid any memory leak.</strong>
     *
     * @param resource the resource supplier
     * @param map      the map function
     * @param <O>      the type of the effect
     * @param <I>      the type of the resource
     * @return an IO effect
     */


    public static <O, I extends AutoCloseable> IO<O> fromResource(final Callable<I> resource,
                                                                  final Function<I, O> map
                                                                 ) {
        return IO.fromEffect(() -> {

            try (var r = resource.call()) {
                return CompletableFuture.completedFuture(r)
                                        .thenApply(map);
            }
            catch (Exception e) {
                return CompletableFuture.failedFuture(e);
            }
        });
    }


    /**
     * Creates an effect that always succeeds and returns the same value
     *
     * @param val the value. Null is allowed
     * @param <O> the type of the value
     * @return an effect
     */
    public static <O> IO<O> fromValue(final O val) {
        return new Val<>(() -> CompletableFuture.completedFuture(val));
    }

    /**
     * Creates an effect from a lazy computation. Since a supplier can't throw an exception,
     * it also exists the constructor {@link #fromTask(Callable)} that takes a {@link Callable callabe}
     * instead of a {@link Supplier supplier}
     *
     * @param supplier the supplier
     * @param <O>      the type of the effect
     * @return an effect
     */
    public static <O> IO<O> fromSupplier(final Supplier<O> supplier) {
        requireNonNull(supplier);
        return
                new Val<>(() -> {
                    try {
                        return CompletableFuture.completedFuture(supplier.get());
                    } catch (Throwable e) {
                        return CompletableFuture.failedFuture(e);
                    }
                });
    }

    /**
     * Creates an effect from a callable.
     *
     * @param callable the callable
     * @param <O>      the type of the effect
     * @return an effect
     */
    public static <O> IO<O> fromTask(final Callable<O> callable) {
        requireNonNull(callable);
        return
                new Val<>(() -> {
                    try {
                        return CompletableFuture.completedFuture(callable.call());
                    } catch (Throwable e) {
                        return CompletableFuture.failedFuture(e);
                    }
                });
    }

    /**
     * Creates an effect from a callable that is called by a thread from
     * the specified executor pool
     *
     * @param callable the callable
     * @param executor the executor
     * @param <O>      the type of the effect
     * @return an effect
     */
    public static <O> IO<O> fromTask(final Callable<O> callable,
                                     final Executor executor
                                    ) {
        requireNonNull(callable);
        return IO.fromEffect(() -> {
            try {
                return CompletableFuture.supplyAsync(() -> {
                                                         try {
                                                             return callable.call();
                                                         } catch (Exception e) {
                                                             throw new CompletionException(e);
                                                         }
                                                     },
                                                     executor
                                                    );
            } catch (Exception e) {
                return CompletableFuture.failedFuture(e);
            }
        });

    }

    /**
     * Creates an effect that always fails with the specified exception
     *
     * @param exc the exception
     * @param <O> the type captured by this method and NOT the type of the exception
     * @return an effect
     */
    public static <O> IO<O> fromFailure(final Throwable exc) {
        requireNonNull(exc);
        return new Val<>(() -> CompletableFuture.failedFuture(exc));

    }

    /**
     * Creates an effect from a lazy computation that is performed by a thread from
     * the specified executor pool.Since a supplier can't throw an exception,
     * it also exists the constructor {@link #fromTask(Callable)} that takes a {@link Callable callabe}
     * instead of a {@link Supplier supplier}
     *
     * @param supplier the supplier
     * @param executor the executor
     * @param <O>      the type of the effect
     * @return an effect
     */
    public static <O> IO<O> fromSupplier(final Supplier<O> supplier,
                                         final Executor executor
                                        ) {
        requireNonNull(supplier);
        requireNonNull(executor);
        return new Val<>(
                () -> CompletableFuture.supplyAsync(supplier,
                                                    executor
                                                   )
        );

    }

    /**
     * Creates an effect from a supplier with the Java interface {@link ForkJoinPool.ManagedBlocker ManagedBlocker}
     * to use efficiently the common Java fork/join thread pool.
     * <p>
     * Take it account that the maximum default level of parallisem you can reach with the ForkJoin pool is by
     * default {@link ForkJoinPool#DEFAULT_COMMON_MAX_SPARES} and can be overwritten
     * using the "java.util.concurrent.ForkJoinPool.common.maximumSpares" system property.
     * In this case a {@link RejectedExecutionException} exception with the message "Thread limit exceeded
     * replacing blocked worker"
     * is the result of the computation.
     *
     * @param supplier the supplier
     * @param <O>      the type of the effect
     * @return an effect
     */
    public static <O> IO<O> fromManagedSupplier(final Supplier<O> supplier) {
        requireNonNull(supplier);
        return new Val<>(() ->
                                 Thread.currentThread() instanceof ForkJoinWorkerThread ?
                                         CompletableFuture.completedFuture(ManagedBlockerHelper.computeSupplier(supplier)) :
                                         CompletableFuture.supplyAsync(() -> ManagedBlockerHelper.computeSupplier(supplier),
                                                                       ForkJoinPool.commonPool()
                                                                      ));
    }

    /**
     * Creates an effect from a task with the Java interface {@link ForkJoinPool.ManagedBlocker ManagedBlocker}
     * to use efficiently the common Java fork/join thread pool.
     * <p>
     * Take it account that the maximum default level of parallelism you can reach with the ForkJoin pool is by
     * default {@link ForkJoinPool#DEFAULT_COMMON_MAX_SPARES} and can be overwritten
     * using the "java.util.concurrent.ForkJoinPool.common.maximumSpares" system property.
     * In this case a {@link RejectedExecutionException} exception with the message "Thread limit exceeded replacing blocked worker"
     * is the result of the computation.
     *
     * @param task the supplier
     * @param <O>  the type of the effect
     * @return an effect
     */
    public static <O> IO<O> fromManagedTask(final Callable<O> task) {
        requireNonNull(task);
        return new Val<>(() ->
                                 Thread.currentThread() instanceof ForkJoinWorkerThread ?
                                         CompletableFuture.completedFuture(ManagedBlockerHelper.computeTask(task)) :
                                         CompletableFuture.supplyAsync(() -> ManagedBlockerHelper.computeTask(task))
        );
    }


    /**
     * Creates a new effect that, when this succeeds, maps the computed value into another value with the specified function
     *
     * @param fn  the map function
     * @param <P> the result type
     * @return a new effect
     */
    public <P> IO<P> map(final Function<O, P> fn) {
        requireNonNull(fn);
        return IO.fromEffect(() -> requireNonNull(get()).thenApply(fn));
    }


    /**
     * Creates a new effect applying the specified lambda to the result of this effect (as long
     * as it succeeds). If it fails, the effect ends and is evaluated to the failure, not
     * applying any lambda.
     * This is the famous flatMap, thenCompose or bind (depending on the language and/or library).
     * Since is used all the time, I chose the name then just for brevity reasons.
     *
     * @param fn  the lambda that takes the result of this effect to create another one
     * @param <Q> the result type of the new effect
     * @return a new effect
     */
    public <Q> IO<Q> then(final Lambda<O, Q> fn) {
        requireNonNull(fn);
        return fromEffect(() -> requireNonNull(get()).thenCompose(it -> fn.apply(it)
                                                                          .get()
                                                                 )
                         );
    }

    /**
     * Creates a new effect applying the specified lambda to the result of this effect (as long
     * as it succeeds). The lambda is evaluated by a thread from the pool of the specified executor.
     * If it fails, the effect ends and is evaluated to the failure, not applying any lambda.
     * This is the famous flatMap, thenCompose or bind (depending on the language and/or library).
     * Since is used all the time, I chose the name then just for brevity reasons.
     *
     * @param fn       the lambda that takes the result of this effect to create another one
     * @param <Q>      the result type of the new effect
     * @param executor the executor
     * @return a new effect
     */
    public <Q> IO<Q> thenOn(final Lambda<O, Q> fn,
                            final Executor executor
                           ) {
        requireNonNull(fn);
        requireNonNull(executor);
        return fromEffect(() -> requireNonNull(get()).thenComposeAsync(it -> fn.apply(it)
                                                                               .get(),
                                                                       executor
                                                                      )
                         );


    }

    /**
     * Creates a new effect after evaluating this one. If this succeeds, the result is applied to
     * the specified successLambda. If this fails, instead of ending with a failure, the exception
     * is applied to the specified failureLambda.
     *
     * @param successLambda the lambda that takes the result to create another one
     * @param failureLambda the lambda that takes the exception to create another one
     * @param <Q>           the result type of the new effect
     * @return a new effect
     */

    public <Q> IO<Q> then(final Lambda<O, Q> successLambda,
                          final Lambda<Throwable, Q> failureLambda
                         ) {
        requireNonNull(successLambda);
        requireNonNull(failureLambda);

        return fromEffect(() -> requireNonNull(get()).thenCompose(it -> successLambda.apply(it)
                                                                                     .get()
                                                                 )
                                                     .exceptionallyCompose(exc -> failureLambda.apply(exc.getCause())
                                                                                           .get()
                                                                      )
                         );
    }

    /**
     * Creates a new effect after evaluating this one. If this succeeds, the result is applied to
     * the specified successLambda. If this fails, instead of ending with a failure, the exception
     * is applied to the specified failureLambda. In both cases, the lambdas are evaluated by
     * a thread from the pool of the specified executor
     *
     * @param successLambda the lambda that takes the result to create another one
     * @param failureLambda the lambda that takes the exception to create another one
     * @param executor      the executor
     * @param <Q>           the result type of the new effect
     * @return a new effect
     */

    public <Q> IO<Q> thenOn(final Lambda<O, Q> successLambda,
                            final Lambda<Throwable, Q> failureLambda,
                            final Executor executor
                           ) {
        requireNonNull(successLambda);
        requireNonNull(failureLambda);
        requireNonNull(executor);

        return fromEffect(() -> requireNonNull(get()).thenComposeAsync(it -> successLambda.apply(it)
                                                                                          .get(),
                                                                       executor
                                                                      )
                                                     .exceptionallyComposeAsync(exc -> failureLambda.apply(exc.getCause())
                                                                                                .get(),
                                                                            executor
                                                                           )
                         );

    }


    /**
     * Creates a new effect that will handle any failure that this effect might contain
     * and will be recovered with the value evaluated by the specified function
     *
     * @param fn the function to apply if this effect fails
     * @return a new effect
     */
    public IO<O> recover(final Function<Throwable, O> fn) {
        requireNonNull(fn);
        return then(IO::fromValue, exc -> fromValue(fn.apply(exc)));
    }


    /**
     * Creates a new effect that will handle any failure that this effect might contain
     * and will be recovered with the effect evaluated by the specified lambda
     *
     * @param lambda the function to apply if this effect fails
     * @return a new effect
     */
    public IO<O> recoverWith(final Lambda<Throwable, O> lambda) {
        requireNonNull(lambda);
        return then(IO::fromValue,
                    lambda
                   );
    }

    /**
     * Creates a new effect that will handle any failure that this effect might contain
     * and will be recovered with the effect evaluated by the specified lambda. The lambda
     * computes the new effect in a thread from the give executor.
     *
     * @param fn       the function to apply if this effect fails
     * @param executor the executor
     * @return a new effect
     */

    public IO<O> recoverWithOn(final Lambda<Throwable, O> fn,
                               final Executor executor
                              ) {
        requireNonNull(fn);
        requireNonNull(executor);
        return thenOn(IO::fromValue,
                      fn,
                      executor
                     );

    }

    /**
     * Creates a new effect that will handle any failure that this effect might contain
     * and will be recovered with a new effect evaluated by the specified lambda. If the
     * new effect fails again, the new failure is ignored and is the original the one
     * that is returned (this is the difference with {@link #recoverWith(Lambda)} recoverWith} that would return this new
     * failure)
     *
     * @param lambda the function to apply if this effect fails
     * @return a new effect
     */
    public IO<O> fallbackTo(final Lambda<Throwable, O> lambda) {
        requireNonNull(lambda);

        return then(IO::fromValue,
                    exc -> lambda.apply(exc)
                                 .then(IO::fromValue,
                                       exc1 -> fromFailure(exc)
                                      )
                   );

    }

    /**
     * Creates a new effect that will handle any failure that this effect might contain
     * and will be recovered with a new effect evaluated by the specified lambda in a thread from
     * the given executor pool. If the new effect fails again, the new failure is ignored and
     * is the original the one that is returned (this is the difference with {@link #recoverWith(Lambda) recoverWith}
     * that would return this new
     * failure)
     *
     * @param lambda   the function to apply if this effect fails
     * @param executor the executor
     * @return a new effect
     */
    public IO<O> fallbackToOn(final Lambda<Throwable, O> lambda,
                              final Executor executor
                             ) {
        requireNonNull(lambda);
        requireNonNull(executor);

        return thenOn(IO::fromValue,
                      exc -> lambda.apply(exc)
                                   .thenOn(IO::fromValue,
                                           exc1 -> fromFailure(exc),
                                           executor
                                          ),
                      executor
                     );
    }

    /**
     * creates a new effect that passes the exception to the specified failConsumer in case
     * of failure.
     * The given consumer can't fail. If it failed, the exception would be just printed out
     * on the console.
     *
     * @param failConsumer the consumer that takes the  exception
     * @return a new effect
     */
    public IO<O> peekFailure(final Consumer<Throwable> failConsumer) {
        return peek($ -> {
                    },
                    failConsumer
                   );
    }

    /**
     * creates a new effect that passes the computed value to the specified successConsumer.
     * The given consumer can't fail. If it failed, the exception would be just printed out
     * on the console .
     *
     * @param successConsumer the consumer that takes the successful result
     * @return a new effect
     */
    public IO<O> peekSuccess(final Consumer<O> successConsumer) {
        return peek(successConsumer,
                    e -> {
                    }
                   );
    }

    /**
     * creates a new effect that passes the computed value to the specified successConsumer and
     * any possible failure to the specified failureConsumer. The given consumers can't fail. If
     * they failed, the exception would be just printed out on the console .
     *
     * @param successConsumer the consumer that takes the successful result
     * @param failureConsumer the consumer that takes the failure
     * @return a new effect
     */
    public IO<O> peek(final Consumer<O> successConsumer,
                      final Consumer<Throwable> failureConsumer
                     ) {
        requireNonNull(successConsumer);
        requireNonNull(failureConsumer);
        return then(it -> {
            try {
                successConsumer.accept(it);
            } catch (Exception exception) {
                Fun.publishException("peek","Exception thrown by a consumer provided by the API client",exception);
            }
            return fromValue(it);
        }, exc -> {
            try {
                failureConsumer.accept(exc);
            } catch (Exception exception) {
                Fun.publishException("peek","Exception thrown by a consumer provided by the API client",exception);
            }
            return fromFailure(exc);
        });
    }


    /**
     * creates a new effect that passes the computed value to the specified successConsumer and
     * any possible failure to the specified failureConsumer. The given consumers can't fail. If
     * they failed, the exception would be just printed out on the console.
     * <strong>A thread from the specified pool executor performs the consumer tasks.</strong>
     *
     * @param successConsumer the consumer that takes the successful result
     * @param failureConsumer the consumer that takes the failure
     * @param executor        the executor
     * @return a new effect
     */
    public IO<O> peekOn(final Consumer<O> successConsumer,
                        final Consumer<Throwable> failureConsumer,
                        final Executor executor
                       ) {
        requireNonNull(successConsumer);
        requireNonNull(failureConsumer);
        requireNonNull(executor);
        return thenOn(it -> {
            try {
                successConsumer.accept(it);
            } catch (Exception exception) {
                Fun.publishException("peekOn","Exception thrown by the consumer provided by the API client",exception);
            }
            return fromValue(it);
        }, exc -> {
            try {
                failureConsumer.accept(exc);
            } catch (Exception exception) {
                Fun.publishException("peekOn","Exception thrown by the consumer provided by the API client",exception);
            }
            return fromFailure(exc);
        }, executor);
    }


    /**
     * Creates a new effect that fails with a {@link TimeoutException} if this effect doesn't
     * compute any value in the given time
     *
     * @param time the time amount
     * @param unit the time unit
     * @return a new effect
     */
    public IO<O> timeout(final int time,
                         final TimeUnit unit
                        ) {
        if (time <= 0) throw new IllegalArgumentException("time <= 0");
        requireNonNull(unit);
        return IO.fromEffect(() -> requireNonNull(get()).orTimeout(time, unit));

    }


    /**
     * Creates a new effect that is evaluated to the given value if this
     * effect doesn't terminate in the specified time. In other words,
     * a timeout failure is map to the specified default value.
     *
     * @param time       the time amount
     * @param unit       the time unit
     * @param defaultVal the default value evaluated if a timeout happens
     * @return a new effect
     */
    public IO<O> timeoutOrElse(final int time,
                               final TimeUnit unit,
                               final Supplier<O> defaultVal
                              ) {
        if (time <= 0) throw new IllegalArgumentException("time <= 0");
        requireNonNull(unit);
        requireNonNull(defaultVal);
        return IO.fromEffect(() -> requireNonNull(get()).completeOnTimeout(defaultVal.get(),
                                                                           time,
                                                                           unit
                                                                          )
                            );

    }

    /**
     * Fires the evaluation of this effect and BLOCKS the caller thread, waiting for the
     * computed value to be returned.
     * Any failure during the computation will be thrown as an exception.
     * <strong>Since is a blocking call, most of the time this method is only used for testing
     * purposes</strong>.
     *
     * @return the computed value
     * @throws CompletionException failure produced during the computation
     */

    public O join() {
        return requireNonNull(get()).join();
    }

    /**
     * Creates a new effect that will retry the computation according to the specified {@link RetryPolicy policy} if
     * this effect fails and the failure is evaluated true against the given predicate
     *
     * @param policy    the policy
     * @param predicate the predicate
     * @return a new effect
     */

    public IO<O> retry(final Predicate<Throwable> predicate,
                       final RetryPolicy policy
                      ) {
        requireNonNull(predicate);
        requireNonNull(policy);
        return retry(this,
                     policy,
                     RetryStatus.ZERO,
                     predicate
                    );

    }

    /**
     * Creates a new effect that will retry with a thread from the given executor the computation according
     * to the specified {@link RetryPolicy policy} if
     * this effect fails and the failure is evaluated true against the given predicate.
     *
     * @param policy    the policy
     * @param predicate the predicate
     * @param executor  the executor
     * @return a new effect
     */

    public IO<O> retryOn(final Predicate<Throwable> predicate,
                         final RetryPolicy policy,
                         final Executor executor
                        ) {
        requireNonNull(predicate);
        requireNonNull(policy);
        return retryOn(this,
                       policy,
                       RetryStatus.ZERO,
                       predicate,
                       executor
                      );

    }

    /**
     * Creates a new effect that will retry with a thread from the given executor the computation according
     * to the specified {@link RetryPolicy policy}.
     *
     * @param policy   the policy
     * @param executor the executor
     * @return a new effect
     */

    public IO<O> retryOn(final RetryPolicy policy,
                         final Executor executor
                        ) {
        requireNonNull(policy);
        return retryOn(this,
                       policy,
                       RetryStatus.ZERO,
                       e -> true,
                       executor
                      );

    }

    /**
     * TODO: explain who thread does the retries: if delay is zero, the same, if not, a managed task is created and
     * executed with a thread from the fork join pool
     * Creates a new effect that will retry the computation according to the specified {@link RetryPolicy policy} if
     * this effect fails
     *
     * @param policy the policy
     * @return a new effect
     */
    public IO<O> retry(final RetryPolicy policy) {
        return retry(e -> true, policy);
    }

    private IO<O> retry(IO<O> exp,
                        Function<RetryStatus, Optional<Duration>> policy,
                        RetryStatus rs,
                        Predicate<Throwable> predicate
                       ) {

        return exp.then(IO::fromValue,
                        exc -> {
                            if (predicate.test(exc)) {
                                Optional<Duration> delayOpt = policy.apply(rs);
                                if (delayOpt.isEmpty()) return fromFailure(exc);
                                Duration duration = delayOpt.get();

                                if (duration.isZero())
                                    return retry(exp,
                                                 policy,
                                                 new RetryStatus(rs.counter() + 1,
                                                                 rs.cumulativeDelay(),
                                                                 Duration.ZERO
                                                 ),
                                                 predicate
                                                );
                                return Delay.of(duration,
                                                ForkJoinPool.commonPool()
                                               )
                                            .then($ -> retry(exp,
                                                             policy,
                                                             new RetryStatus(rs.counter() + 1,
                                                                             rs.cumulativeDelay().plus(duration),
                                                                             duration
                                                             ),
                                                             predicate
                                                            )
                                                 );
                            }
                            return fromFailure(exc);
                        }
                       );

    }


    private IO<O> retryOn(IO<O> exp,
                          Function<RetryStatus, Optional<Duration>> policy,
                          RetryStatus rs,
                          Predicate<Throwable> predicate,
                          Executor executor
                         ) {

        return exp.then(IO::fromValue,
                        exc -> {
                            if (predicate.test(exc)) {
                                Optional<Duration> delayOpt = policy.apply(rs);
                                if (delayOpt.isEmpty()) return fromFailure(exc);
                                Duration duration = delayOpt.get();

                                if (duration.isZero())
                                    return retryOn(exp,
                                                   policy,
                                                   new RetryStatus(rs.counter() + 1,
                                                                   rs.cumulativeDelay(),
                                                                   Duration.ZERO
                                                   ),
                                                   predicate,
                                                   executor
                                                  );
                                return Delay.of(duration,
                                                executor
                                               )
                                            .then($ -> retryOn(exp,
                                                               policy,
                                                               new RetryStatus(rs.counter() + 1,
                                                                               rs.cumulativeDelay().plus(duration),
                                                                               duration
                                                               ),
                                                               predicate,
                                                               executor
                                                              )
                                                 );
                            }
                            return fromFailure(exc);
                        }
                       );

    }


    /**
     * Creates a new effect that repeats the computation according to the specified policy if the result
     * is evaluated to true by the given predicate
     *
     * @param predicate the predicate
     * @param policy    the policy
     * @return a new effect
     */
    public IO<O> repeat(final Predicate<O> predicate,
                        final RetryPolicy policy
                       ) {
        return repeat(this,
                      requireNonNull(policy),
                      RetryStatus.ZERO,
                      requireNonNull(predicate)
                     );

    }

    /**
     * Creates a new effect that repeats with a thread from the given executor the computation according to the specified
     * policy if the result is evaluated to true by the given predicate
     *
     * @param predicate the predicate
     * @param policy    the policy
     * @param executor  the executor
     * @return a new effect
     */
    public IO<O> repeatOn(final Predicate<O> predicate,
                          final RetryPolicy policy,
                          final Executor executor
                         ) {
        return repeatOn(this,
                        requireNonNull(policy),
                        RetryStatus.ZERO,
                        requireNonNull(predicate),
                        executor
                       );

    }

    private IO<O> repeat(IO<O> exp,
                         RetryPolicy policy,
                         RetryStatus rs,
                         Predicate<O> predicate
                        ) {

        return exp.then(o -> {
            if (predicate.test(o)) {
                Optional<Duration> delayOpt = policy.apply(rs);
                if (delayOpt.isEmpty()) return fromValue(o);
                Duration duration = delayOpt.get();
                if (duration.isZero())
                    return repeat(exp,
                                  policy,
                                  new RetryStatus(rs.counter() + 1,
                                                  rs.cumulativeDelay(),
                                                  Duration.ZERO
                                  ),
                                  predicate
                                 );
                return Delay.of(duration,
                                ForkJoinPool.commonPool()
                               )
                            .then($ -> repeat(exp,
                                              policy,
                                              new RetryStatus(rs.counter() + 1,
                                                              rs.cumulativeDelay().plus(duration),
                                                              duration
                                              ),
                                              predicate
                                             )
                                 );
            }
            return fromValue(o);
        });

    }

    private IO<O> repeatOn(IO<O> exp,
                           RetryPolicy policy,
                           RetryStatus rs,
                           Predicate<O> predicate,
                           Executor executor
                          ) {

        return exp.then(o -> {
            if (predicate.test(o)) {
                Optional<Duration> delayOpt = policy.apply(rs);
                if (delayOpt.isEmpty()) return fromValue(o);
                Duration duration = delayOpt.get();
                if (duration.isZero())
                    return repeatOn(exp,
                                    policy,
                                    new RetryStatus(rs.counter() + 1,
                                                    rs.cumulativeDelay(),
                                                    Duration.ZERO
                                    ),
                                    predicate,
                                    executor
                                   );
                return Delay.of(duration,
                                executor
                               )
                            .then($ -> repeatOn(exp,
                                                policy,
                                                new RetryStatus(rs.counter() + 1,
                                                                rs.cumulativeDelay().plus(duration),
                                                                duration
                                                ),
                                                predicate,
                                                executor
                                               )

                                 );
            }
            return fromValue(o);
        });

    }


    /**
     * Returns the firs computation that ends, no matter if it fails. It's implemented with
     * the {@link CompletableFuture#anyOf(CompletableFuture[])} method, so it inherits its
     * behaviour. This operator is a good candidate to be reimplemented with Fibers in the
     * future to benefit from cancellation and no returns failing computations.
     *
     * @param first  the first computation
     * @param others the rest computations
     * @param <O>    the type of the computation
     * @return a new computation
     */
    @SafeVarargs
    @SuppressWarnings({"unchecked", "varargs"})
    public static <O> IO<O> race(final IO<O> first, final IO<O>... others) {
        requireNonNull(first);
        requireNonNull(others);
        List<IO<O>> list = new ArrayList<>();
        list.add(first);
        if (others.length > 0) {
            List<IO<O>> c = Arrays.stream(others)
                                  .toList();
            list.addAll(c);
        }
        return IO.fromEffect(() -> {
                             var cfs = list.stream()
                                           .map(Supplier::get)
                                           .toArray(CompletableFuture[]::new);
                             return CompletableFuture.anyOf(cfs)
                                                     .thenApply(it -> ((O) it));
                         }
                            );

    }

    /**
     * Creates a copy of this effect that prints out the result of this effect with the following format:
     * [instant] [thread] [computation time ns] [result.string() || exception.getMessage()]
     *
     * @return a new effect
     */
    public IO<O> debug() {
        return debug(EventBuilder.ofExp(getClass().getSimpleName()));
    }

    /**
     * Creates a new effect that writes a log message to the given logger when the computation is done.
     * The message is built with the specified message builder.
     *
     * @param builder the builder to create the log message
     * @return a new effect
     */
    public IO<O> debug(final EventBuilder<O> builder) {
        requireNonNull(builder);
        builder.exp = this.getClass().getSimpleName();
        return IO.fromSupplier(ExpEvent::new)
                 .then(event -> this.peek(val -> builder.updateAndCommit(val, event),
                                          exc -> builder.updateAndCommit(exc, event)
                                         )
                      );
    }


}
