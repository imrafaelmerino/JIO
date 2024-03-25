package jio.test.pbt;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import jio.time.Fun;
import jsonvalues.JsArray;
import jsonvalues.JsInstant;
import jsonvalues.JsInt;
import jsonvalues.JsLong;
import jsonvalues.JsObj;
import jsonvalues.JsStr;
import org.junit.jupiter.api.Assertions;

/**
 * Represents the result of the execution of a property-based test ({@link Property}). A report can be serialized into
 * JSON format with the method {@link #toJson()}. It contains detailed information about the test execution, including
 * the number of executed tests, the name and description of the property, execution time statistics, failures, and
 * exceptions.
 *
 * <p>Report Contents:</p>
 * <ul>
 * <li>The number of executed tests</li>
 * <li>The name of the property</li>
 * <li>The description of the property</li>
 * <li>Instant when the execution started</li>
 * <li>The average execution time (in nanoseconds)</li>
 * <li>The maximum execution time (in nanoseconds)</li>
 * <li>The minimum execution time (in nanoseconds)</li>
 * <li>The accumulative time (in nanoseconds)</li>
 * <li>The number of failures</li>
 * <li>The number of exceptions</li>
 * </ul>
 *
 * <p>Reports are typically used to track the results of property-based tests and can be
 * aggregated to summarize the overall test suite performance.</p>
 *
 * @see Property
 * @see FailureContext
 * @see ExceptionContext
 */

public final class Report {

  private final String propName;
  private final String propDescription;
  private final List<FailureContext> failures = new ArrayList<>();
  private final List<ExceptionContext> exceptions = new ArrayList<>();
  /**
   * map that holds the tags of the generated values and the number of times they appear. This map is fed only when
   * classifiers are specified
   */
  private final Map<String, Long> tagsCounter = new HashMap<>();
  /**
   * map that holds the generated values and the number of times they are generated
   */
  private final Map<String, Long> valuesCounter = new HashMap<>();
  private int tests;
  private long avgTime;
  private long maxTime = Long.MIN_VALUE;
  private long minTime = Long.MAX_VALUE;
  private long accumulativeTime;
  private Instant startTime;
  private Instant endTime;

  Report(final String name,
         final String description
  ) {
    this.propName = name;
    this.propDescription = description;
  }

  private static String addTagToVal(Context it) {
    Object input = it.input();
    String str = input == null ? "null" : input.toString();
    String tags = it.tags();
    return (tags != null && !tags.isEmpty()) ? String.format("(%s, %s)",
                                                             str,
                                                             tags) : str;
  }

  /**
   * Get the name of the property associated with this report.
   *
   * @return The name of the property.
   */
  public String getPropName() {
    return propName;
  }

  /**
   * Get the average execution time (in milliseconds) needed to execute a single test.
   *
   * @return The average execution time in milliseconds.
   */
  public long getAvgTime() {
    return avgTime;
  }

  /**
   * Get the maximum execution time (in milliseconds) among all executed tests.
   *
   * @return The maximum execution time in milliseconds.
   */
  public long getMaxTime() {
    return maxTime;
  }

  /**
   * Get the minimum execution time (in milliseconds) among all executed tests.
   *
   * @return The minimum execution time in milliseconds.
   */
  public long getMinTime() {
    return minTime;
  }

  /**
   * Get the accumulative execution time (in milliseconds) spent on executing all tests.
   *
   * @return The accumulative execution time in milliseconds.
   */
  public long getAccumulativeTime() {
    return accumulativeTime;
  }

  /**
   * Get a list of failure contexts, containing information about failed tests.
   *
   * @return A list of failure contexts.
   */
  public List<FailureContext> getFailures() {
    return failures;
  }

  /**
   * Get a list of exception contexts, containing information about tests that threw exceptions.
   *
   * @return A list of exception contexts.
   */
  public List<ExceptionContext> getExceptions() {
    return exceptions;
  }

  void tac(Instant tic) {

    long duration = Duration.between(tic,
                                     Instant.now())
                            .toNanos();
    if (duration > maxTime) {
      maxTime = duration;
    }
    if (duration < minTime) {
      minTime = duration;
    }

    this.accumulativeTime += duration;
    this.avgTime = accumulativeTime / tests;
  }

  void addFailure(final FailureContext failure) {
    failures.add(failure);
  }

