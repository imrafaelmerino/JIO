package jio;


import java.io.IOException;
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
 * Represents a functional effect that encapsulates asynchronous computations, including successful results and
 * failures. This class models a computation that returns a {@link CompletableFuture} of type `O`, where `O` is the type
 * of the result the future is completed with when the computation succeeds. Functional effects are used to model
 * asynchronous operations in a composable and error-handling manner.
 *
 * <p>
 * A computation can either succeed, in which case the returned {@link CompletableFuture} is completed with a result, or
 * it can fail, in which case the future is completed with a {@link CompletionException} holding the original exception
 * that caused the failure.
 *
 * <p>
 * Functional effects are typically created using various factory methods provided by this class. These factory methods
 * allow you to create effects from different types of computations, such as lazy computations, tasks, resources, and
 * more.
 *
 * <p>
 * Functional effects support a wide range of operations for composing, transforming, and handling asynchronous
 * computations. These operations include mapping, flat mapping, error handling, retries, timeouts, and debugging.
 * <p>
 * To perform a blocking wait for the computation's result, you can use the {@link IO#result()} method, which blocks the
 * caller thread until the computation is complete and returns the result or throws a failure as a
 * {@link CompletionException}.
 *
 * <p>
 * Functional effects are a powerful tool for modeling and handling asynchronous operations in a composable way while
 * providing robust error handling and retry capabilities. They are particularly useful for dealing with asynchronous
 * I/O operations, concurrent processing, and distributed systems.
 *
 * @param <O> the type of the result returned by the computation when it succeeds.
 * @see CompletableFuture
 * @see CompletionException
 * @see java.util.concurrent.Executor
 * @see ForkJoinPool.ManagedBlocker
 * @see RetryPolicy
 * @see EventBuilder
 * @see ExpEvent
 * @see Val
 * @see Delay
 * @see Exp
 */

@SuppressWarnings("JavadocReference")
public sealed abstract class IO<O> implements Supplier<CompletableFuture<O>> permits Delay, Exp, Val {


    /**
     * Effect that always succeed with true
     */
    public static final IO<Boolean> TRUE = succeed(true);

    /**
     * Effect that always succeed with false
     */
    public static final IO<Boolean> FALSE = succeed(false);

    /**
     * Creates an effect that always produces a result of null. This method is generic and captures the type of the
     * caller, allowing you to create null effects with different result types.
     *
     * <p>Example usage:
     * <pre>{@code
     * IO<String> a = NULL();
     * IO<Integer> b = NULL();
     * }</pre>
     *
     * @param <O> the type parameter that represents the result type of the null effect.
     * @return an effect that produces a null result.
     */
    public static <O> IO<O> NULL() {
        return IO.succeed(null);
    }

    /**
     * Creates an effect from a lazy computation that returns a CompletableFuture. This method allows you to encapsulate
     * an asynchronous operation represented by a lazy future into an IO effect.
     *
     * @param effect the lazy future that produces a CompletableFuture.
     * @param <O>    the type parameter representing the result type of the CompletableFuture.
     * @return an IO effect that wraps the provided lazy effect.
     */
    public static <O> IO<O> effect(final Supplier<CompletableFuture<O>> effect) {
        return new Val<>(requireNonNull(effect));
    }


