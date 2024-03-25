package jio;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.StructuredTaskScope.ShutdownOnSuccess;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import jdk.jfr.consumer.RecordedEvent;
import jio.Result.Failure;
import jio.Result.Success;

/**
 * Represents a functional effect that encapsulates computations, including successful results and failures. This class
 * models a computation that returns a {@link Result} of type `Output`, where `Output` is the type of the result when
 * the computation succeeds. Functional effects are used to model IO operations in a composable and error-handling
 * manner.
 *
 * <p>
 * A computation can either succeed, in which case the returned {@link Result} is a {@link Success}, or it can fail, in
 * which case the result is a {@link Failure} holding the original exception that caused the failure.
 *
 * <p>
 * Functional effects are typically created using various factory methods provided by this class. These factory methods
 * allow you to create effects from different types of computations, such as lazy computations, tasks, resources,
 * futures and more.
 *
 * <p>
 * Functional effects support a wide range of operations for composing, transforming, and handling asynchronous
 * computations. These operations include mapping, flat mapping, error handling, retries, timeouts, and debugging.
 *
 * <p>
 * Functional effects are a powerful tool for modeling and handling IO operations in a composable way while providing
 * robust error handling and retry capabilities.
 *
 * @param <Output> the type of the result returned by the computation when it succeeds.
 * @see RetryPolicy
 * @see EventBuilder
 * @see EvalExpEvent
 * @see Val
 * @see Exp
 */

public sealed abstract class IO<Output> implements Callable<Result<Output>> permits Exp, Val {

  /**
   * Effect that always succeeds with true
   */
  public static final IO<Boolean> TRUE = succeed(true);
  /**
   * Effect that always succeeds with false
   */
  public static final IO<Boolean> FALSE = succeed(false);

  IO() {
  }

  /**
   * Creates an effect that always produces a result of null. This method is generic and captures the type of the
   * caller, allowing you to create null effects with different result types.

   * {@snippet :
   * IO<String> a = NULL();
   * IO<Integer> b = NULL();
   *}
   *
   * @param <Output> the type parameter that represents the result type of the null effect.
   * @return an effect that produces a null result.
   */
  public static <Output> IO<Output> NULL() {
    return IO.succeed(null);
  }

  /**
   * Creates an effect from a lazy computation that returns a Future. This method allows you to encapsulate an
   * asynchronous operation represented by a lazy future into an IO effect.
   *
   * @param effect   the lazy future that produces a Future.
   * @param <Output> the type parameter representing the result type of the CompletableFuture.
   * @return an IO effect that wraps the provided lazy effect.
   */
  public static <Output> IO<Output> effect(final Supplier<Future<Output>> effect) {
    return new Val<>(() -> {
      try {
        return new Success<>(effect.get()
                                   .get());
      } catch (Exception e) {
        return new Failure<>(e);
      }
    });
  }

