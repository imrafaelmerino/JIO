package jio.test.stub.effect;

import fun.gen.Gen;
import jio.Delay;
import jio.IO;
import jio.Lambda;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utility class for generating `IO` instances using generators, commonly used for creating stubs in testing scenarios.
 * Remember you can create complex generators using the operators from the {@link fun.gen.Combinators } class.
 * <p>
 * Example of generating `IO` instances with generators:
 * <pre>
 * {@code
 * var gen1 = Gens.seq(n -> IO.succeed(n));
 * var gen2 = Gens.fail(new RuntimeException("bad luck!"));
 * var gen = Combinators.oneOf(gen1, gen2);
 * }
 * </pre>
 *
 * @see Stub
 * @see fun.gen.Combinators
 */
public final class Gens {

    private Gens() {
    }

    /**
     * Creates a generator that produces successful `IO` instances using the provided generator.
     *
     * @param gen The generator to produce values.
     * @param <O> The type of value to generate.
     * @return A generator of successful `IO` instances.
     */
    public static <O> Gen<IO<O>> gen(final Gen<O> gen) {
        return Objects.requireNonNull(gen).map(IO::succeed);
    }

    /**
     * Creates a generator that always fails with the specified exception.
     *
     * @param exc The exception the generator fails with.
     * @param <O> The type of value to generate.
     * @return A generator that always fails with the given exception.
     */
    public static <O> Gen<IO<O>> fail(final Throwable exc) {
        Objects.requireNonNull(exc);
        return seed -> () -> IO.fail(exc);
    }

    /**
     * Creates a generator that produces `IO` instances by applying a lambda function to the integer sequence of calls.
     * <pre>
     * {@code
     *
     * var gen = Gens.seq(n -> IO.succeed(n));
     * gen.sample(3).forEach(io -> System.out.println(io.result()));
     *
     * And the result is 1,2,3
     *
     * var gen1 = Gens.seq(n -> n < 2
     *                          ? IO.succeed(n)
     *                          : IO.fail(new RuntimeException("bad luck"))
     *                     );
     * The two first generated values are 1 and 2 and then always an exception
     * }
     * </pre>
     *
     * @param fn  The lambda function to apply. The integer represents the call sequence (1 for the first call, 2 for
     *            the second, and so on).
     * @param <O> The type of value to generate.
     * @return A generator of `IO` instances, commonly used for creating stubs in testing.
     */
    public static <O> Gen<IO<O>> seq(final Lambda<Integer, O> fn) {
        Objects.requireNonNull(fn);
        return random -> {
            AtomicInteger ai = new AtomicInteger(1);
            Supplier<IO<O>> supplier = () -> {
                int n = ai.getAndIncrement();
                return fn.apply(n);
            };
            return supplier;
        };
    }

    /**
     * Creates a generator that produces `IO` instances with delays by applying a lambda function and a delay function
     * to the integer sequence of calls.
     * <pre>
     * {@code
     *
     * var gen = Gens.seq(n -> IO.succeed(n),
     *                    n  -> Duration.ofSeconds(n));
     * gen.sample(3).forEach(io -> System.out.println(io.result()));
     *
     * And the result is 1 after 1 second, 2 after 2 seconds and 3 after three seconds
     *
     * }
     * </pre>
     *
     * @param fn    The lambda function to apply. The integer represents the call sequence (1 for the first call, 2 for
     *              the second, and so on).
     * @param delay The delay function to determine the delay for each value.
     * @param <O>   The type of value to generate.
     * @return A generator of delayed `IO` instances, commonly used for creating stubs in testing.
     */
    public static <O> Gen<IO<O>> seq(final Lambda<Integer, O> fn,
                                     final Function<Integer, Duration> delay
                                    ) {
        Objects.requireNonNull(fn);
        Objects.requireNonNull(delay);
        return random -> {
            AtomicInteger ai = new AtomicInteger(1);
            Supplier<IO<O>> supplier = () -> {
                int n = ai.getAndIncrement();
                try {
                    Thread.sleep(delay.apply(n).toMillis());
                    return fn.apply(n);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return IO.fail(e);
                }
            };
            return supplier;
        };
    }

    /**
     * Creates a generator that produces `IO` instances with delays and execution on a specified executor by applying a
     * lambda function and a delay function to the integer sequence of calls, and an executor.
     *
     * @param fn       The lambda function to apply. The integer represents the call sequence (1 for the first call, 2
     *                 for the second, and so on).
     * @param delay    The delay function to determine the delay for each value.
     * @param executor The executor for asynchronous execution.
     * @param <O>      The type of value to generate.
     * @return A generator of delayed and asynchronously executed `IO` instances, commonly used for creating stubs in
     * testing.
     */
    public static <O> Gen<IO<O>> seq(final Lambda<Integer, O> fn,
                                     final Function<Integer, Duration> delay,
                                     final Executor executor
                                    ) {
        return random -> {
            AtomicInteger ai = new AtomicInteger(1);
            Supplier<IO<O>> supplier = () -> {
                int n = ai.getAndIncrement();
                return Delay.of(delay.apply(n), executor).then(it -> fn.apply(n));
            };
            return supplier;
        };
    }
}