    /**
     * Creates an effect from a callable that returns a closable resource and maps it into a value. This method is
     * designed to handle resources that implement the {@link AutoCloseable} interface, ensuring proper resource
     * management to avoid memory leaks.
     *
     * @param resource the resource supplier that provides the closable resource.
     * @param map      the map function that transforms the resource into a value.
     * @param <O>      the type parameter representing the result type of the effect.
     * @param <I>      the type parameter representing the type of the resource.
     * @return an IO effect that encapsulates the resource handling and mapping.
     */
    public static <O, I extends AutoCloseable> IO<O> resource(final Callable<I> resource,
                                                              final Function<I, O> map
                                                             ) {
        return IO.effect(() -> {
            try (var r = resource.call()) {
                return CompletableFuture.completedFuture(r)
                                        .thenApply(map);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }


    /**
     * Creates an effect that always succeeds and returns the same value.
     *
     * @param val the value to be returned by the effect. Null values are allowed.
     * @param <O> the type parameter representing the result type of the effect.
     * @return an IO effect that always succeeds with the specified value.
     */
    public static <O> IO<O> succeed(final O val) {
        return new Val<>(() -> CompletableFuture.completedFuture(val));
    }


    /**
     * Creates an effect from a lazy computation. Every time the `get()` or `result` method is called, the provided
     * supplier is invoked, and a new computation is returned. Since a supplier cannot throw exceptions, an alternative
     * constructor {@link #task(Callable)} is available that takes a {@link Callable callable} instead of a
     * {@link Supplier supplier} if exception handling is needed.
     *
     * @param supplier the supplier representing the lazy computation.
     * @param <O>      the type parameter representing the result type of the effect.
     * @return an IO effect encapsulating the lazy computation.
     */
    public static <O> IO<O> lazy(final Supplier<O> supplier) {
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
     * Creates an effect from a task modeled with a {@link Callable}. Every time the `get()` or `result` method is
     * called, the provided task is executed.
     *
     * @param callable the callable task to be executed.
     * @param <O>      the type parameter representing the result type of the effect.
     * @return an IO effect encapsulating the callable task.
     */
    public static <O> IO<O> task(final Callable<O> callable) {
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
     * Creates an effect from a task modeled with a {@link Callable} and executes it using the specified
     * {@link Executor}. Every time the `get()` or `result` method is called, the provided task is executed
     * asynchronously using the given executor.
     *
     * @param callable the callable task to be executed.
     * @param executor the executor responsible for running the task.
     * @param <O>      the type parameter representing the result type of the effect.
     * @return an IO effect encapsulating the callable task.
     */
    public static <O> IO<O> task(final Callable<O> callable,
                                 final Executor executor
                                ) {
        requireNonNull(callable);
        return IO.effect(() -> {
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
     * Creates an effect that always returns a failed result with the specified exception.
     *
     * @param exc the exception to be returned by the effect.
     * @param <O> the type parameter representing the result type of the effect (in this case, typically representing an
     *            exception).
     * @return an IO effect that returns the specified exception as its result.
     */
    public static <O> IO<O> fail(final Throwable exc) {
        requireNonNull(exc);
        return new Val<>(() -> CompletableFuture.failedFuture(exc));

    }

    /**
     * Creates an effect from a lazy computation modeled with a {@link Supplier} and executes it asynchronously using
     * the specified {@link Executor}. Every time the `get()` or `result` method is called, the provided lazy
     * computation is executed asynchronously using the given executor.
     *
     * @param supplier the supplier representing the lazy computation to be executed.
     * @param executor the executor responsible for running the computation.
     * @param <O>      the type parameter representing the result type of the effect.
     * @return an IO effect encapsulating the lazy computation executed with the specified executor.
     */
    public static <O> IO<O> lazy(final Supplier<O> supplier,
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
     * Creates an effect from a supplier using the Java interface {@link ForkJoinPool.ManagedBlocker ManagedBlocker} to
     * efficiently utilize the common Java fork/join thread pool.
     * <p>
     * Please note that every time the `get` or `result` methods are called on the returned effect, the `get` method of
     * the provided supplier is invoked, and a new computation is executed.
     * <p>
     * Also, be aware that the maximum default level of parallelism that can be achieved with the ForkJoin pool is
     * typically governed by the {@link ForkJoinPool#DEFAULT_COMMON_MAX_SPARES} constant, which can be overridden using
     * the "java.util.concurrent.ForkJoinPool.common.maximumSpares" system property. Exceeding this limit may result in
     * a {@link RejectedExecutionException} exception with the message "Thread limit exceeded replacing blocked
     * worker."
     *
     * @param supplier the supplier representing the computation.
     * @param <O>      the type parameter representing the result type of the effect.
     * @return an IO effect encapsulating the computation, utilizing the ForkJoin pool if available.
     */
    public static <O> IO<O> managedLazy(final Supplier<O> supplier) {
        requireNonNull(supplier);
        return new Val<>(() ->
                                 Thread.currentThread() instanceof ForkJoinWorkerThread ?
                                         CompletableFuture.completedFuture(ManagedBlockerHelper.computeSupplier(supplier)) :
                                         CompletableFuture.supplyAsync(() -> ManagedBlockerHelper.computeSupplier(supplier),
                                                                       ForkJoinPool.commonPool()
                                                                      ));
    }

    /**
     * Creates an effect from a task modeled with a {@link Callable} and executes it using the Java interface
     * {@link ForkJoinPool.ManagedBlocker ManagedBlocker} to efficiently utilize the common Java fork/join thread pool.
     * <p>
     * Please note that every time the `get` or `result` methods are called on the returned effect, the provided task is
     * executed, and a new computation is performed.
     * <p>
     * Also, be aware that the maximum default level of parallelism that can be achieved with the ForkJoin pool is
     * typically governed by the {@link ForkJoinPool#DEFAULT_COMMON_MAX_SPARES} constant, which can be overridden using
     * the "java.util.concurrent.ForkJoinPool.common.maximumSpares" system property. Exceeding this limit may result in
     * a {@link RejectedExecutionException} exception with the message "Thread limit exceeded replacing blocked
     * worker."
     *
     * @param task the callable task to be executed.
     * @param <O>  the type parameter representing the result type of the effect.
     * @return an IO effect encapsulating the callable task, utilizing the ForkJoin pool if available.
     */
    public static <O> IO<O> managedTask(final Callable<O> task) {
        requireNonNull(task);
        return new Val<>(() ->
                                 Thread.currentThread() instanceof ForkJoinWorkerThread ?
                                         CompletableFuture.completedFuture(ManagedBlockerHelper.computeTask(task)) :
                                         CompletableFuture.supplyAsync(() -> ManagedBlockerHelper.computeTask(task))
        );
    }

    /**
     * Returns the first computation that completes, regardless of whether it fails or succeeds. This method is
     * implemented using {@link CompletableFuture#anyOf(CompletableFuture[])}, and its behavior is inherited from that
     * method. It's important to note that this operator is a good candidate to be reimplemented with Fibers in the
     * future to benefit from cancellation and non-returning failing computations.
     *
     * @param first  the first computation.
     * @param others the rest of the computations.
     * @param <O>    the type of the computation.
     * @return a new computation representing the first to complete among the provided computations.
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
        return IO.effect(() -> {
                             var cfs = list.stream()
                                           .map(Supplier::get)
                                           .toArray(CompletableFuture[]::new);
                             return CompletableFuture.anyOf(cfs)
                                                     .thenApply(it -> ((O) it));
                         }
                        );

    }

    /**
     * Creates a new effect that, when this succeeds, maps the computed value into another value using the specified
     * function.
     *
     * @param fn  the mapping function that transforms the result of this effect.
     * @param <P> the result type of the new effect.
     * @return a new effect that represents the mapped result.
     */
    public <P> IO<P> map(final Function<O, P> fn) {
        requireNonNull(fn);
        return IO.effect(() -> requireNonNull(get()).thenApply(fn));
    }

    /**
     * Creates a new effect by applying the specified {@link Lambda} to the result of this effect (if it succeeds). If
     * this effect fails, the new effect also ends with the same failure, and the lambda is not applied. This method is
     * commonly referred to as "flatMap," "thenCompose," or "bind" in different programming languages and libraries. For
     * brevity, it's named "then" here.
     *
     * @param fn  the lambda that takes the result of this effect to create another one.
     * @param <Q> the result type of the new effect.
     * @return a new effect representing the result of applying the lambda.
     */
    public <Q> IO<Q> then(final Lambda<O, Q> fn) {
        requireNonNull(fn);
        return effect(() -> requireNonNull(get()).thenCompose(it -> fn.apply(it)
                                                                      .get()
                                                             )
                     );
    }

    /**
     * Creates a new effect by applying the specified {@link Lambda} to the result of this effect (if it succeeds). The
     * lambda is evaluated by a thread from the pool of the specified executor. If this effect fails, the new effect
     * also ends with the same failure, and the lambda is not applied. This method is commonly referred to as "flatMap,"
     * "thenCompose," or "bind" in different programming languages and libraries. For brevity, it's named "then" here.
     *
     * @param fn       the lambda that takes the result of this effect to create another one.
     * @param <Q>      the result type of the new effect.
     * @param executor the executor responsible for evaluating the lambda.
     * @return a new effect representing the result of applying the lambda.
     */
    public <Q> IO<Q> thenOn(final Lambda<O, Q> fn,
                            final Executor executor
                           ) {
        requireNonNull(fn);
        requireNonNull(executor);
        return effect(() -> requireNonNull(get()).thenComposeAsync(it -> fn.apply(it)
                                                                           .get(),
                                                                   executor
                                                                  )
                     );


    }

    /**
     * Creates a new effect after evaluating this one. If this succeeds, the result is applied to the specified
     * successLambda. If this fails, instead of ending with a failure, the exception is applied to the specified
     * failureLambda to create a new result.
     *
     * @param successLambda the lambda that takes the result to create another one in case of success.
     * @param failureLambda the lambda that takes the exception to create another result in case of failure.
     * @param <Q>           the result type of the new effect.
     * @return a new effect representing the result of applying either the successLambda or the failureLambda.
     */

    public <Q> IO<Q> then(final Lambda<O, Q> successLambda,
                          final Lambda<Throwable, Q> failureLambda
                         ) {
        requireNonNull(successLambda);
        requireNonNull(failureLambda);

        return effect(() -> requireNonNull(get()).thenCompose(it -> successLambda.apply(it)
                                                                                 .get()
                                                             )
                                                 .exceptionallyCompose(exc -> failureLambda.apply(exc.getCause())
                                                                                           .get()
                                                                      )
                     );
    }

    /**
     * Creates a new effect after evaluating this one. If this succeeds, the result is applied to the specified
     * successLambda. If this fails, instead of ending with a failure, the exception is applied to the specified
     * failureLambda to create a new result. In both cases, the lambdas are evaluated by a thread from the pool of the
     * specified executor.
     *
     * @param successLambda the lambda that takes the result to create another one in case of success.
     * @param failureLambda the lambda that takes the exception to create another result in case of failure.
     * @param executor      the executor responsible for evaluating the successLambda and failureLambda.
     * @param <Q>           the result type of the new effect.
     * @return a new effect representing the result of applying either the successLambda or the failureLambda.
     */

    public <Q> IO<Q> thenOn(final Lambda<O, Q> successLambda,
                            final Lambda<Throwable, Q> failureLambda,
                            final Executor executor
                           ) {
        requireNonNull(successLambda);
        requireNonNull(failureLambda);
        requireNonNull(executor);

        return effect(() -> requireNonNull(get()).thenComposeAsync(it -> successLambda.apply(it)
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
     * Creates a new effect that will handle any failure that this effect might contain and will be recovered with the
     * value evaluated by the specified function. If this effect succeeds, the new effect will also succeed with the
     * same value. If this effect fails, the specified function is applied to the exception to produce a new value for
     * the new effect.
     *
     * @param fn the function to apply if this effect fails, taking the exception as input.
     * @return a new effect representing the original value or the result of applying the function in case of failure.
     */
    public IO<O> recover(final Function<Throwable, O> fn) {
        requireNonNull(fn);
        return then(IO::succeed, exc -> succeed(fn.apply(exc)));
    }

    /**
     * Creates a new effect that will handle any failure that this effect might contain and will be recovered with the
     * effect evaluated by the specified lambda. If this effect succeeds, the new effect will also succeed with the same
     * value. If this effect fails, the specified lambda is applied to the exception to produce a new effect for the new
     * effect.
     *
     * @param lambda the lambda to apply if this effect fails, taking the exception as input and producing a new
     *               effect.
     * @return a new effect representing the original value or the result of applying the lambda in case of failure.
     */
    public IO<O> recoverWith(final Lambda<Throwable, O> lambda) {
        requireNonNull(lambda);
        return then(IO::succeed,
                    lambda
                   );
    }

    /**
     * Creates a new effect that will handle any failure that this effect might contain and will be recovered with the
     * effect evaluated by the specified lambda. The lambda computes the new effect in a thread from the given executor.
     * If this effect succeeds, the new effect will also succeed with the same value. If this effect fails, the
     * specified lambda is applied to the exception to produce a new effect for the new effect.
     *
     * @param fn       the lambda to apply if this effect fails, taking the exception as input and producing a new
     *                 effect.
     * @param executor the executor responsible for evaluating the lambda.
     * @return a new effect representing the original value or the result of applying the lambda in case of failure.
     */

    public IO<O> recoverWithOn(final Lambda<Throwable, O> fn,
                               final Executor executor
                              ) {
        requireNonNull(fn);
        requireNonNull(executor);
        return thenOn(IO::succeed,
                      fn,
                      executor
                     );

    }

    /**
     * Creates a new effect that will handle any failure that this effect might contain and will be recovered with a new
     * effect evaluated by the specified lambda. If the new effect fails again, the new failure is ignored, and the
     * original failure is returned (this is different from {@link #recoverWith(Lambda) recoverWith} which would return
     * the new failure).
     *
     * @param lambda the lambda to apply if this effect fails, producing a new effect.
     * @return a new effect representing either the original value or the result of applying the lambda in case of
     * failure.
     */
    public IO<O> fallbackTo(final Lambda<Throwable, O> lambda) {
        requireNonNull(lambda);

        return then(IO::succeed,
                    exc -> lambda.apply(exc)
                                 .then(IO::succeed,
                                       exc1 -> fail(exc)
                                      )
                   );

    }

    /**
     * Creates a new effect that will handle any failure that this effect might contain and will be recovered with a new
     * effect evaluated by the specified lambda in a thread from the given executor pool. If the new effect fails again,
     * the new failure is ignored, and the original failure is returned (this is different from
     * {@link #recoverWithOn(Lambda, Executor)} which would return the new failure).
     *
     * @param lambda   the lambda to apply if this effect fails, producing a new effect.
     * @param executor the executor responsible for evaluating the lambda.
     * @return a new effect representing either the original value or the result of applying the lambda in case of
     * failure.
     */
    public IO<O> fallbackToOn(final Lambda<Throwable, O> lambda,
                              final Executor executor
                             ) {
        requireNonNull(lambda);
        requireNonNull(executor);

        return thenOn(IO::succeed,
                      exc -> lambda.apply(exc)
                                   .thenOn(IO::succeed,
                                           exc1 -> fail(exc),
                                           executor
                                          ),
                      executor
                     );
    }

    /**
     * Creates a new effect that passes the exception to the specified failConsumer in case of failure. The given
     * consumer is responsible for handling the exception and can't fail itself. If it fails, the exception would be
     * just printed out on the console or handled in another appropriate manner.
     *
     * @param failConsumer the consumer that takes the exception in case of failure.
     * @return a new effect representing the original value or the failure with the exception passed to the consumer.
     */
    public IO<O> peekFailure(final Consumer<Throwable> failConsumer) {
        return peek($ -> {
                    },
                    failConsumer
                   );
    }

    /**
     * Creates a new effect that passes the computed value to the specified successConsumer in case of success. The
     * given consumer is responsible for handling the value and can't fail itself. If it fails, the exception would be
     * just printed out on the console or handled in another appropriate manner.
     *
     * @param successConsumer the consumer that takes the successful result in case of success.
     * @return a new effect representing the original value or the result of applying the consumer in case of success.
     */
    public IO<O> peekSuccess(final Consumer<O> successConsumer) {
        return peek(successConsumer,
                    e -> {
                    }
                   );
    }

    /**
     * Creates a new effect that passes the computed value to the specified successConsumer and any possible failure to
     * the specified failureConsumer. The given consumers are responsible for handling the value and failure,
     * respectively, and they can't fail themselves. If they fail, the exception would be just printed out on the
     * console or handled in another appropriate manner.
     *
     * @param successConsumer the consumer that takes the successful result.
     * @param failureConsumer the consumer that takes the failure.
     * @return a new effect representing the original value or the result of applying the consumers in case of success
     * or failure.
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
                Fun.publishException("peek", "Exception thrown by a consumer provided by the API client", exception);
            }
            return succeed(it);
        }, exc -> {
            try {
                failureConsumer.accept(exc);
            } catch (Exception exception) {
                Fun.publishException("peek", "Exception thrown by a consumer provided by the API client", exception);
            }
            return fail(exc);
        });
    }

    /**
     * Creates a new effect that passes the computed value to the specified successConsumer and any possible failure to
     * the specified failureConsumer. The given consumers are responsible for handling the value and failure,
     * respectively, and they can't fail themselves. If they fail, the exception would be just printed out on the
     * console.
     * <strong>The consumer tasks are performed by threads from the specified executor pool.</strong>
     *
     * @param successConsumer the consumer that takes the successful result.
     * @param failureConsumer the consumer that takes the failure.
     * @param executor        the executor responsible for performing the consumer tasks.
     * @return a new effect representing the original value or the result of applying the consumers in case of success
     * or failure.
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
                Fun.publishException("peekOn", "Exception thrown by the consumer provided by the API client", exception);
            }
            return succeed(it);
        }, exc -> {
            try {
                failureConsumer.accept(exc);
            } catch (Exception exception) {
                Fun.publishException("peekOn", "Exception thrown by the consumer provided by the API client", exception);
            }
            return fail(exc);
        }, executor);
    }

    /**
     * Creates a new effect that fails with a {@link TimeoutException} if this effect doesn't compute any value within
     * the specified time duration.
     *
     * @param time the time amount, which must be greater than 0.
     * @param unit the time unit representing the time duration.
     * @return a new effect representing the original value if computed within the timeout, or a failure with a
     * TimeoutException.
     * @throws IllegalArgumentException if the specified time is less than or equal to 0.
     */
    public IO<O> timeout(final int time,
                         final TimeUnit unit
                        ) {
        if (time <= 0) throw new IllegalArgumentException("time <= 0");
        requireNonNull(unit);
        return IO.effect(() -> requireNonNull(get()).orTimeout(time, unit));

    }

    /**
     * Creates a new effect that is evaluated to the given value if this effect doesn't terminate in the specified time.
     * In other words, a timeout failure is map to the specified default value.
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
        return IO.effect(() -> requireNonNull(get()).completeOnTimeout(defaultVal.get(),
                                                                       time,
                                                                       unit
                                                                      )
                        );

    }

    /**
     * Fires the evaluation of this effect and BLOCKS the caller thread, waiting for the computed value to be returned.
     * Any failure during the computation will be thrown as an exception.
     * <strong>Since this is a blocking call, it is typically used for testing purposes or
     * in situations where blocking is acceptable.</strong>
     *
     * @return the computed value.
     * @throws CompletionException if a failure occurs during the computation.
     */
    public O result() {
        return requireNonNull(get()).join();
    }

    /**
     * Creates a new effect that will retry the computation according to the specified {@link RetryPolicy policy} if
     * this effect fails and the failure satisfies the given predicate. If a delay before the retry is imposed by the
     * policy, a thread from the fork join pool will execute the retry; otherwise (delay is zero), the same thread as
     * the one computing this effect will execute the retry.
     *
     * @param predicate the predicate that determines if the failure should be retried.
     * @param policy    the retry policy specifying the retry behavior.
     * @return a new effect representing the original computation with retry behavior.
     * @see RetryPolicy
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
     * Creates a new effect that will retry the computation according to the specified {@link RetryPolicy policy} if
     * this effect fails and the failure satisfies the given predicate. If a delay before the retry is imposed by the
     * policy, a thread from the specified executor will execute the retry; otherwise (delay is zero), the same thread
     * as the one computing this effect will execute the retry.
     *
     * @param predicate the predicate that determines if the failure should be retried.
     * @param policy    the retry policy specifying the retry behavior.
     * @param executor  the executor used to perform the retry, if necessary.
     * @return a new effect representing the original computation with retry behavior.
     * @see RetryPolicy
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
     * Creates a new effect that will retry the computation according to the specified {@link RetryPolicy policy} if
     * this effect fails. If a delay before the retry is imposed by the policy, a thread from the specified executor
     * will execute the retry; otherwise (delay is zero), the same thread as the one computing this effect will execute
     * the retry.
     *
     * @param policy   the retry policy specifying the retry behavior.
     * @param executor the executor used to perform the retry, if necessary.
     * @return a new effect representing the original computation with retry behavior.
     * @see RetryPolicy
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
     * Creates a new effect that will retry the computation according to the specified {@link RetryPolicy policy} if
     * this effect fails. If a delay before the retry is imposed by the policy, a thread from the fork join pool will
     * execute the retry; otherwise (delay is zero), the same thread as the one computing this effect will execute the
     * retry.
     *
     * @param policy the retry policy specifying the retry behavior.
     * @return a new effect representing the original computation with retry behavior.
     * @see RetryPolicy
     */
    public IO<O> retry(final RetryPolicy policy) {
        return retry(e -> true, policy);
    }

    private IO<O> retry(IO<O> exp,
                        Function<RetryStatus, Optional<Duration>> policy,
                        RetryStatus rs,
                        Predicate<Throwable> predicate
                       ) {

        return exp.then(IO::succeed,
                        exc -> {
                            if (predicate.test(exc)) {
                                Optional<Duration> delayOpt = policy.apply(rs);
                                if (delayOpt.isEmpty()) return fail(exc);
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
                            return fail(exc);
                        }
                       );

    }

    private IO<O> retryOn(IO<O> exp,
                          Function<RetryStatus, Optional<Duration>> policy,
                          RetryStatus rs,
                          Predicate<Throwable> predicate,
                          Executor executor
                         ) {

        return exp.then(IO::succeed,
                        exc -> {
                            if (predicate.test(exc)) {
                                Optional<Duration> delayOpt = policy.apply(rs);
                                if (delayOpt.isEmpty()) return fail(exc);
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
                            return fail(exc);
                        }
                       );

    }

    /**
     * Creates a new effect that repeats the computation according to the specified {@link RetryPolicy policy} if the
     * result, when computed, satisfies the given predicate. If a delay before the retry is imposed by the policy,
     * thread from the fork join pool will execute the retry; otherwise (delay is zero), the same thread as the one
     * computing this effect will execute the retry.
     *
     * @param predicate the predicate that determines if the result should be computed again.
     * @param policy    the retry policy specifying the repeat behavior.
     * @return a new effect representing the original computation with repeat behavior.
     * @see RetryPolicy
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
     * Creates a new effect that repeats the computation according to the specified {@link RetryPolicy policy} if the
     * result, when computed, satisfies the given predicate. If a delay before the retry is imposed by the policy, a
     * thread from the given executor will execute the retry; otherwise (delay is zero), the same thread as the one
     * computing this effect will execute the retry.
     *
     * @param predicate the predicate that determines if the result should be computed again.
     * @param policy    the retry policy specifying the repeat behavior.
     * @param executor  the executor used to perform the repeat, if necessary.
     * @return a new effect representing the original computation with repeat behavior.
     * @see RetryPolicy
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
                if (delayOpt.isEmpty()) return succeed(o);
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
            return succeed(o);
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
                if (delayOpt.isEmpty()) return succeed(o);
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
            return succeed(o);
        });

    }

    /**
     * Creates a copy of this effect that generates an {@link jdk.jfr.consumer.RecordedEvent} from the result of the
     * computation and sends it to the Flight Recorder system. Customization of the event can be achieved using the
     * {@link #debug(EventBuilder)} method.
     *
     * @return a new effect with debugging enabled.
     * @see ExpEvent
     */
    public IO<O> debug() {
        return debug(EventBuilder.ofExp(getClass().getSimpleName()));
    }

    /**
     * Creates a copy of this effect that generates an {@link jdk.jfr.consumer.RecordedEvent} from the result of the
     * computation and sends it to the Flight Recorder system. Customization of the event can be achieved using the
     * provided {@link EventBuilder}.
     *
     * @param builder the builder used to customize the event.
     * @return a new effect with debugging enabled.
     * @see ExpEvent
     */
    public IO<O> debug(final EventBuilder<O> builder) {
        requireNonNull(builder);
        builder.exp = this.getClass().getSimpleName();
        return IO.lazy(ExpEvent::new)
                 .then(event -> this.peek(val -> builder.updateAndCommit(val, event),
                                          exc -> builder.updateAndCommit(exc, event)
                                         )
                      );
    }


}
