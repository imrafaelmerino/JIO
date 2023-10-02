
package jio.test.stub.effect;

import fun.gen.Gen;
import jio.IO;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Class to create different kinds of stubs that stand in for {@link IO IO} effects.
 * Each time the stub generates a value, an event is created and sent to the JFR (Java Flight Recorder) system.
 * This event contains information about the result and the number of times the stub has been called, which can be
 * useful for debugging purposes.
 *
 * @param <O> The type of the value computed by this stub.
 */
public abstract sealed class IOStub<O> implements Supplier<IO<O>> permits ValStub, FailureStub {

    Executor executor;

    /**
     * Sets the executor for this stub to perform computations asynchronously.
     *
     * @param executor The executor.
     * @return A new IO stub with the specified executor.
     */
    public IOStub<O> onExecutor(Executor executor) {
        this.executor = Objects.requireNonNull(executor);
        return this;
    }


    public static <O> IOStub<O> gen(Gen<O> gen){
        Supplier<O> supplier = gen.sample();
        return new ValStub<>(n -> supplier.get(),null);
    }

    /**
     * Creates an IO stub that returns successful effects with some delay. The value and the delay
     * are specified with functions that take as input the number of times the stub has been called.
     * For example:
     * <pre>
     * {@code
     * IOStub<Integer> supplier = IOStub.succeed(n -> n, n -> Duration.ofSeconds(n));
     * IO<Integer> stub = supplier.get();
     * stub.join();  // Produces 1 after one second
     * stub.join();  // Produces 2 after two seconds
     * // and so on
     * }
     * </pre>
     *
     * @param valueFn Function that takes the call's number and returns the value.
     * @param delayFn Function that takes the call's number and returns the delay.
     * @return An IOStub.
     * @param <O> The type of the returned value.
     */
    public static <O> IOStub<O> succeed(final IntFunction<O> valueFn,
                                        final IntFunction<Duration> delayFn
                                       ) {
        return new ValStub<>(Objects.requireNonNull(valueFn),
                             Objects.requireNonNull(delayFn),
                             null);
    }

    /**
     * Creates an IO stub that returns successful effects. The value
     * is specified with a function that takes as input the number of times the stub has been called.
     * For example:
     *
     * <pre>
     *
     *   {@code
     *
     *        IOStub<Integer> supplier = IOStub.succeed(n -> n);
     *
     *        IO<Integer> stub = supplier.get();
     *
     *        stub.join()  // produces 1
     *        stub.join()  // produces 2
     *        stub.join()  // produces 3
     *        and so on
     *
     *   }
     *
     * </pre>
     * @param valueFn function that takes the calls number and returns the value
     * @return an IOStub
     * @param <O> the type of the returned value
     */
    public static <O> IOStub<O> succeed(final IntFunction<O> valueFn) {
        return new ValStub<>(Objects.requireNonNull(valueFn), null);
    }

    /**
     * Creates an IO stub that fails with the exception returned by the given function. When
     * the function returns null, the specified value is returned.
     * For example:
     * <pre>
     * {@code
     * IOStub<Integer> supplier = IOStub.failThenSucceed(n -> n < 3 ? new RuntimeException() : null, 1);
     * IO<Integer> stub = supplier.get();
     * stub.join();  // Throws RuntimeException
     * stub.join();  // Throws RuntimeException
     * stub.join();  // Produces 1
     * // and so on
     * }
     * </pre>
     *
     * @param errorFn Function that takes the call's number and returns an exception or null.
     * @param value   The returned value when the errorFn returns null.
     * @return An IOStub.
     * @param <O> The type of the returned value.
     */
    public static <O> IOStub<O> failThenSucceed(final IntFunction<Throwable> errorFn,
                                                final O value
                                               ) {
        return new FailureStub<>(Objects.requireNonNull(errorFn),
                                 Objects.requireNonNull(value),
                                 null
        );
    }


    /**
     * Creates an IO stub that delays the computations according to the given delay function, failing with the
     * exception returned by errorFn. When errorFn returns null, the computation succeeds, and the specified
     * value is returned.
     * For example:
     * <pre>
     * {@code
     * IOStub<Integer> supplier = IOStub.failThenSucceed(
     *     n -> n < 3 ? new RuntimeException() : null,
     *     n -> Duration.ofSeconds(n),
     *     1
     * );
     * IO<Integer> stub = supplier.get();
     * stub.join();  // Throws RuntimeException after 1 second
     * stub.join();  // Throws RuntimeException after 2 seconds
     * stub.join();  // Produces 1 after three seconds
     * // and so on
     * }
     * </pre>
     *
     * @param errorFn Function that takes the call's number and returns an exception or null.
     * @param delayFn Function that takes the call's number and returns a delay.
     * @param value   The value returned.
     * @return An IOStub.
     * @param <O> The type of the returned value.
     */
    public static <O> IOStub<O> failThenSucceed(final IntFunction<Throwable> errorFn,
                                                final IntFunction<Duration> delayFn,
                                                final O value
                                               ) {

        return new FailureStub<>(Objects.requireNonNull(errorFn),
                                 Objects.requireNonNull(delayFn),
                                 Objects.requireNonNull(value),
                                 null
        );
    }

    void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    CompletableFuture<O> delayValue(long millis,
                                    O value
                                   ) {
        if (executor == null) {
            sleep(millis);
            return CompletableFuture.completedFuture(value);
        }

        return CompletableFuture.supplyAsync(() -> {
                                                 sleep(millis);
                                                 return value;
                                             },
                                             executor
                                            );

    }

    CompletableFuture<O> delayExc(long millis,
                                  Throwable failure
                                 ) {
        return delayValue(millis,
                          null
                         )
                .thenCompose(it -> CompletableFuture.failedFuture(failure));
    }

    /**
     * Wraps an IO effect with stub event tracking, recording information about the execution of the effect.
     * Records success or failure and commits the event.
     *
     * @param effect  The IO effect to be wrapped.
     * @param counter A supplier of an integer counter.
     * @return An IO effect with stub event tracking.
     */
    static <O> IO<O> wrapStub(IO<O> effect,Supplier<Integer> counter) {
        return IO.lazy(() -> {
            StubEvent event = new StubEvent();
            event.begin();
            return event;
        }).then(event -> effect
                .peek(value -> {
                          event.counter = counter.get();
                          event.value = value == null ? "null" : value.toString();
                          event.result = StubEvent.RESULT.SUCCESS.name();
                          event.commit();
                      },
                      exc -> {
                          event.counter = counter.get();
                          event.exception = String.format("%s:%s",exc.getClass().getName(),exc.getMessage());
                          event.result = StubEvent.RESULT.FAILURE.name();
                          event.commit();
                      }
                     )
               );
    }


}