  /**
   * Creates an effect from a callable that returns a closable resource and maps the resource into an effect. This
   * method is designed to handle resources that implement the {@link AutoCloseable} interface, ensuring proper resource
   * management to avoid memory leaks.
   *
   * @param callable the resource supplier that provides the closable resource.
   * @param map      the map function that transforms the resource into an effect.
   * @param <Output> the type parameter representing the result type of the effect.
   * @param <Input>  the type parameter representing the type of the resource.
   * @return an IO effect.
   */
  public static <Output, Input extends AutoCloseable> IO<Output> resource(final Callable<? extends Input> callable,
                                                                          final Lambda<? super Input, Output> map) {
    requireNonNull(map);
    requireNonNull(callable);
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
                                        }));

  }

  /**
   * Creates an effect that always succeeds and returns the same output.
   *
   * @param val      the output to be returned by the effect. Null values are allowed.
   * @param <Output> the type parameter representing the result type of the effect.
   * @return an IO effect that always succeeds with the specified output.
   */
  public static <Output> IO<Output> succeed(final Output val) {
    return new Val<>(() -> new Success<>(val));
  }

  /**
   * Creates an effect from a lazy computation. Every time the `compute()` method is called, the provided supplier is
   * invoked, and a new computation is returned. Since a supplier cannot throw exceptions, an alternative constructor
   * {@link #task(Callable)} is available that takes a {@link Callable callable} instead of a {@link Supplier supplier}
   * if exception handling is needed.
   *
   * @param supplier the supplier representing the lazy computation.
   * @param <Output> the type parameter representing the result type of the effect.
   * @return an IO effect encapsulating the lazy computation.
   */
  public static <Output> IO<Output> lazy(final Supplier<? extends Output> supplier) {
    requireNonNull(supplier);
    return new Val<>(() -> {
      try {
        return new Success<>(supplier.get());
      } catch (Exception e) {
        return new Failure<>(e);
      }
    });
  }

  /**
   * Creates an effect from a task modeled with a {@link Callable}. Every time the `compute()` method is called, the
   * provided task is executed.
   *
   * @param callable the callable task to be executed.
   * @param <Output> the type parameter representing the result type of the effect.
   * @return an IO effect encapsulating the callable task.
   */
  public static <Output> IO<Output> task(final Callable<? extends Output> callable) {
    requireNonNull(callable);
    return new Val<>(() -> {
      try {
        return new Success<>(callable.call());
      } catch (Exception e) {
        return new Failure<>(e);
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
  public static <Output> IO<Output> fail(final Exception exc) {
    requireNonNull(exc);
    return new Val<>(() -> new Failure<>(exc));

  }

  /**
   * Returns the first computation that completes successfully
   *
   * @param first    the first computation.
   * @param others   the rest of the computations.
   * @param <Output> the type of the computation.
   * @return a new computation representing the first to complete among the provided computations.
   */
  @SafeVarargs
  public static <Output> IO<Output> race(final IO<Output> first,
                                         final IO<Output>... others) {
    requireNonNull(first);
    requireNonNull(others);
    List<IO<Output>> tasks = new ArrayList<>();
    tasks.add(first);
    if (others.length > 0) {
      List<IO<Output>> c = Arrays.stream(others)
                                 .toList();
      tasks.addAll(c);
    }
    return new Val<>(() -> {
      try (var scope = new ShutdownOnSuccess<Result<Output>>()) {
        for (var task : tasks) {
          scope.fork(task);
        }
        try {
          return scope.join()
                      .result();
        } catch (Exception e) {
          return new Failure<>(e);
        }
      }
    });

  }

  /**
   * The `async` method allows you to compute this effect without waiting for its result and returns immediately. It is
   * useful when you are not interested in the outcome of the action and want to trigger it asynchronously.
   *
   * @return An effect representing the asynchronous execution of this effect, producing no meaningful result.
   */
  @SuppressWarnings("ReturnValueIgnored")
  public IO<Void> async() {
    var unused = VirtualThreadExecutor.INSTANCE.submit(this);
    return IO.NULL();
  }

  /**
   * Creates a new effect that, when this succeeds, maps the computed output into another output using the specified
   * function.
   *
   * @param fn             the mapping function that transforms the result of this effect.
   * @param <OutputMapped> the result type of the new effect.
   * @return a new effect that represents the mapped result.
   */
  public <OutputMapped> IO<OutputMapped> map(final Function<? super Output, ? extends OutputMapped> fn) {
    requireNonNull(fn);
    return new Val<>(() -> {
      try {
        Result<Output> result = call();
        return switch (result) {
          case Success<Output>(Output output) -> new Success<>(fn.apply(output));
          case Failure<Output>(Exception exception) -> new Failure<>(exception);
        };
      } catch (Exception e) { //fn can fail!
        return new Failure<>(e);
      }
    });
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
   * @param fn The function to apply to the failure. It takes the original exception and returns the transformed
   *           exception.
   * @return A new IO operation with the same result type, where failures are transformed using the provided function.
   */
  public IO<Output> mapFailure(final Function<Exception, Exception> fn) {
    requireNonNull(fn);
    return new Val<>(
        () -> {
          try {
            return switch (call()) {
              case Success<Output> s -> s;
              case Failure<Output>(Exception exception) -> new Failure<>(fn.apply(exception));
            };
          } catch (Exception e) {
            return new Failure<>(e);
          }
        });
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
    return new Val<>(() -> {
      try {
        return switch (call()) {
          case Success<Output>(Output output) -> fn.apply(output)
                                                   .call();
          case Failure<Output>(Exception e) -> new Failure<>(e);
        };
      } catch (Exception e) {
        return new Failure<>(e);
      }
    });
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
                        final Lambda<? super Exception, Q> failureLambda) {
    requireNonNull(successLambda);
    requireNonNull(failureLambda);
    return new Val<>(() -> {
      try {
        Result<Output> result = call();
        return switch (result) {
          case Success<Output>(Output output) -> successLambda.apply(output)
                                                              .call();
          case Failure<Output>(Exception exception) -> failureLambda.apply(exception)
                                                                    .call();
        };
      } catch (Exception e) {
        return new Failure<>(e);
      }
    });
  }

  /**
   * Creates a new effect that will handle any failure that this effect might contain, and will be recovered with the
   * output evaluated by the specified function. If this effect succeeds, the new effect will also succeed with the same
   * output. If this effect fails, the specified function is applied to the exception to produce a new output for the
   * new effect.
   *
   * @param fn the function to apply if this effect fails, taking the exception as input.
   * @return a new effect representing the original output or the result of applying the function in case of failure.
   */
  public IO<Output> recover(final Function<? super Exception, Output> fn) {
    requireNonNull(fn);
    return new Val<>(() -> {
      try {
        return switch (call()) {
          case Success<Output> success -> success;
          case Failure<Output>(Exception exception) -> new Success<>(fn.apply(exception));
        };
      } catch (Exception e) {
        return new Failure<>(e);
      }
    });

  }

  /**
   * Creates a new effect that will handle any failure that this effect might contain and will be recovered with the
   * effect evaluated by the specified lambda. If this effect succeeds, the new effect will also succeed with the same
   * output. If this effect fails, the specified lambda is applied to the exception to produce a new effect for the new
   * effect.
   *
   * @param lambda the lambda to apply if this effect fails, taking the exception as input and producing a new effect.
   * @return a new effect representing the original output or the result of applying the lambda in case of failure.
   */
  public IO<Output> recoverWith(final Lambda<? super Throwable, Output> lambda) {
    requireNonNull(lambda);
    return then(IO::succeed,
                lambda);
  }

  /**
   * Creates a new effect that will handle any failure that this effect might contain and will be recovered with a new
   * effect evaluated by the specified lambda. If the new effect fails again, the new failure is ignored, and the
   * original failure is returned (this is different from {@link #recoverWith(Lambda) recoverWith} which would return
   * the new failure).
   *
   * @param lambda the lambda to apply if this effect fails, producing a new effect.
   * @return a new effect representing either the original output or the result of applying the lambda in case of
   * failure.
   */
  public IO<Output> fallbackTo(final Lambda<? super Throwable, Output> lambda) {
    requireNonNull(lambda);

    return then(IO::succeed,
                exc -> lambda.apply(exc)
                             .then(IO::succeed,
                                   _ -> fail(exc)));

  }

  /**
   * Creates a new effect that passes the exception to the specified failConsumer in case of failure. The given consumer
   * is responsible for handling the exception and can't fail itself. If it fails, the exception would be just printed
   * out on the console or handled in another appropriate manner.
   *
   * @param failConsumer the consumer that takes the exception in case of failure.
   * @return a new effect representing the original output or the failure with the exception passed to the consumer.
   */
  public IO<Output> peekFailure(final Consumer<? super Throwable> failConsumer) {
    return peek(_ -> {
                },
                failConsumer);
  }

  /**
   * Creates a new effect that passes the computed output to the specified successConsumer in case of success. The given
   * consumer is responsible for handling the output and can't fail itself. If it fails, the exception would be just
   * printed out on the console or handled in another appropriate manner.
   *
   * @param successConsumer the consumer that takes the successful result in case of success.
   * @return a new effect representing the original output or the result of applying the consumer in case of success.
   */
  public IO<Output> peekSuccess(final Consumer<? super Output> successConsumer) {
    return peek(successConsumer,
                _ -> {
                });
  }

  /**
   * Creates a new effect that passes the computed output to the specified successConsumer and any possible failure to
   * the specified failureConsumer. The given consumers are responsible for handling the output and failure,
   * respectively, and they can't fail themselves. If they fail, the exception would be just printed out on the console
   * or handled in another appropriate manner.
   *
   * @param successConsumer the consumer that takes the successful result.
   * @param failureConsumer the consumer that takes the failure.
   * @return a new effect representing the original output or the result of applying the consumers in case of success or
   * failure.
   */
  public IO<Output> peek(final Consumer<? super Output> successConsumer,
                         final Consumer<? super Throwable> failureConsumer) {
    requireNonNull(successConsumer);
    requireNonNull(failureConsumer);
    return then(it -> {
                  try {
                    successConsumer.accept(it);
                  } catch (Exception exception) {
                    Fun.publishException("peek",
                                         exception);
                  }
                  return succeed(it);
                },
                exc -> {
                  try {
                    failureConsumer.accept(exc);
                  } catch (Exception exception) {
                    Fun.publishException("peek",
                                         exception);
                  }
                  return fail(exc);
                });
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
                          final RetryPolicy policy) {
    requireNonNull(predicate);
    requireNonNull(policy);
    return retry(this,
                 policy,
                 RetryStatus.ZERO,
                 predicate);

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
    return retry(_ -> true,
                 policy);
  }

  private IO<Output> retry(IO<Output> effect,
                           Function<RetryStatus, Duration> policy,
                           RetryStatus rs,
                           Predicate<? super Throwable> predicate) {

    return effect.then(IO::succeed,
                       exc -> {
                         if (predicate.test(exc)) {
                           Duration duration = policy.apply(rs);
                           if (duration == null) {
                             return fail(exc);
                           }
                           if (duration.isZero()) {
                             return retry(effect,
                                          policy,
                                          new RetryStatus(rs.counter() + 1,
                                                          rs.cumulativeDelay(),
                                                          Duration.ZERO),
                                          predicate);
                           }
                           Fun.sleep(duration);
                           return retry(effect,
                                        policy,
                                        new RetryStatus(rs.counter() + 1,
                                                        rs.cumulativeDelay()
                                                          .plus(duration),
                                                        duration),
                                        predicate

                                       );
                         }
                         return fail(exc);
                       });

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
                           final RetryPolicy policy) {
    return repeat(this,
                  requireNonNull(policy),
                  RetryStatus.ZERO,
                  requireNonNull(predicate));

  }

  private IO<Output> repeat(IO<Output> exp,
                            RetryPolicy policy,
                            RetryStatus rs,
                            Predicate<? super Output> predicate) {

    return exp.then(output -> {
      if (predicate.test(output)) {
        Duration delay = policy.apply(rs);
        if (delay == null) {
          return succeed(output);
        }
        if (delay.isZero()) {
          return repeat(exp,
                        policy,
                        new RetryStatus(rs.counter() + 1,
                                        rs.cumulativeDelay(),
                                        Duration.ZERO),
                        predicate);
        }
        Fun.sleep(delay);
        return repeat(exp,
                      policy,
                      new RetryStatus(rs.counter() + 1,
                                      rs.cumulativeDelay()
                                        .plus(delay),
                                      delay),
                      predicate

                     );
      }
      return succeed(output);
    });

  }

  /**
   * Creates a copy of this effect that generates an {@link RecordedEvent} from the result of the computation and sends
   * it to the Flight Recorder system. Customization of the event can be achieved using the {@link #debug(EventBuilder)}
   * method.
   *
   * @return a new effect with debugging enabled.
   * @see EvalExpEvent
   */
  public IO<Output> debug() {
    return debug(EventBuilder.of(getClass().getSimpleName()));
  }

  /**
   * Creates a copy of this effect that generates an {@link RecordedEvent} from the result of the computation and sends
   * it to the Flight Recorder system. Customization of the event can be achieved using the provided
   * {@link EventBuilder}.
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
                                        builder.commitSuccess(val,
                                                              event);
                                      },
                                      exc -> {
                                        event.end();
                                        builder.commitFailure(exc,
                                                              event);
                                      }));
  }

  /**
   * Computes the result of this effect. If the computation succeeds, returns a {@link Success} containing the computed
   * output. If the computation fails, returns a {@link Failure} containing the exception that caused the failure.
   *
   * @return the result of the computation, either a {@link Success} or a {@link Failure}.
   */
  public Result<Output> compute() {
    try {
      return call();
    } catch (Exception e) {
      return new Failure<>(e);
    }
  }
}
