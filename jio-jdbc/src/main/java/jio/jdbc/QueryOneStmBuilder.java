package jio.jdbc;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Builder class for constructing instances of {@link QueryOneStm}, which represents a JDBC query operation returning a
 * single result. This builder allows customization of the SQL query, parameter setting, result mapping, and the option
 * to disable Java Flight Recorder (JFR) event recording for the query execution.
 *
 * @param <Params> The type of input parameters for the JDBC query.
 * @param <Output> The type of the output result for the JDBC query.
 */
public final class QueryOneStmBuilder<Params, Output> implements Supplier<JdbcLambda<Params, Output>> {

  private final String sqlQuery;
  private final Duration timeout;
  private final ParamsSetter<Params> setter;


  private final ResultSetMapper<Output> mapper;
  private boolean enableJFR = true;
  private String label;

  private QueryOneStmBuilder(String sqlQuery,
                             Duration timeout,
                             ParamsSetter<Params> setter,
                             ResultSetMapper<Output> mapper) {
    this.sqlQuery = Objects.requireNonNull(sqlQuery);
    this.timeout = Objects.requireNonNull(timeout);
    this.setter = Objects.requireNonNull(setter);
    this.mapper = Objects.requireNonNull(mapper);
  }

  /**
   * Creates a new instance of {@code QueryOneStmBuilder} with the specified SQL query, parameter setter, and result
   * mapper.
   *
   * @param sqlQuery The SQL query string.
   * @param setter   The parameter setter for the SQL query.
   * @param mapper   The result mapper for mapping query results.
   * @param timeout  The time the driver will wait for a statement to execute
   * @param <I>      The type of input parameters for the JDBC query.
   * @param <O>      The type of the output result for the JDBC query.
   * @return A new instance of {@code QueryOneStmBuilder}.
   */
  public static <I, O> QueryOneStmBuilder<I, O> of(String sqlQuery,
                                                   ParamsSetter<I> setter,
                                                   ResultSetMapper<O> mapper,
                                                   Duration timeout) {
    return new QueryOneStmBuilder<>(sqlQuery,
                                    timeout,
                                    setter,
                                    mapper);
  }

  /**
   * Sets a label for the Java Flight Recorder (JFR) event associated with this database query statement builder.
   * The label provides a descriptive identifier for the event and can be useful for tracking and analyzing events.
   *
   * @param label The label to be assigned to the JFR event.
   * @return This {@code QueryStmBuilder} instance with the specified event label.
   */
  public QueryOneStmBuilder<Params, Output> withEventLabel(String label){
    this.label = Objects.requireNonNull(label);
    return this;
  }

  /**
   * Disables the recording of Java Flight Recorder (JFR) events for the JDBC query execution.
   *
   * @return This {@code QueryOneStmBuilder} instance with JFR event recording disabled.
   */
  public QueryOneStmBuilder<Params, Output> withoutRecordedEvents() {
    this.enableJFR = false;
    return this;
  }

  /**
   * Builds and returns a new instance of {@link QueryOneStm} based on the configured parameters.
   *
   * @return A new instance of {@code QueryOneStm}.
   */
  @Override
  public JdbcLambda<Params, Output> get() {
    return new QueryOneStm<>(timeout,
                             sqlQuery,
                             setter,
                             mapper,
                             enableJFR,
                             label);
  }
}