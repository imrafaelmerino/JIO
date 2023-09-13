package jio.pbt;

import fun.gen.Gen;
import jio.*;
import jsonvalues.JsObj;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Random;
import java.util.SplittableRandom;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

import static java.util.Objects.requireNonNull;

/**
 * Represents a property of a piece of code or program that should always
 * be held and never fails. It's modeled with a supplier that returns
 * a JIO effect that generates pseudorandom data to feed a
 * test executed the specified number of times with {@link #times(int)} ({@link #DEFAULT_TESTS} by default) and
 * generates a {@link Report}.
 * <p>
 * The tests will be executed by a thread from the common FromJoinPool since
 * the JIO effect is created with the {@link IO#managedLazy(Supplier)} constructor.
 * To run the tests with a thread from a different pool,
 * an executor can be specified with the method {@link #on(ExecutorService)}.
 * <p>
 * A generator has to be specified with the method {@link #withGen(Gen)}, otherwise
 * the value null is always generated
 * <p>
 * Properties can be created with the static factory methods {@link #ofFunction(String, Function)} and
 * {@link #ofLambda(String, Lambda)}
 * <p>
 * A property test is stopped as soon as an exception or failure happens if the
 * method {@link #stopAfterFailure()} is called.
 *
 * @param <O> the type of the data generated to feed the property tests
 */
public final class Property<O> implements Function<JsObj, Report> {
    private static final RandomGenerator seedGen = new SplittableRandom();
    private Gen<O> gen = Gen.cons(null);
    final BiLambda<JsObj, O, TestResult> lambda;
    private static final int DEFAULT_TESTS = 1000;
    int times = DEFAULT_TESTS;
    public String description = "";
    public final String name;
    private boolean stopAfterFailure = false;
    private Executor executor;


    /**
     * Returns a new property that stops the execution when a test is not successful, instead of
     * keep testing the property the specified number of times.
     *
     * @return a new property that stops the execution a soon as test is not successful
     */
    public Property<O> stopAfterFailure() {
        Property<O> test = copy(lambda);
        test.stopAfterFailure = true;
        return test;
    }


    public Property<O> withDelay(final Duration duration) {
        return copy((conf, o) -> Delay.of(duration,
                                          ForkJoinPool.commonPool()
                                         )
                                      .then($ -> lambda.apply(conf, o))
                   );

    }

    /**
     * @param fn the property to be tested
     */
    private Property(final String name,
                     final BiFunction<JsObj, O, TestResult> fn
                    ) {
        if (name == null || name.isBlank() || name.isEmpty())
            throw new IllegalArgumentException("property name missing");
        this.lambda = (conf, o) -> switch (fn.apply(conf, o)) {
            case TestSuccess r -> IO.value(r);
            case TestFailure f -> IO.failure(f);
            case TestException e -> IO.failure(e);
        };
        this.name = requireNonNull(name);
    }

    private Property(final String name,
                     final BiLambda<JsObj, O, TestResult> property
                    ) {
        this.lambda = requireNonNull(property);
        this.name = requireNonNull(name);
    }

    /**
     * Returns a new property that uses the specified generator to produce  randomized input data
     *
     * @param gen generator to produce randomized input data
     * @return a new property with the specified property
     */
    public Property<O> withGen(final Gen<O> gen) {
        final Property<O> test = copy(lambda);
        test.gen = requireNonNull(gen);
        return test;
    }

    /**
     * Returns a new property instance that will be tested the specified number of times
     *
     * @param times number of times an input is produced and tested on the property
     * @return a new property
     */
    public Property<O> times(int times) {
        if (times < 0) throw new IllegalArgumentException("times < 0");
        Property<O> test = copy(lambda);
        test.times = times;
        return test;
    }


    /**
     * Returns a new property instance with the specified description
     *
     * @param description the test description
     * @return a new property with the specified description
     */
    public Property<O> withDescription(final String description) {
        if (requireNonNull(description).isBlank())
            throw new IllegalArgumentException("property description is blank");
        Property<O> test = copy(lambda);
        test.description = description;
        return test;
    }

    private Property<O> copy(final BiLambda<JsObj, O, TestResult> lambda) {
        Property<O> test = new Property<>(name,
                                          lambda
        );
        test.stopAfterFailure = stopAfterFailure;
        test.description = description;
        test.gen = gen;
        test.executor = executor;
        test.times = times;
        return test;
    }


    /**
     * Constructor to create a property modeled with a function
     *
     * @param name the name of the property
     * @param fn   property to be tested
     * @param <O>  the type of the data generated to feed the property tests
     * @return a Property
     */
    public static <O> Property<O> ofFunction(final String name,
                                             final Function<O, TestResult> fn
                                            ) {
        BiFunction<JsObj, O, TestResult> bfn = (conf, o) -> requireNonNull(fn).apply(o);
        return new Property<>(requireNonNull(name),
                              bfn
        );
    }

    public static <O> Property<O> ofFunction(final String name,
                                             final BiFunction<JsObj, O, TestResult> fn
                                            ) {
        return new Property<>(requireNonNull(name), requireNonNull(fn));
    }

