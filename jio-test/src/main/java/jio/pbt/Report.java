package jio.pbt;

import jsonvalues.*;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents the result of the execution of a {@link Property property}.
 * A report can be serialized into a Json with the method {@link #toJson()}. It contains
 * the following information:
 * <ul>
 *   <li>The number of executed tests</li>
 *   <li>The name of the property</li>
 *   <li>The description of the property</li>
 *   <li>Instant when the execution started</li>
 *   <li>The average execution time (in nanoseconds)</li>
 *   <li>The maximum execution time (in nanoseconds)</li>
 *   <li>The minimum execution time (in nanoseconds)</li>
 *   <li>The accumulative time (in nanoseconds)</li>
 *   <li>The number of failures</li>
 *   <li>The number of exceptions</li>
 * </ul>
 */

public final class Report {

    private final String propName;
    private final String propDescription;

    private int tests;
    private long avgTime;
    private long maxTime = Long.MIN_VALUE;
    private long minTime = Long.MAX_VALUE;
    private long accumulativeTime;
    private final List<FailureContext> failures = new ArrayList<>();
    private final List<ExceptionContext> exceptions = new ArrayList<>();


    /**
     * the name of the property
     *
     * @return the name of the property
     */
    public String getPropName() {
        return propName;
    }

    /**
     * the average time in ms needed to execute a test
     *
     * @return the average in ms time needed to execute a test
     */
    public long getAvgTime() {
        return avgTime;
    }

    /**
     * the maximum time in ms needed to execute a test
     *
     * @return the maximum in ms time needed to execute a test
     */
    public long getMaxTime() {
        return maxTime;
    }

    /**
     * the minimum time in ms needed to execute a test
     *
     * @return the minimum time in ms needed to execute a test
     */
    public long getMinTime() {
        return minTime;
    }

    /**
     * the accumulative time in ms spent on executing all the tests
     *
     * @return the accumulative time in ms spent on executing all the tests
     */
    public long getAccumulativeTime() {
        return accumulativeTime;
    }


    /**
     * the number of failures
     *
     * @return the number of failures
     */
    public List<FailureContext> getFailures() {
        return failures;
    }

    /**
     * the number of exceptions
     *
     * @return the number of exceptions
     */
    public List<ExceptionContext> getExceptions() {
        return exceptions;
    }

    void tac(Instant tic) {

        long duration = Duration.between(tic,
                                         Instant.now()
                                        ).toNanos();
        if (duration > maxTime) {
            maxTime = duration;
        }
        if (duration < minTime) {
            minTime = duration;
        }

        this.accumulativeTime += duration;
        this.avgTime = accumulativeTime / tests;
    }

    Report(final String name,
           final String description
          ) {
        this.propName = name;
        this.propDescription = description;
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
     *     {@code
     *
     *     JsObjSpec.of("n_tests",integer,
     *                  "n_failures", integer,
     *                  "n_exceptions", integer,
     *                  "property_name", string,
     *                  "property_description", string,
     *                  "avg_time", long,
     *                  "max_time", long,
     *                  "min_time", long,
     *                  "accumulative_time", long,
     *                  "failures", arrayOf(JsObjSpec.of("reason",string,
     *                                                   "context", JsObj
     *                                                  ),
     *                  "exceptions", arrayOf(JsObjSpec.of("message", string,
     *                                                     "type", string,
     *                                                     "stacktrace", array
     *                                                    )
     *                                       )
     *                 )
     *     }
     * </pre>
     *
     * @return a Json representing this report
     */
    public JsObj toJson() {
        return JsObj.of("n_tests", JsInt.of(tests),
                        "n_failures", JsInt.of(failures.size()),
                        "n_exceptions", JsInt.of(exceptions.size()),
                        "property_name", JsStr.of(propName),
                        "property_description", JsStr.of(propDescription),
                        "avg_time", JsLong.of(avgTime),
                        "max_time", JsLong.of(maxTime),
                        "min_time", JsLong.of(minTime),
                        "accumulative_time", JsLong.of(accumulativeTime),
                        "failures", JsArray.ofIterable(failures.stream()
                                                               .map(FailureContext::toJson)
                                                               .toList()
                                                      ),
                        "exceptions", JsArray.ofIterable(exceptions.stream()
                                                                   .map(ExceptionContext::toJson)
                                                                   .toList())
                       );
    }

    Report aggregatePar(Report other) {
        final Report result = aggregateCommon(other);

        result.accumulativeTime = Math.max(accumulativeTime,
                                           other.accumulativeTime
                                          );

        return result;
    }

    private Report aggregateCommon(Report other) {
        final Report result = new Report(propName, propDescription);

        result.avgTime = (avgTime + other.avgTime) / 2;
        result.minTime = Math.min(minTime,
                                  other.minTime
                                 );
        result.maxTime = Math.max(maxTime,
                                  other.maxTime
                                 );
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


    public void assertAllSuccess() {

        Assertions.assertTrue(getExceptions().isEmpty() && getFailures().isEmpty(),
                              () -> {
                                  if (getExceptions().isEmpty()) return "Report with failures: " + this.toJson();
                                  if (getFailures().isEmpty()) return "Report with exceptions: " + this.toJson();
                                  return "Report with failures and exceptions: " + this.toJson();
                              }
                             );
    }

    public void assertNoFailures() {

        Assertions.assertTrue(getFailures()
                                      .isEmpty(),
                              () -> "Report with failures: " + this.toJson()
                             );
    }


    public void assertThat(Predicate<Report> condition,
                           Supplier<String> message
                          ) {

        Assertions.assertTrue(condition.test(this),
                              message
                             );
    }
}
