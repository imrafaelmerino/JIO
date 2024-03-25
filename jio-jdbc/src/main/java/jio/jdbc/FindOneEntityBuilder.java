package jio.jdbc;

import java.time.Duration;
import java.util.Objects;
import java.util.regex.Pattern;
import jio.Lambda;

/**
 * Builder class for constructing instances of {@link FindOneEntity}, which represents a JDBC query operation returning
 * a single result. This builder allows customization of the SQL query, parameter setting, result mapping, and the
 * option to disable Java Flight Recorder (JFR) event recording for the query execution.
 *
 * @param <Filter> The type of input parameters for the JDBC query.
 * @param <Entity> The type of the output result for the JDBC query.
 */
public final class FindOneEntityBuilder<Filter, Entity> {

  private static final int DEFAULT_FETCH_SIZE = 1000;

  private final String sqlQuery;
  private int fetchSize = DEFAULT_FETCH_SIZE;
  private final Duration timeout;
  private final ParamsSetter<Filter> setter;

  private final ResultSetMapper<Entity> mapper;
  private boolean enableJFR = true;
  private String label;

  private static final String SELECT_REGEX = "\\s*SELECT\\s+.*";
  private static final Pattern PATTERN = Pattern.compile(SELECT_REGEX,
                                                         Pattern.CASE_INSENSITIVE);

  private FindOneEntityBuilder(String sqlQuery,
                               Duration timeout,
                               ParamsSetter<Filter> setter,
                               ResultSetMapper<Entity> mapper) {
    this.sqlQuery = Objects.requireNonNull(sqlQuery);
    if (!PATTERN.matcher(sqlQuery)
                .matches()) {
      throw new IllegalArgumentException("`sql` must match the pattern `%s`".formatted(SELECT_REGEX));
    }
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
  public static <I, O> FindOneEntityBuilder<I, O> of(String sqlQuery,
                                                     ParamsSetter<I> setter,
                                                     ResultSetMapper<O> mapper,
                                                     Duration timeout) {
    return new FindOneEntityBuilder<>(sqlQuery,
                                      timeout,
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
  public FindOneEntityBuilder<Filter, Entity> withFetchSize(int fetchSize) {
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
  public FindOneEntityBuilder<Filter, Entity> withEventLabel(String label) {
    this.label = Objects.requireNonNull(label);
    return this;
  }

  /**
   * Disables the recording of Java Flight Recorder (JFR) events for the JDBC query execution.
   *
   * @return This {@code QueryOneStmBuilder} instance with JFR event recording disabled.
   */
  public FindOneEntityBuilder<Filter, Entity> withoutRecordedEvents() {
    this.enableJFR = false;
    return this;
  }

  /**
   * Builds and returns a {@code Lambda} representing the JDBC query operation configured with the specified settings.
   * The resulting lambda is suitable for automatic resource management (ARM) and is configured to execute the query,
   * process the result, and close the associated JDBC resources. The operations are performed on virtual threads for
   * improved concurrency and resource utilization.
   *
   * @param datasourceBuilder The {@code DatasourceBuilder} used to obtain the datasource and connections.
   * @return A {@code Lambda} representing the JDBC query operation with a duration, input, and output. Note: The
   *         operations are performed on virtual threads for improved concurrency and resource utilization.
   * @see FindOneEntity#buildAutoClosable(DatasourceBuilder)
   */
  public Lambda<Filter, Entity> buildAutoClosable(DatasourceBuilder datasourceBuilder) {
    return new FindOneEntity<>(timeout,
                               sqlQuery,
                               setter,
                               mapper,
                               fetchSize,
                               enableJFR,
                               label)
                                     .buildAutoClosable(datasourceBuilder);
  }

  /**
   * Builds and returns a {@code ClosableStatement} representing a JDBC query operation on a database. This method is
   * appropriate for use during transactions, where the connection needs to be managed externally. The lambda is
   * configured to bind parameters to its SQL, execute the query, and map the result. The operations are performed on
   * virtual threads for improved concurrency and resource utilization.
   *
   * @return A {@code ClosableStatement} representing the JDBC query operation with a duration, input, and output. Note:
   *         The operations are performed on virtual threads for improved concurrency and resource utilization.
   * @see FindOneEntity#buildClosable()
   */
  public ClosableStatement<Filter, Entity> buildClosable() {
    return new FindOneEntity<>(timeout,
                               sqlQuery,
                               setter,
                               mapper,
                               fetchSize,
                               enableJFR,
                               label).buildClosable();
  }
}