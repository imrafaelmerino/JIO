package jio;


import java.time.Duration;
import java.util.*;
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
 * @param <Output> the type of the result returned by the computation when it succeeds.
 * @see CompletableFuture
 * @see CompletionException
 * @see java.util.concurrent.Executor
 * @see ForkJoinPool.ManagedBlocker
 * @see RetryPolicy
 * @see EventBuilder
 * @see EvalExpEvent
 * @see Val
 * @see Delay
 * @see Exp
 */

public sealed abstract class IO<Output> implements Supplier<CompletableFuture<Output>> permits Delay, Exp,
                                                                                               Val {

  /**
   * Effect that always succeed with true
   */
  public static final IO<Boolean> TRUE = succeed(true);
  /**
   * Effect that always succeed with false
   */
  public static final IO<Boolean> FALSE = succeed(false);

  IO() {
  }

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
   * @param <Output> the type parameter that represents the result type of the null effect.
   * @return an effect that produces a null result.
   */
  public static <Output> IO<Output> NULL() {
    return IO.succeed(null);
  }

  /**
   * Creates an effect from a lazy computation that returns a CompletableFuture. This method allows you to encapsulate
   * an asynchronous operation represented by a lazy future into an IO effect.
   *
   * @param effect   the lazy future that produces a CompletableFuture.
   * @param <Output> the type parameter representing the result type of the CompletableFuture.
   * @return an IO effect that wraps the provided lazy effect.
   */
  public static <Output> IO<Output> effect(final Supplier<CompletableFuture<Output>> effect) {
    return new Val<>(requireNonNull(effect));
  }

  /**
   * Creates an effect from a callable that returns a closable resource and maps it into an effect. This method is
   * designed to handle resources that implement the {@link AutoCloseable} interface, ensuring proper resource
   * management to avoid memory leaks.
   *
   * @param callable the resource supplier that provides the closable resource.
   * @param map      the map function that transforms the resource into an effect.
   * @param <Output> the type parameter representing the result type of the effect.
   * @param <Input>  the type parameter representing the type of the resource.
   * @return an IO effect.
   */
  public static <Output, Input extends AutoCloseable> IO<Output> resource(final Callable<? extends Input> callable,
                                                                          final Lambda<? super Input, Output> map
                                                                         ) {
    return IO.task(callable)
             .then(resource -> map.apply(resource)
                                  .then(success -> {
                                          try {
                                            resource.close();
                                            return IO.succeed(success);
                                          } catch (Exception e) {
                                            return IO.fail(e);
                                          }
                                        },
                                        failure -> {
                                          try {
                                            resource.close();
                                            return IO.fail(failure);
                                          } catch (Exception e) {
                                            return IO.fail(e);
                                          }
                                        })
                  );

  }


  /**
   * Creates an effect that always succeeds and returns the same value.
   *
   * @param val      the value to be returned by the effect. Null values are allowed.
   * @param <Output> the type parameter representing the result type of the effect.
   * @return an IO effect that always succeeds with the specified value.
   */
  public static <Output> IO<Output> succeed(final Output val) {
    return new Val<>(() -> CompletableFuture.completedFuture(val));
  }


  /**
   * Creates an effect from a lazy computation. Every time the `get()` or `result` method is called, the provided
   * supplier is invoked, and a new computation is returned. Since a supplier cannot throw exceptions, an alternative
   * constructor {@link #task(Callable)} is available that takes a {@link Callable callable} instead of a
   * {@link Supplier supplier} if exception handling is needed.
   *
   * @param supplier the supplier representing the lazy computation.
   * @param <Output> the type parameter representing the result type of the effect.
   * @return an IO effect encapsulating the lazy computation.
   */
  public static <Output> IO<Output> lazy(final Supplier<? extends Output> supplier) {
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
   * Creates an effect from a task modeled with a {@link Callable}. Every time the `get()` or `result` method is called,
   * the provided task is executed.
   *
   * @param callable the callable task to be executed.
   * @param <Output> the type parameter representing the result type of the effect.
   * @return an IO effect encapsulating the callable task.
   */
  public static <Output> IO<Output> task(final Callable<? extends Output> callable) {
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
   * Creates an effect from a task modeled with a {@link Callable} and executes it using the specified {@link Executor}.
   * Every time the `get()` or `result` method is called, the provided task is executed asynchronously using the given
   * executor.
   *
   * @param callable the callable task to be executed.
   * @param executor the executor responsible for running the task.
   * @param <Output> the type parameter representing the result type of the effect.
   * @return an IO effect encapsulating the callable task.
   */
  public static <Output> IO<Output> task(final Callable<? extends Output> callable,
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
   * @param exc      the exception to be returned by the effect.
   * @param <Output> the type parameter representing the result type of the effect (in this case, typically representing
   *                 an exception).
   * @return an IO effect that returns the specified exception as its result.
   */
  public static <Output> IO<Output> fail(final Throwable exc) {
    requireNonNull(exc);
    return new Val<>(() -> CompletableFuture.failedFuture(exc));

  }

  /**
   * Creates an effect from a lazy computation modeled with a {@link Supplier} and executes it asynchronously using the
   * specified {@link Executor}. Every time the `get()` or `result` method is called, the provided lazy computation is
   * executed asynchronously using the given executor.
   *
   * @param supplier the supplier representing the lazy computation to be executed.
   * @param executor the executor responsible for running the computation.
   * @param <Output> the type parameter representing the result type of the effect.
   * @return an IO effect encapsulating the lazy computation executed with the specified executor.
   */
  public static <Output> IO<Output> lazy(final Supplier<Output> supplier,
                                         final Executor executor
                                        ) {
    requireNonNull(supplier);
    requireNonNull(executor);
    return new Val<>(() -> CompletableFuture.supplyAsync(supplier,
                                                         executor
                                                        ));

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
   * the "java.util.concurrent.ForkJoinPool.common.maximumSpares" system property. Exceeding this limit may result in a
   * {@link RejectedExecutionException} exception with the message "Thread limit exceeded replacing blocked worker."
   *
   * @param supplier the supplier representing the computation.
   * @param <Output> the type parameter representing the result type of the effect.
   * @return an IO effect encapsulating the computation, utilizing the ForkJoin pool if available.
   */
  public static <Output> IO<Output> managedLazy(final Supplier<? extends Output> supplier) {
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
   * the "java.util.concurrent.ForkJoinPool.common.maximumSpares" system property. Exceeding this limit may result in a
   * {@link RejectedExecutionException} exception with the message "Thread limit exceeded replacing blocked worker."
   *
   * @param task     the callable task to be executed.
   * @param <Output> the type parameter representing the result type of the effect.
   * @return an IO effect encapsulating the callable task, utilizing the ForkJoin pool if available.
   */
  public static <Output> IO<Output> managedTask(final Callable<? extends Output> task) {
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
   * method. It's important to note that this operator is a good candidate to be reimplemented with Fibers in the future
   * to benefit from cancellation and non-returning failing computations.
   *
   * @param first    the first computation.
   * @param others   the rest of the computations.
   * @param <Output> the type of the computation.
   * @return a new computation representing the first to complete among the provided computations.
   */
  @SafeVarargs
  @SuppressWarnings({"unchecked", "varargs"})
  public static <Output> IO<Output> race(final IO<? extends Output> first,
                                         final IO<? extends Output>... others) {
    requireNonNull(first);
    requireNonNull(others);
    List<IO<? extends Output>> list = new ArrayList<>();
    list.add(first);
    if (others.length > 0) {
      List<IO<? extends Output>> c = Arrays.stream(others)
                                           .toList();
      list.addAll(c);
    }
    return IO.effect(() -> {
                       var cfs = list.stream()
                                     .map(Supplier::get)
                                     .toArray(CompletableFuture[]::new);
                       return CompletableFuture.anyOf(cfs)
                                               .thenApply(it -> ((Output) it));
                     }
                    );

  }

  /**
   * The `async` method allows you to execute an action without waiting for its result and returns immediately. It is
   * useful when you are not interested in the outcome of the action and want to trigger it asynchronously.
   *
   * <p><b>Note:</b> To achieve non-blocking behavior, ensure that the caller thread and the thread
   * executing the action are different.
   *
   * @return An effect representing the asynchronous execution of the action, producing no meaningful result.
   */
  @SuppressWarnings("ReturnValueIgnored")
  public IO<Void> async() {
    return IO.NULL()
             .then(nill -> {
               get();
               return IO.NULL();
             });
  }


  /**
   * Creates a new effect that, when this succeeds, maps the computed value into another value using the specified
   * function.
   *
   * @param fn             the mapping function that transforms the result of this effect.
   * @param <OutputMapped> the result type of the new effect.
   * @return a new effect that represents the mapped result.
   */
  public <OutputMapped> IO<OutputMapped> map(final Function<? super Output, ? extends OutputMapped> fn) {
    requireNonNull(fn);
    return IO.effect(() -> requireNonNull(get()).thenApply(fn));
  }

  /**
   * Maps failures (exceptions) that may occur during the execution of the IO operation. This method allows you to apply
   * a function to transform or handle the failure in a custom way. The original exception is replaced with the result
   * of applying the provided function.
   *
   * <p>The mapping function {@code fn} is applied only if the original IO operation results in a failure. If the IO
   * operation succeeds, the result is unchanged.</p>
   *
   * <p>This operation creates a new IO operation with the same behavior as the original one, except for the handling
   * of failures as modified by the mapping function.</p>
   *
   * @param mappingFunction The function to apply to the failure. It takes the original exception and returns the
   *                        transformed exception.
   * @return A new IO operation with the same result type, where failures are transformed using the provided function.
   * @throws NullPointerException If the mapping function {@code fn} is {@code null}.
   * @see CompletableFuture#exceptionallyCompose(java.util.function.Function)
   */
  public IO<Output> mapFailure(final Function<Throwable, Throwable> mappingFunction) {
    requireNonNull(mappingFunction);
    return IO.effect(() -> requireNonNull(get())
                         .exceptionallyCompose(exc -> CompletableFuture.failedFuture(mappingFunction.apply(exc)))
                    );
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
  public <Q> IO<Q> then(final Lambda<? super Output, Q> fn) {
    requireNonNull(fn);
    return effect(() -> requireNonNull(get()).thenCompose(it -> fn.apply(it)
                                                                  .get()
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

  public <Q> IO<Q> then(final Lambda<? super Output, Q> successLambda,
                        final Lambda<? super Throwable, Q> failureLambda
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
   * Creates a new effect that will handle any failure that this effect might contain and will be recovered with the
   * value evaluated by the specified function. If this effect succeeds, the new effect will also succeed with the same
   * value. If this effect fails, the specified function is applied to the exception to produce a new value for the new
   * effect.
   *
   * @param fn the function to apply if this effect fails, taking the exception as input.
   * @return a new effect representing the original value or the result of applying the function in case of failure.
   */
  public IO<Output> recover(final Function<? super Throwable, Output> fn) {
    requireNonNull(fn);
    return then(IO::succeed,
                exc -> succeed(fn.apply(exc)));
  }

  /**
   * Creates a new effect that will handle any failure that this effect might contain and will be recovered with the
   * effect evaluated by the specified lambda. If this effect succeeds, the new effect will also succeed with the same
   * value. If this effect fails, the specified lambda is applied to the exception to produce a new effect for the new
   * effect.
   *
   * @param lambda the lambda to apply if this effect fails, taking the exception as input and producing a new effect.
   * @return a new effect representing the original value or the result of applying the lambda in case of failure.
   */
  public IO<Output> recoverWith(final Lambda<? super Throwable, Output> lambda) {
    requireNonNull(lambda);
    return then(IO::succeed,
                lambda
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
  public IO<Output> fallbackTo(final Lambda<? super Throwable, Output> lambda) {
    requireNonNull(lambda);

    return then(IO::succeed,
                exc -> lambda.apply(exc)
                             .then(IO::succeed,
                                   exc1 -> fail(exc)
                                  )
               );

  }


  /**
   * Creates a new effect that passes the exception to the specified failConsumer in case of failure. The given consumer
   * is responsible for handling the exception and can't fail itself. If it fails, the exception would be just printed
   * out on the console or handled in another appropriate manner.
   *
   * @param failConsumer the consumer that takes the exception in case of failure.
   * @return a new effect representing the original value or the failure with the exception passed to the consumer.
   */
  public IO<Output> peekFailure(final Consumer<? super Throwable> failConsumer) {
    return peek($ -> {
                },
                failConsumer
               );
  }

  /**
   * Creates a new effect that passes the computed value to the specified successConsumer in case of success. The given
   * consumer is responsible for handling the value and can't fail itself. If it fails, the exception would be just
   * printed out on the console or handled in another appropriate manner.
   *
   * @param successConsumer the consumer that takes the successful result in case of success.
   * @return a new effect representing the original value or the result of applying the consumer in case of success.
   */
  public IO<Output> peekSuccess(final Consumer<? super Output> successConsumer) {
    return peek(successConsumer,
                e -> {
                }
               );
  }

  /**
   * Creates a new effect that passes the computed value to the specified successConsumer and any possible failure to
   * the specified failureConsumer. The given consumers are responsible for handling the value and failure,
   * respectively, and they can't fail themselves. If they fail, the exception would be just printed out on the console
   * or handled in another appropriate manner.
   *
   * @param successConsumer the consumer that takes the successful result.
   * @param failureConsumer the consumer that takes the failure.
   * @return a new effect representing the original value or the result of applying the consumers in case of success or
   * failure.
   */
  public IO<Output> peek(final Consumer<? super Output> successConsumer,
                         final Consumer<? super Throwable> failureConsumer
                        ) {
    requireNonNull(successConsumer);
    requireNonNull(failureConsumer);
    return then(it -> {
                  try {
                    successConsumer.accept(it);
                  } catch (Exception exception) {
                    Fun.publishException("peek",
                                         "Exception thrown by a consumer provided by the API client",
                                         exception);
                  }
                  return succeed(it);
                },
                exc -> {
                  try {
                    failureConsumer.accept(exc);
                  } catch (Exception exception) {
                    Fun.publishException("peek",
                                         "Exception thrown by a consumer provided by the API client",
                                         exception);
                  }
                  return fail(exc);
                });
  }


  /**
   * Creates a new effect that fails with a {@link TimeoutException} if this effect doesn't compute any value within the
   * specified time duration.
   *
   * @param time the time amount, which must be greater than 0.
   * @param unit the time unit representing the time duration.
   * @return a new effect representing the original value if computed within the timeout, or a failure with a
   * TimeoutException.
   * @throws IllegalArgumentException if the specified time is less than or equal to 0.
   */
  public IO<Output> timeout(final long time,
                            final TimeUnit unit
                           ) {
    if (time <= 0) {
      throw new IllegalArgumentException("time <= 0");
    }
    requireNonNull(unit);
    return IO.effect(() -> requireNonNull(get()).orTimeout(time,
                                                           unit));

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
  public IO<Output> timeoutOrElse(final long time,
                                  final TimeUnit unit,
                                  final Supplier<Output> defaultVal
                                 ) {
    if (time <= 0) {
      throw new IllegalArgumentException("time <= 0");
    }
    requireNonNull(unit);
    requireNonNull(defaultVal);
    return IO.effect(() -> requireNonNull(get()).completeOnTimeout(defaultVal.get(),
                                                                   time,
                                                                   unit
                                                                  )
                    );

  }


  /**
   * Creates a new effect that will retry the computation according to the specified {@link RetryPolicy policy} if this
   * effect fails and the failure satisfies the given predicate. If a delay before the retry is imposed by the policy, a
   * thread from the fork join pool will execute the retry; otherwise (delay is zero), the same thread as the one
   * computing this effect will execute the retry.
   *
   * @param predicate the predicate that determines if the failure should be retried.
   * @param policy    the retry policy specifying the retry behavior.
   * @return a new effect representing the original computation with retry behavior.
   * @see RetryPolicy
   */
  public IO<Output> retry(final Predicate<? super Throwable> predicate,
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
   * Creates a new effect that will retry the computation according to the specified {@link RetryPolicy policy} if this
   * effect fails. If a delay before the retry is imposed by the policy, a thread from the fork join pool will execute
   * the retry; otherwise (delay is zero), the same thread as the one computing this effect will execute the retry.
   *
   * @param policy the retry policy specifying the retry behavior.
   * @return a new effect representing the original computation with retry behavior.
   * @see RetryPolicy
   */
  public IO<Output> retry(final RetryPolicy policy) {
    return retry(e -> true,
                 policy);
  }

  private IO<Output> retry(IO<Output> exp,
                           Function<RetryStatus, Optional<Duration>> policy,
                           RetryStatus rs,
                           Predicate<? super Throwable> predicate
                          ) {

    return exp.then(IO::succeed,
                    exc -> {
                      if (predicate.test(exc)) {
                        Optional<Duration> delayOpt = policy.apply(rs);
                        if (delayOpt.isEmpty()) {
                          return fail(exc);
                        }
                        Duration duration = delayOpt.get();

                        if (duration.isZero()) {
                          return retry(exp,
                                       policy,
                                       new RetryStatus(rs.counter() + 1,
                                                       rs.cumulativeDelay(),
                                                       Duration.ZERO
                                       ),
                                       predicate
                                      );
                        }
                        return Delay.of(duration)
                                    .then($ -> retry(exp,
                                                     policy,
                                                     new RetryStatus(rs.counter() + 1,
                                                                     rs.cumulativeDelay()
                                                                       .plus(duration),
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


  /**
   * Creates a new effect that repeats the computation according to the specified {@link RetryPolicy policy} if the
   * result, when computed, satisfies the given predicate. If a delay before the retry is imposed by the policy, thread
   * from the fork join pool will execute the retry; otherwise (delay is zero), the same thread as the one computing
   * this effect will execute the retry.
   *
   * @param predicate the predicate that determines if the result should be computed again.
   * @param policy    the retry policy specifying the repeat behavior.
   * @return a new effect representing the original computation with repeat behavior.
   * @see RetryPolicy
   */
  public IO<Output> repeat(final Predicate<? super Output> predicate,
                           final RetryPolicy policy
                          ) {
    return repeat(this,
                  requireNonNull(policy),
                  RetryStatus.ZERO,
                  requireNonNull(predicate)
                 );

  }


  private IO<Output> repeat(IO<Output> exp,
                            RetryPolicy policy,
                            RetryStatus rs,
                            Predicate<? super Output> predicate
                           ) {

    return exp.then(output -> {
      if (predicate.test(output)) {
        Optional<Duration> delayOpt = policy.apply(rs);
        if (delayOpt.isEmpty()) {
          return succeed(output);
        }
        Duration duration = delayOpt.get();
        if (duration.isZero()) {
          return repeat(exp,
                        policy,
                        new RetryStatus(rs.counter() + 1,
                                        rs.cumulativeDelay(),
                                        Duration.ZERO
                        ),
                        predicate
                       );
        }
        return Delay.of(duration)
                    .then($ -> repeat(exp,
                                      policy,
                                      new RetryStatus(rs.counter() + 1,
                                                      rs.cumulativeDelay()
                                                        .plus(duration),
                                                      duration
                                      ),
                                      predicate
                                     )

                         );
      }
      return succeed(output);
    });

  }

  /**
   * Creates a copy of this effect that generates an {@link jdk.jfr.consumer.RecordedEvent} from the result of the
   * computation and sends it to the Flight Recorder system. Customization of the event can be achieved using the
   * {@link #debug(EventBuilder)} method.
   *
   * @return a new effect with debugging enabled.
   * @see EvalExpEvent
   */
  public IO<Output> debug() {
    return debug(EventBuilder.of(getClass().getSimpleName()));
  }

  /**
   * Creates a copy of this effect that generates an {@link jdk.jfr.consumer.RecordedEvent} from the result of the
   * computation and sends it to the Flight Recorder system. Customization of the event can be achieved using the
   * provided {@link EventBuilder}.
   *
   * @param builder the builder used to customize the event.
   * @return a new effect with debugging enabled.
   * @see EvalExpEvent
   */
  public IO<Output> debug(final EventBuilder<Output> builder) {
    requireNonNull(builder);
    return IO.lazy(() -> {
               EvalExpEvent expEvent = new EvalExpEvent();
               expEvent.begin();
               return expEvent;
             })
             .then(event -> this.peek(val -> {
                                        event.end();
                                        builder.updateAndCommit(val,
                                                                event);
                                      },
                                      exc -> {
                                        event.end();
                                        builder.updateAndCommit(exc,
                                                                event);
                                      }
                                     )
                  );
  }

  /**
   * Sleeps for the specified duration before evaluating this effect.
   * <p>This method introduces a pause in the execution flow for the specified duration using the
   * {@link Delay#of(Duration)} effect.It can be useful for testing purposes. However, it should be used with caution,
   * as introducing delays in a program can impact performance and behavior.
   *
   * @param duration The duration to sleep for.
   * @return An {@code IO<Output>} representing the delayed operation.
   */
  public IO<Output> sleep(final Duration duration) {
    Objects.requireNonNull(duration);
    return Delay.of(duration)
                .then(it -> this);

  }


  /**
   * Computes this effect and passes the result to the specified consumer or handles the exception using the provided
   * exception handler. Internally, it creates a new CompletionStage with the same result or exception as this stage,
   * that executes the given consumer or exception handler when this IO effect completes.
   *
   * @param successConsumer The consumer to be called with the result value if the operation succeeds.
   * @param failureConsumer The handler to be called with the exception if the operation fails.
   */
  public void onResult(final Consumer<Output> successConsumer,
                       final Consumer<Throwable> failureConsumer
                      ) {
    requireNonNull(successConsumer);
    requireNonNull(failureConsumer);
    var unused = get().whenComplete((value, exc) -> {
      if (exc == null) {
        successConsumer.accept(value);
      } else {
        failureConsumer.accept(exc);
      }
    });
    assert unused != null;
  }

  /**
   * Blocks the caller thread to wait for the result of this effect. Any failure during the computation will be thrown
   * as an exception.
   * <p>
   * This method is a blocking call and is commonly used for testing purposes or in situations where blocking is
   * acceptable. When using virtual threads, it is completely normal to call this method.
   * </p>
   *
   * @return the computed value.
   * @throws Exception if a failure occurs during the computation.
   * @see #join()
   */
  public Output result() throws Exception {
    try {
      return get().join();
    } catch (CompletionException e) {
      Throwable cause = e.getCause();
      if (cause != null) {
        throw ((Exception) cause);
      }
      throw e;
    }
  }


  /**
   * Returns the result value when this effect completes or throws an (unchecked) exception if completed exceptionally.
   * Concretely, this method throws an (unchecked) CompletionException with the underlying exception as its cause.
   *
   * @return the computed value.
   */
  public Output join() {
    return get().join();
  }

}
