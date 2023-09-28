package jio.test.pbt;

import fun.gen.Gen;
import jio.*;
import jsonvalues.JsObj;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Random;
import java.util.SplittableRandom;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

import static java.util.Objects.requireNonNull;

/**
 * Represents a property of a piece of code or program that should always
 * be held and never fails. It's modeled with a supplier that returns
 * a JIO effect that generates pseudorandom data to feed a
 * test executed the specified number of times with {@link #withTimes(int)} ({@link #DEFAULT_TESTS} by default) and
 * generates a {@link Report}.
 * <p>
 * The tests will be executed by a thread from the common FromJoinPool since
 * the JIO effect is created with the {@link IO#managedLazy(Supplier)} constructor.
 * <p>
 * <p>
 * Properties can be created with the static factory methods {@link #ofFunction(String, Function)} and
 * {@link #ofLambda(String, Lambda)}
 * <p>
 *
 * @param <O> the type of the data generated to feed the property tests
 */
public class Property<O> implements Testable {
    private static final RandomGenerator seedGen = new SplittableRandom();
    private static final int DEFAULT_TESTS = 1000;
    final String name;
    final Gen<O> gen;
    final BiLambda<JsObj, O, TestResult> lambda;
    String description = "";
    int times = DEFAULT_TESTS;
    private Path path;
    private JsObj conf = JsObj.empty();

    /**
     * @param fn the property to be tested
     */
    private Property(final String name, final BiFunction<JsObj, O, TestResult> fn, Gen<O> gen) {
        if (name == null || name.isBlank() || name.isEmpty())
            throw new IllegalArgumentException("property name missing");
        this.lambda = (conf, o) -> switch (fn.apply(conf, o)) {
            case TestSuccess r -> IO.succeed(r);
            case TestFailure f -> IO.fail(f);
            case TestException e -> IO.fail(e);
        };
        this.name = requireNonNull(name);
        this.gen = gen;
    }


    private Property(final String name,
                     final BiLambda<JsObj, O, TestResult> property,
                     final Gen<O> gen
                    ) {
        this.lambda = requireNonNull(property);
        this.name = requireNonNull(name);
        this.gen = requireNonNull(gen);
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
                                             final Gen<O> gen,
                                             final Function<O, TestResult> fn
                                            ) {
        BiFunction<JsObj, O, TestResult> bfn = (conf, o) -> requireNonNull(fn).apply(o);
        return new Property<>(requireNonNull(name), bfn, gen);
    }

    public static <O> Property<O> ofFunction(final String name,
                                             final Gen<O> gen,
                                             final BiFunction<JsObj, O, TestResult> fn
                                            ) {
        return new Property<>(requireNonNull(name),
                              requireNonNull(fn),
                              gen
        );
    }

    /**
     * Constructor to create a property modeled with a lambda
     *
     * @param name     the name of the property
     * @param property property to be tested
     * @param <O>      the type of the data generated to feed the property tests
     * @return a Property
     */
    public static <O> Property<O> ofLambda(final String name, final Gen<O> gen, final Lambda<O, TestResult> property) {
        BiLambda<JsObj, O, TestResult> bfn = (conf, o) -> requireNonNull(property).apply(o);
        return new Property<>(requireNonNull(name),
                              bfn,
                              gen);
    }

    public static <O> Property<O> ofLambda(final String name, Gen<O> gen, final BiLambda<JsObj, O, TestResult> property) {
        return new Property<>(requireNonNull(name),
                              requireNonNull(property),
                              gen);
    }

    private static IO<TestResult> handleExc(Report report, Throwable exc, Context context) {
        return switch (exc) {
            case TestFailure tf -> {
                report.addFailure(new FailureContext(context,
                                                     tf));
                yield IO.succeed(tf);
            }

            case Throwable error -> {
                report.addException(new ExceptionContext(context,
                                                         error));
                yield IO.succeed(new TestException(error));
            }

        };
    }

    private static IO<TestResult> handleResult(Report report,
                                               TestResult tr,
                                               Context context) {
        return switch (tr) {
            case TestFailure tf -> {
                report.addFailure(new FailureContext(context,
                                                     tf));
                yield IO.succeed(tf);
            }

            case TestException tf -> {
                report.addException(new ExceptionContext(context,
                                                         tf.getCause()));
                yield IO.succeed(tf);
            }

            case TestSuccess ts -> IO.succeed(ts);

        };
    }

    synchronized void dump(Report report) {
        try {
            Files.writeString(path, report + "\n");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Property<O> withExportPath(Path path) {
        if (!Files.isRegularFile(requireNonNull(path)))
            throw new IllegalArgumentException(String.format("%s is not a regular file", path));
        if (!Files.exists(path))
            throw new IllegalArgumentException(String.format("%s doesn't exist", path));
        this.path = path;
        return this;
    }

    public Testable repeatPar(int n) {
        return new ParProperty<>(n, this);
    }

    public Testable repeatSeq(int n) {
        return new SeqProperty<>(n, this);
    }

    /**
     * Returns a new property instance that will be tested the specified number of times
     *
     * @param times number of times an input is produced and tested on the property
     * @return a new property
     */
    public Property<O> withTimes(int times) {
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
                                          lambda,
                                          gen);
        test.description = description;
        test.times = times;
        test.conf = conf;
        return test;
    }


    @Override
    public IO<Report> check(JsObj conf) {
        Supplier<Report> task = () -> {
            Report report = new Report(name, description);
            long seed = seedGen.nextLong();
            Supplier<O> rg = gen.apply(new Random(seed));
            report.setStartTime(Instant.now());
            for (int i = 1; i <= times; i++) {
                report.incTest();
                IO.succeed(i).then(n -> {
                                       var tic = Instant.now();
                                       var generated = rg.get();
                                       return lambda.apply(conf, generated).then(tr -> {
                                           report.tac(tic);
                                           var context = new Context(tic, seed, n, generated);
                                           return handleResult(report, tr, context);

                                       }, exc -> {
                                           report.tac(tic);
                                           var context = new Context(tic, seed, n, generated);
                                           return handleExc(report, exc, context);
                                       });
                                   }

                                  ).result();
            }
            report.setEndTime(Instant.now());
            return report;
        };

        IO<Report> io = IO.managedLazy(task).peekSuccess(Report::summarize);

        return path == null ? io : io.peekSuccess(this::dump);

    }


}
