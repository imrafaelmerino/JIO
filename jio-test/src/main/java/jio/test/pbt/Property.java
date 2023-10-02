package jio.test.pbt;

import fun.gen.Gen;
import jio.*;
import jsonvalues.JsObj;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.SplittableRandom;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Represents a property of a piece of code or program that should always be held and never fails.
 * This property is modeled with a supplier that returns a JIO effect used for property testing.
 * The property test is executed the specified number of times with {@link #withTimes(int)} (default is {@link #DEFAULT_TESTS}),
 * each time with a different value generated using the provided data generator.
 *
 * <p>The tests are executed by the same thread from the common ForkJoinPool repeatedly, and for each execution,
 * a different value is generated using the specified data generator.</p>
 *
 * <p>The JIO effect is created with the {@link IO#managedLazy(Supplier)} constructor, allowing deferred execution
 * of the property tests.</p>
 *
 * <p>Properties can be created using static factory methods {@link #ofFunction(String, Gen, Function)} and
 * {@link #ofLambda(String, Gen, Lambda)}.</p>
 *
 * <p>The {@link #check(JsObj)} method creates an IO computation that defers the execution of the tests.</p>
 *
 * <p>By default, the property is executed {@value #DEFAULT_TESTS} times, but this can be changed using the
 * {@link #withTimes(int)} method to specify a different number of executions.</p>
 *
 * @param <O> the type of the data generated to feed the property tests
 */
public class Property<O> implements Testable {
    private boolean collect;
    private static final RandomGenerator seedGen = new SplittableRandom();
    private static final int DEFAULT_TESTS = 1000;
    public final String name;
    final Gen<O> gen;
    final BiLambda<JsObj, O, TestResult> lambda;


    public String description = "";
    int times = DEFAULT_TESTS;
    private Path path;
    private JsObj conf = JsObj.empty();
    private Map<String, Predicate<O>> classifiers;

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
     * Creates a new Property instance that represents a property to be tested,
     * modeled with a function. This method is used to define a property with a
     * specific name, data generator, and a function that tests the property.
     * If you need to pass configuration (JsObj) to the testing function, you can
     * use the {@link #ofFunction(String, Gen, BiFunction)} method instead.
     *
     * @param name The name of the property, which provides a descriptive label
     *             for the property being tested.
     * @param gen  The data generator, represented by a Gen object, that produces
     *             pseudorandom data to feed the property tests.
     * @param fn   The property testing function, represented by a Function, that
     *             takes generated data of type O and returns a TestResult indicating
     *             the success or failure of the property test.
     * @param <O>  The type of the data generated by the Gen object and processed by
     *             the testing function.
     * @return A new Property instance representing the defined property.
     * @see #ofFunction(String, Gen, BiFunction)
     */
    public static <O> Property<O> ofFunction(final String name,
                                             final Gen<O> gen,
                                             final Function<O, TestResult> fn
                                            ) {
        BiFunction<JsObj, O, TestResult> bfn = (conf, o) -> requireNonNull(fn).apply(o);
        return new Property<>(requireNonNull(name), bfn, gen);
    }

    /**
     * Creates a new Property instance that represents a property to be tested,
     * modeled with a function. This method is used to define a property with a
     * specific name, data generator, and a testing function that takes a JSON
     * configuration (JsObj) as well as generated data of type O to produce a TestResult.
     *
     * @param name The name of the property, which provides a descriptive label
     *             for the property being tested.
     * @param gen  The data generator, represented by a Gen object, that produces
     *             pseudorandom data to feed the property tests.
     * @param fn   The property testing function, represented by a BiFunction, that
     *             takes a JSON configuration (JsObj) and generated data of type O,
     *             and returns a TestResult indicating the success or failure
     *             of the property test.
     * @param <O>  The type of the data generated by the Gen object and processed by
     *             the testing function.
     * @return A new Property instance representing the defined property.
     */
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
     * Creates a new Property instance that represents a property to be tested,
     * modeled with a lambda function. This method is used to define a property with
     * a specific name, data generator, and a lambda function that takes generated
     * data of type O and produces a TestResult. If you need to pass configuration
     * to the lambda function, you can use the {@link #ofLambda(String, Gen, BiLambda)}
     * method instead.
     *
     * @param name     The name of the property, which provides a descriptive label
     *                 for the property being tested.
     * @param gen      The data generator, represented by a Gen object, that produces
     *                 pseudorandom data to feed the property tests.
     * @param property The property testing lambda function, represented by a Lambda,
     *                 that takes generated data of type O and returns a TestResult
     *                 indicating the success or failure of the property test.
     * @param <O>      The type of the data generated by the Gen object and processed
     *                 by the testing function.
     * @return A new Property instance representing the defined property.
     * @see #ofLambda(String, Gen, BiLambda)
     */
    public static <O> Property<O> ofLambda(final String name,
                                           final Gen<O> gen,
                                           final Lambda<O, TestResult> property
                                          ) {
        BiLambda<JsObj, O, TestResult> bfn = (conf, o) -> requireNonNull(property).apply(o);
        return new Property<>(requireNonNull(name),
                              bfn,
                              gen);
    }

    /**
     * Creates a new Property instance that represents a property to be tested,
     * modeled with a lambda function. This method is used to define a property with
     * a specific name, data generator, and a lambda function that takes a JSON
     * configuration (JsObj) and generated data of type O to produce a TestResult.
     *
     * @param name     The name of the property, which provides a descriptive label
     *                 for the property being tested.
     * @param gen      The data generator, represented by a Gen object, that produces
     *                 pseudorandom data to feed the property tests.
     * @param property The property testing lambda function, represented by a
     *                 BiLambda, that takes a JSON configuration (JsObj) and generated
     *                 data of type O, and returns a TestResult indicating the success
     *                 or failure of the property test.
     * @param <O>      The type of the data generated by the Gen object and processed
     *                 by the testing function.
     * @return A new Property instance representing the defined property.
     */
    public static <O> Property<O> ofLambda(final String name,
                                           Gen<O> gen,
                                           final BiLambda<JsObj, O, TestResult> property
                                          ) {
        // Create and return a new Property instance with the provided name, testing lambda function, and data generator.
        return new Property<>(requireNonNull(name),
                              requireNonNull(property),
                              gen);
    }

    private static IO<TestResult> handleExc(Report report,
                                            Throwable exc,
                                            Context context
                                           ) {
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
                                               Context context
                                              ) {
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

    //todo property immutable
    public Property<O> withCollector() {
        this.collect = true;
        return this;
    }

    public Property<O> withClassifiers(final Map<String, Predicate<O>> classifiers,
                                       final String defaultTag
                                      ) {
        if (requireNonNull(classifiers).isEmpty()) throw new IllegalArgumentException("classifiers empty");
        Predicate<O> defaultClassifier =
                o -> classifiers.values().stream()
                                .noneMatch(cla -> cla.test(o));

        Map<String, Predicate<O>> xs = new HashMap<>(classifiers);
        xs.put(requireNonNull(defaultTag), defaultClassifier);
        this.classifiers = xs;
        return this;
    }

    private String getTags(O value) {
        if (classifiers == null) return "";
        return classifiers.keySet()
                          .stream()
                          .filter(key -> classifiers.get(key).test(value))
                          .collect(Collectors.joining(","));
    }


    synchronized void dump(Report report) {
        try {
            Files.writeString(path, report + "\n");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Specifies the path where the report of the property test result should be exported.
     * If this method is called with a valid path, the report will be copied to the specified location.
     * The report is written as a JSON representation of the test result.
     *
     * @param path The path where the report should be exported. The path must represent a regular file
     *             that exists on the file system.
     * @return This Property instance with the export path set.
     * @throws IllegalArgumentException If the provided path is not a regular file or does not exist.
     */
    public Property<O> withExportPath(Path path) {
        if (!Files.isRegularFile(requireNonNull(path)))
            throw new IllegalArgumentException(String.format("%s is not a regular file", path));
        if (!Files.exists(path))
            throw new IllegalArgumentException(String.format("%s doesn't exist", path));
        this.path = path;
        return this;
    }

    /**
     * Returns a new testable instance that represents the property and will be executed in parallel
     * for the specified number of times, using multiple threads from the common ForkJoinPool.
     *
     * @param n the number of parallel executions for the property
     * @return a new testable instance with parallel execution
     */
    public Testable repeatPar(int n) {
        return new ParProperty<>(n, this);
    }

    /**
     * Returns a new testable instance that represents the property and will be executed sequentially
     * for the specified number of times.
     *
     * @param n the number of sequential executions for the property
     * @return a new testable instance with sequential execution
     */
    public Testable repeatSeq(int n) {
        return new SeqProperty<>(n, this);
    }

    /**
     * Returns a new property instance that will be tested the specified number of times.
     * Each execution of the property test generates a new input value using the data generator,
     * and the test is repeated for the specified number of times with different input values.
     *
     * @param times the number of times an input is produced and tested on the property
     * @return a new property with the specified number of test executions
     * @throws IllegalArgumentException if {@code times} is less than 0
     */
    public Property<O> withTimes(int times) {
        if (times < 0) throw new IllegalArgumentException("times < 0");
        Property<O> test = copy(lambda);
        test.times = times;
        return test;
    }

    /**
     * Returns a new Property instance with the specified description.
     *
     * @param description The test description providing additional context for the property test.
     * @return A new Property instance with the specified description.
     * @throws IllegalArgumentException If the provided description is blank.
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
                IO.succeed(i)
                  .then(n -> {
                            var tic = Instant.now();
                            var generated = rg.get();
                            String tags = getTags(generated);
                            if(classifiers!=null)report.classify(tags);
                            if(collect) report.collect(generated == null ? "null" : generated.toString());
                            return lambda.apply(conf, generated)
                                         .then(
                                                 tr -> {
                                                     report.tac(tic);
                                                     var context = new Context(tic, seed, n, generated, tags);
                                                     return handleResult(report, tr, context);

                                                 },
                                                 exc -> {
                                                     report.tac(tic);
                                                     var context = new Context(tic, seed, n, generated, tags);
                                                     return handleExc(report, exc, context);
                                                 });
                        }
                       )
                  .result();
            }
            report.setEndTime(Instant.now());
            return report;
        };

        IO<Report> io = IO.managedLazy(task)
                          .peekSuccess(Report::summarize);

        return path == null ? io : io.peekSuccess(this::dump);

    }


}
