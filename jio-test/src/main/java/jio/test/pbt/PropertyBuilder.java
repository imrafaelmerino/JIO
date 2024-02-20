package jio.test.pbt;

import fun.gen.Gen;
import jio.BiLambda;
import jio.IO;
import jio.Lambda;
import jsonvalues.JsObj;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Represents a builder of Properties. A property of a piece of code or program should always be held and never fails.
 * This property is modeled with a supplier that returns a JIO effect used for property testing. The property test is
 * executed the specified number of times with {@link #withTimes(int)} (default is {@link #DEFAULT_TESTS}), each time
 * with a different value generated using the provided data generator.
 *
 * <p>The tests are executed by the same thread from the common ForkJoinPool repeatedly, and for
 * each execution, a different value is generated using the specified data generator.</p>
 *
 * <p>The JIO effect is created with the {@link IO#managedLazy(Supplier)} constructor, allowing
 * deferred execution of the property tests.</p>
 *
 * <p>Properties can be created using static factory methods {@link #of(String, Gen, Function)} and
 * {@link #ofLambda(String, Gen, Lambda)}.</p>
 *
 *
 * <p>By default, the property is executed {@value #DEFAULT_TESTS} times, but this can be changed
 * using the {@link #withTimes(int)} method to specify a different number of executions.</p>
 *
 * @param <GenValue> the type of the data generated to feed the property tests
 */
public final class PropertyBuilder<GenValue> implements Supplier<Property<GenValue>> {

  private static final int DEFAULT_TESTS = 1000;
  private final String name;
  private final Gen<GenValue> gen;
  private final BiLambda<JsObj, GenValue, TestResult> property;
  private String description = "";
  private int times = DEFAULT_TESTS;
  private Path reportPath;
  private boolean collect;
  private Map<String, Predicate<GenValue>> classifiers;

  private PropertyBuilder(String name,
                          Gen<GenValue> gen,
                          BiLambda<JsObj, GenValue, TestResult> property) {
    this.name = name;
    this.gen = gen;
    this.property = property;
  }

  /**
   * Creates a new Property instance that represents a property to be tested, modeled with a lambda function. This
   * method is used to define a property with a specific name, data generator, and a lambda function that takes a JSON
   * configuration (JsObj) and generated data of type O to produce a TestResult.
   *
   * @param name     The name of the property, which provides a descriptive label for the property being tested.
   * @param gen      The data generator, represented by a Gen object, that produces pseudorandom data to feed the
   *                 property tests.
   * @param property The property testing lambda function, represented by a BiLambda, that takes a JSON configuration
   *                 (JsObj) and generated data of type O, and returns a TestResult indicating the success or failure of
   *                 the property test.
   * @param <O>      The type of the data generated by the Gen object and processed by the testing function.
   * @return A new Property instance representing the defined property.
   */
  public static <O> PropertyBuilder<O> ofLambda(final String name,
                                                final Gen<O> gen,
                                                final BiLambda<JsObj, O, TestResult> property
  ) {
    return new PropertyBuilder<>(name,
                                 gen,
                                 property);
  }

  /**
   * Creates a new Property instance that represents a property to be tested, modeled with a lambda function. This
   * method is used to define a property with a specific name, data generator, and a lambda function that takes
   * generated data of type O and produces a TestResult. If you need to pass configuration to the lambda function, you
   * can use the {@link #ofLambda(String, Gen, BiLambda)} method instead.
   *
   * @param name     The name of the property, which provides a descriptive label for the property being tested.
   * @param gen      The data generator, represented by a Gen object, that produces pseudorandom data to feed the
   *                 property tests.
   * @param property The property testing lambda function, represented by a Lambda, that takes generated data of type O
   *                 and returns a TestResult indicating the success or failure of the property test.
   * @param <O>      The type of the data generated by the Gen object and processed by the testing function.
   * @return A new Property instance representing the defined property.
   * @see #ofLambda(String, Gen, BiLambda)
   */
  public static <O> PropertyBuilder<O> ofLambda(String name,
                                                Gen<O> gen,
                                                Lambda<O, TestResult> property
  ) {
    BiLambda<JsObj, O, TestResult> bfn = (conf,
                                          o) -> requireNonNull(property).apply(o);
    return new PropertyBuilder<>(name,
                                 gen,
                                 bfn);
  }

  /**
   * Creates a new Property instance that represents a property to be tested, modeled with a function. This method is
   * used to define a property with a specific name, data generator, and a testing function that takes a JSON
   * configuration (JsObj) as well as generated data of type O to produce a TestResult.
   *
   * @param name     The name of the property, which provides a descriptive label for the property being tested.
   * @param gen      The data generator, represented by a Gen object, that produces pseudorandom data to feed the
   *                 property tests.
   * @param property The property testing function, represented by a BiFunction, that takes a JSON configuration (JsObj)
   *                 and generated data of type O, and returns a TestResult indicating the success or failure of the
   *                 property test.
   * @param <O>      The type of the data generated by the Gen object and processed by the testing function.
   * @return A new Property instance representing the defined property.
   */
  public static <O> PropertyBuilder<O> of(final String name,
                                          final Gen<O> gen,
                                          final BiFunction<JsObj, O, TestResult> property
  ) {
    if (name == null || name.isBlank() || name.isEmpty()) {
      throw new IllegalArgumentException("property name missing");
    }
    BiLambda<JsObj, O, TestResult> bfn = (conf,
                                          o) -> IO.succeed(property.apply(conf,
                                                                          o));
    return new PropertyBuilder<>(name,
                                 gen,
                                 bfn);
  }

  /**
   * Creates a new Property instance that represents a property to be tested, modeled with a function. This method is
   * used to define a property with a specific name, data generator, and a function that tests the property. If you need
   * to pass configuration (JsObj) to the testing function, you can use the {@link #ofLambda(String, Gen, BiLambda)}
   * method instead.
   *
   * @param name     The name of the property, which provides a descriptive label for the property being tested.
   * @param gen      The data generator, represented by a Gen object, that produces pseudorandom data to feed the
   *                 property tests.
   * @param property The property testing function, represented by a Function, that takes generated data of type O and
   *                 returns a TestResult indicating the success or failure of the property test.
   * @param <O>      The type of the data generated by the Gen object and processed by the testing function.
   * @return A new Property instance representing the defined property.
   */
  public static <O> PropertyBuilder<O> of(final String name,
                                          final Gen<O> gen,
                                          final Function<O, TestResult> property
  ) {
    if (name == null || name.isBlank() || name.isEmpty()) {
      throw new IllegalArgumentException("property name missing");
    }
    BiLambda<JsObj, O, TestResult> bfn = (conf,
                                          o) -> IO.succeed(property.apply(o));
    return new PropertyBuilder<>(name,
                                 gen,
                                 bfn);
  }

  /**
   * Returns a new Property instance with the specified description.
   *
   * @param description The test description providing additional context for the property test.
   * @return A new Property instance with the specified description.
   * @throws IllegalArgumentException If the provided description is blank.
   */
  public PropertyBuilder<GenValue> withDescription(final String description) {
    this.description = requireNonNull(description);
    return this;
  }

  /**
   * Returns a new property instance that will be tested the specified number of times. Each execution of the property
   * test generates a new input value using the data generator, and the test is repeated for the specified number of
   * times with different input values.
   *
   * @param times the number of times an input is produced and tested on the property
   * @return a new property with the specified number of test executions
   * @throws IllegalArgumentException if {@code times} is less than 0
   */
  public PropertyBuilder<GenValue> withTimes(int times) {
    if (times < 0) {
      throw new IllegalArgumentException("times < 0");
    }
    this.times = times;
    return this;
  }

  /**
   * Specifies the path where the report of the property test result should be exported. If this method is called with a
   * valid path, the report will be copied to the specified location. The report is written as a JSON representation of
   * the test result.
   *
   * @param path The path where the report should be exported. The path must represent a regular file that exists on the
   *             file system.
   * @return This Property instance with the export path set.
   * @throws IllegalArgumentException If the provided path is not a regular file or does not exist.
   */
  public PropertyBuilder<GenValue> withReportPath(final Path path) {
    if (!Files.isRegularFile(requireNonNull(path))) {
      throw new IllegalArgumentException(String.format("%s is not a regular file",
                                                       path));
    }
    if (!Files.exists(path)) {
      throw new IllegalArgumentException(String.format("%s doesn't exist",
                                                       path));
    }
    this.reportPath = path;
    return this;
  }

  /**
   * The property will create a map of generated values and their counts and will be printed out on the console after
   * executing it
   *
   * @return this property
   */
  public PropertyBuilder<GenValue> withCollector() {
    this.collect = true;
    return this;
  }

  /**
   * Sets the classifiers for grouping generated values into different categories and assigns a default tag for values
   * that do not match any classifier. This feature can be particularly useful for identifying tags assigned to values
   * that produce errors.
   *
   * @param classifiers A map of classifiers, where each classifier is represented by a tag name and a predicate.
   * @param defaultTag  The default tag to be assigned to values that do not match any classifier.
   * @return This Property instance with updated classifiers and default tag.
   * @throws IllegalArgumentException If the provided classifiers map is empty.
   */
  public PropertyBuilder<GenValue> withClassifiers(final Map<String, Predicate<GenValue>> classifiers,
                                                   final String defaultTag
  ) {
    if (requireNonNull(classifiers).isEmpty()) {
      throw new IllegalArgumentException("classifiers empty");
    }
    Predicate<GenValue> defaultClassifier = genValue -> classifiers.values()
                                                                   .stream()
                                                                   .noneMatch(cla -> cla.test(genValue));

    Map<String, Predicate<GenValue>> xs = new HashMap<>(classifiers);
    xs.put(requireNonNull(defaultTag),
           defaultClassifier);
    this.classifiers = xs;
    return this;
  }

  /**
   * builds a property with the specified parameters
   *
   * @return a Property
   */
  @Override
  public Property<GenValue> get() {
    return new Property<>(name,
                          gen,
                          property,
                          description,
                          times,
                          reportPath,
                          collect,
                          classifiers);
  }
}