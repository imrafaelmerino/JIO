package jio.jdbc;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import jio.Lambda;

/**
 * Builder class for creating JDBC query operations in a JDBC context.
 *
 * @param <Params> The type of input elements for the query operation.
 * @param <Output> The type of the output result from the query operation.
 */
public final class QueryStmBuilder<Params, Output> {

  private static final int DEFAULT_FETCH_SIZE = 1000;
  private final Duration timeout;

  private final String sqlQuery;
  private final ParamsSetter<Params> setter;
  private final ResultSetMapper<Output> mapper;
  private int fetchSize = DEFAULT_FETCH_SIZE;

  private String label;
  private boolean enableJFR = true;

  /**
   * Constructs a QueryStmBuilder instance.
   *
   * @param timeout  query timeout
   * @param sqlQuery The SQL query statement for the query operation.
   * @param setter   A function to set parameters on a {@link java.sql.PreparedStatement}.
   * @param mapper   A function to map the result set to the desired output type.
   */
  private QueryStmBuilder(Duration timeout,
                          String sqlQuery,
                          ParamsSetter<Params> setter,
                          ResultSetMapper<Output> mapper) {
    this.timeout = Objects.requireNonNull(timeout);
    this.sqlQuery = Objects.requireNonNull(sqlQuery);
    this.setter = Objects.requireNonNull(setter);
    this.mapper = Objects.requireNonNull(mapper);
  }

  /**
   * Creates a new instance of QueryStmBuilder with the specified SQL query statement, parameter setter, and result
   * mapper.
   *
   * @param <I>      The type of input elements for the query operation.
   * @param <O>      The type of the output result from the query operation.
   * @param sqlQuery The SQL query statement for the query operation.
   * @param timeout  The time the driver will wait for a statement to execute
   * @param setter   A function to set parameters on a {@link java.sql.PreparedStatement}.
   * @param mapper   A function to map the result set to the desired output type.
   * @return A new instance of QueryStmBuilder.
   */
  public static <I, O> QueryStmBuilder<I, O> of(String sqlQuery,
                                                ParamsSetter<I> setter,
                                                ResultSetMapper<O> mapper,
                                                Duration timeout) {
    return new QueryStmBuilder<>(timeout,
                                 sqlQuery,
                                 setter,
                                 mapper);
  }

  /**
   * Sets the fetch size for the JDBC query operation.
   *
   * @param fetchSize The fetch size to be set. Must be greater than 0.
   * @return This QueryStmBuilder instance with the specified fetch size.
   * @throws IllegalArgumentException If the fetch size is less than or equal to 0.
   */
  public QueryStmBuilder<Params, Output> withFetchSize(int fetchSize) {
    if (fetchSize <= 0) {
      throw new IllegalArgumentException("fetchSize <= 0");
    }
    this.fetchSize = fetchSize;
    return this;
  }

  /**
   * Sets a label for the Java Flight Recorder (JFR) event associated with this database query statement builder. The
   * label provides a descriptive identifier for the event and can be useful for tracking and analyzing events.
   *
   * @param label The label to be assigned to the JFR event.
   * @return This {@code QueryStmBuilder} instance with the specified event label.
   */
  public QueryStmBuilder<Params, Output> withEventLabel(String label) {
    this.label = Objects.requireNonNull(label);
    return this;
  }

  /**
   * Disables the recording of Java Flight Recorder (JFR) events for the JDBC query execution.
   *
   * @return This QueryStmBuilder instance with JFR event recording disabled.
   */
  public QueryStmBuilder<Params, Output> withoutRecordedEvents() {
    this.enableJFR = false;
    return this;
  }

  /**
   * Builds a JdbcLambda representing the JDBC query operation based on the specified settings.
   *
   * @return A JdbcLambda instance for the JDBC query operation.
   */
  public Lambda<Params, List<Output>> buildAutoClosable(DatasourceBuilder datasourceBuilder) {
    return new QueryStm<>(timeout,
                          sqlQuery,
                          setter,
                          mapper,
                          fetchSize,
                          enableJFR,
                          label).buildAutoClosable(datasourceBuilder);
  }

  public ClosableStatement<Params, List<Output>> buildClosable() {
    return new QueryStm<>(timeout,
                          sqlQuery,
                          setter,
                          mapper,
                          fetchSize,
                          enableJFR,
                          label).buildClosable();
  }
}