  void addException(final ExceptionContext exceptionContext) {
    exceptions.add(exceptionContext);
  }

  void incTest() {
    tests++;
  }

  /**
   * returns the instant when tests started execution
   *
   * @return the instant when tests started execution
   */
  public Instant getStartTime() {
    return startTime;
  }

  void setStartTime(Instant startTime) {
    this.startTime = startTime;
  }

  /**
   * returns the instant when tests ended execution
   *
   * @return the instant when tests ended execution
   */
  public Instant getEndTime() {
    return endTime;
  }

  void setEndTime(Instant endTime) {
    this.endTime = endTime;
  }

  /**
   * returns a string representation of the report in a json format
   *
   * @return string representation in a json format
   * @see #toJson()
   */
  @Override
  public String toString() {
    return toJson().toString();
  }

  /**
   * serializes this report into a Json with the following schema:
   *
   * <pre>
   * {@code
   *
   *     JsObjSpec.of( "n_tests",integer,
   *                   "name",string
   *                   "n_failures", integer,
   *                   "n_exceptions", integer,
   *                   "property_name", string,
   *                   "description", string,
   *                   "start_time", instant,
   *                   "end_time", instant,
   *                   "avg_time", long,
   *                   "max_time", long,
   *                   "min_time", long,
   *                   "accumulative_time", long,
   *                   "failures", arrayOf(JsObjSpec.of("reason",string,
   *                                                    "context", JsObj
   *                                                   )),
   *                   "exceptions", arrayOf(JsObjSpec.of("message", string,
   *                                                      "type", string,
   *                                                      "stacktrace", array
   *                                                      ))
   *                 )
   *     }
   * </pre>
   *
   * @return a Json representing this report
   */
  public JsObj toJson() {
    return JsObj.of(
                    "name",
                    JsStr.of(propName),
                    "n_tests",
                    JsInt.of(tests),
                    "n_failures",
                    JsInt.of(failures.size()),
                    "n_exceptions",
                    JsInt.of(exceptions.size()),
                    "description",
                    JsStr.of(propDescription),
                    "start_time",
                    JsInstant.of(startTime),
                    "end_time",
                    JsInstant.of(endTime),
                    "avg_time",
                    JsLong.of(avgTime),
                    "max_time",
                    JsLong.of(maxTime),
                    "min_time",
                    JsLong.of(minTime),
                    "accumulative_time",
                    JsLong.of(accumulativeTime),
                    "failures",
                    JsArray.ofIterable(failures.stream()
                                               .map(FailureContext::toJson)
                                               .toList()
                    ),
                    "exceptions",
                    JsArray.ofIterable(exceptions.stream()
                                                 .map(ExceptionContext::toJson)
                                                 .toList())
    );
  }

  Report aggregatePar(Report other) {
    final Report result = aggregateCommon(other);

    result.accumulativeTime = Math.max(accumulativeTime,
                                       other.accumulativeTime);

    return result;
  }

  private Report aggregateCommon(Report other) {
    final Report result = new Report(propName,
                                     propDescription);
    result.setStartTime(startTime.isBefore(other.startTime) ? startTime : other.startTime);
    result.setEndTime(endTime.isAfter(other.endTime) ? endTime : other.endTime);
    result.avgTime = (avgTime + other.avgTime) / 2;
    result.minTime = Math.min(minTime,
                              other.minTime);
    result.maxTime = Math.max(maxTime,
                              other.maxTime);
    result.tests = tests + other.tests;
    var exceptions = new ArrayList<>(this.exceptions);
    exceptions.addAll(other.exceptions);
    result.exceptions.addAll(exceptions);
    var failures = new ArrayList<>(this.failures);
    failures.addAll(other.failures);
    result.failures.addAll(failures);
    return result;
  }

  Report aggregate(Report other) {
    final Report result = aggregateCommon(other);
    result.accumulativeTime = accumulativeTime + other.accumulativeTime;
    return result;
  }

  /**
   * Assert that all tests associated with this report have passed successfully. If there are any failures or
   * exceptions, this assertion will fail.
   */
  public void assertAllSuccess() {

    Assertions.assertTrue(getExceptions().isEmpty() && getFailures().isEmpty(),
                          () -> {
                            if (getExceptions().isEmpty()) {
                              return String.format("Property %s with failures. JSON report: ",
                                                   propName) + this.toJson();
                            }
                            if (getFailures().isEmpty()) {
                              return String.format("Property %s with exceptions. JSON report: ",
                                                   propName) + this.toJson();
                            }
                            return String.format("Property %s with failures and exceptions. JSON report: ",
                                                 propName) + this.toJson();
                          }
    );
  }