    /**
     * Constructor to create a property modeled with a lambda
     *
     * @param name     the name of the property
     * @param property property to be tested
     * @param <O>      the type of the data generated to feed the property tests
     * @return a Property
     */
    public static <O> Property<O> ofLambda(final String name,
                                           final Lambda<O, TestResult> property
                                          ) {
        BiLambda<JsObj, O, TestResult> bfn = (conf, o) -> requireNonNull(property).apply(o);

        return new Property<>(requireNonNull(name),
                              requireNonNull(bfn)
        );
    }

    public static <O> Property<O> ofLambda(final String name,
                                           final BiLambda<JsObj, O, TestResult> property
                                          ) {
        return new Property<>(requireNonNull(name),
                              requireNonNull(property)
        );
    }

    /**
     * Returns a new property instance which tests will be executed on a thread from the
     * specified executor.
     *
     * @param executor the executor from which the tests will pick threads to be executed on
     * @return a new property instance
     */
    public Property<O> on(final ExecutorService executor) {
        Property<O> test = copy(lambda);
        test.executor = requireNonNull(executor);
        return test;
    }

    public Report repeatPar(int n) {
        return repeatPar(n, JsObj.empty());
    }

    /**
     * Returns an effect that executes the property in parallel the specified number of times and
     * aggregates all the reports into the result
     *
     * @param n the number of time the property is tested
     * @return a JIO effect
     */
    public Report repeatPar(int n, JsObj conf) {
        if (n < 1) throw new IllegalArgumentException("n < 1");
        final IO<Report> test = task(Objects.requireNonNull(conf));
        var result = ListExp.par(test);
        for (int i = 1; i < n; i++) result = result.append(test);
        IO<Report> aggregated = result.map(it -> it.stream()
                                                   .reduce(Report::aggregatePar)
                                                   .get()
                                          );
        return aggregated.join();
    }

    public Report repeatSeq(int n) {
        return repeatSeq(n,
                         JsObj.empty()
                        );
    }

    public Report repeatSeq(int n, JsObj conf) {
        if (n < 1) throw new IllegalArgumentException("n < 1");
        final IO<Report> test = task(Objects.requireNonNull(conf));
        var result = ListExp.seq(test);
        for (int i = 1; i < n; i++) result = result.append(test);
        IO<Report> aggregated = result.map(it -> it.stream()
                                                   .reduce(Report::aggregate)
                                                   .get()
                                          );
        return aggregated.join();
    }


    IO<Report> task(JsObj conf) {
        Supplier<Report> task = () -> {
            Report report = new Report(name, description);
            long seed = seedGen.nextLong();
            Supplier<O> rg = gen.apply(new Random(seed));
            for (int i = 1; i <= times; i++) {
                report.incTest();
                TestResult result =
                        IO.value(i)
                          .then(n -> {
                                    var tic = Instant.now();
                                    var generated = rg.get();
                                    return lambda.apply(conf, generated)
                                                 .then(tr -> {
                                                           report.tac(tic);
                                                           var context = new Context(tic,
                                                                                     seed,
                                                                                     n,
                                                                                     generated
                                                           );
                                                           return handleResult(report, tr, context);

                                                       },
                                                       exc -> {
                                                           report.tac(tic);
                                                           var context = new Context(tic,
                                                                                     seed,
                                                                                     n,
                                                                                     generated
                                                           );
                                                           return handleExc(report, exc, context);
                                                       }
                                                      );
                                }

                               )
                          .join();

                if (!result.equals(TestResult.SUCCESS) && stopAfterFailure) break;

            }
            return report;
        };
        IO<Report> io = executor == null ?
                IO.managedLazy(task) :
                IO.lazy(task, executor);

        return io;

    }


    /**
     * returns the name of the test
     *
     * @return the name of the test
     */
    public String getName() {
        return name;
    }

    /**
     * returns the description of the test
     *
     * @return the description of the test
     */
    public String getDescription() {
        return description;
    }


    private static IO<TestResult> handleExc(Report report, Throwable exc, Context context) {
        return switch (exc) {
            case TestFailure tf -> {
                report.addFailure(new FailureContext(context,
                                                     tf
                                  )
                                 );
                yield IO.value(tf);
            }


            case Throwable error -> {
                report.addException(new ExceptionContext(context,
                                                         error
                                    )
                                   );
                yield IO.value(new TestException(error));
            }

        };
    }

    private static IO<TestResult> handleResult(Report report, TestResult tr, Context context) {
        return switch (tr) {
            case TestFailure tf -> {
                report.addFailure(new FailureContext(context,
                                                     tf
                                  )
                                 );
                yield IO.value(tf);
            }

            case TestException tf -> {
                report.addException(new ExceptionContext(context,
                                                         tf.getCause()
                                    )
                                   );
                yield IO.value(tf);
            }


            case TestSuccess ts -> IO.value(ts);

        };
    }

    @Override
    public Report apply(JsObj conf) {
        return task(conf).join();
    }

    public Report apply() {
        return task(JsObj.empty()).join();
    }
}