  /**
   * Assert that there are no failures associated with this report. If there are any failures, this assertion will
   * fail.
   */
  public void assertNoFailures() {

    Assertions.assertTrue(getFailures()
                                       .isEmpty(),
                          () -> String.format("Property %s with failures: ",
                                              propName) + this.toJson()
    );
  }

  /**
   * Perform a custom assertion on the report using a provided condition and message supplier.
   *
   * @param condition A predicate condition to evaluate the report.
   * @param message   A supplier that provides a message to be used if the condition fails.
   */
  public void assertThat(Predicate<Report> condition,
                         Supplier<String> message
  ) {

    Assertions.assertTrue(condition.test(this),
                          message
    );
  }

  /**
   * Print a summary of the report to the console, including test execution details and results.
   */
  public void summarize() {
    synchronized (System.out) {
      if (tests == 0) {
        throw new RuntimeException("No test was executed or incTest method wasn't called");
      }
      System.out.printf("Property %s executed %s times at %s for %s:%n",
                        propName,
                        tests,
                        startTime.atZone(ZoneId.systemDefault())
                                 .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        Fun.formatTime(accumulativeTime));
      if (getExceptions().isEmpty() && getFailures().isEmpty()) {
        System.out.printf("  + OK, passed %d tests.\n",
                          tests
        );
      } else if (getExceptions().isEmpty()) {
        System.out.printf("  ! KO, passed %d tests (%s) and %d (%s) ended with a failure.\n",
                          tests - getFailures().size(),
                          calculatePer(tests - getFailures().size()),
                          getFailures().size(),
                          calculatePer(getFailures().size())
        );
        printFailuresValues();
      } else if (getFailures().isEmpty()) {
        System.out.printf("  ! KO, passed %d tests (%s) and %d (%s) ended with a exception.\n",
                          tests - getExceptions().size(),
                          calculatePer(tests),
                          getExceptions().size(),
                          0
        );
        printExceptionsValues();
      } else {
        System.out.printf(
                          "  ! KO, passed %d tests (%s), %d (%s) ended with a failure and %d (%s) ended with a exception.\n",
                          tests - getExceptions().size() - getFailures().size(),
                          calculatePer(tests - getExceptions().size() - getFailures().size()),
                          getFailures().size(),
                          calculatePer(getFailures().size()),
                          getExceptions().size(),
                          calculatePer(getExceptions().size())
        );
        printFailuresValues();
        printExceptionsValues();
      }

      if (!valuesCounter.isEmpty() || !tagsCounter.isEmpty()) {
        System.out.println("  Distribution of collected values:");
      }

      if (!valuesCounter.isEmpty()) {
        printCounter(valuesCounter);
      }
      if (!tagsCounter.isEmpty()) {
        printCounter(tagsCounter);
      }

      System.out.flush();
    }
  }

  private void printFailuresValues() {
    System.out.println("  Some generated values that caused a failure:");
    var failureValues = getFailures()
                                     .stream()
                                     .limit(20)
                                     .map(it -> addTagToVal(it.context()))
                                     .collect(Collectors.joining(","));

    System.out.println("   " + failureValues);
  }

  private void printExceptionsValues() {
    System.out.println("  Some generated values that caused an exception:");

    var failureValues = getExceptions()
        .stream()
        .limit(20)
        .map(it -> addTagToVal(it.context()))
        .collect(Collectors.joining(","));

    System.out.println(STR."   \{failureValues}");
    System.out.println();
  }

  void classify(final String tags) {
    if (!tags.isEmpty()) {
      tagsCounter.compute(tags,
                          (_,
                           value) -> value == null ? 1 : value + 1);
    }
  }

  void collect(final String value) {
    valuesCounter.compute(value,
                          (_,
                           val) -> val == null ? 1 : val + 1);
  }

  private void printCounter(Map<?, Long> map) {
    map.forEach((key,
                 value) -> System.out.printf("     %s %s%n",
                                             calculatePer(value),
                                             key
                 ));
  }

  private String calculatePer(long n) {
    return String.format("%.1f %%",
                         ((double) n / tests) * 100);
  }
}
