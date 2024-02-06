package jio.jdbc;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import jio.Lambda;

/**
 * Builder class for creating JDBC query operations in a JDBC context.
 *
 * <p>
 * This builder facilitates the creation of {@code QueryStm} instances for executing parameterized SQL queries,
 * processing the results, and mapping them to a specified output type. It provides methods to configure various aspects
 * of the query operation, such as the SQL statement, parameter setter, result set mapper, timeout, fetch size, and Java
 * Flight Recorder (JFR) event recording. The JDBC query operation is designed to be executed on virtual threads for
 * improved concurrency and resource utilization.
 * </p>
 *
 * @param <Params> The type of input elements for the query operation.
 * @param <Output> The type of the output results from the query operation.
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
   * Builds and returns a {@code Lambda} representing a JDBC query operation on a database. The lambda is configured to
   * bind parameters to its SQL, execute the query, and map the result. The JDBC connection is automatically obtained
   * from the datasource and closed, which means that it cannot be used for transactions where the connection can't be
   * closed before committing or doing rollback.
   *
   * @param datasourceBuilder The {@code DatasourceBuilder} used to obtain the datasource and connections.
   * @return A {@code Lambda} representing the JDBC query operation. Note: The operations are performed on virtual
   * threads for improved concurrency and resource utilization.
   * @see QueryStm#buildAutoClosable(DatasourceBuilder)
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
  /**
   * Builds and returns a {@code ClosableStatement} representing a JDBC query operation on a database. This method is
   * appropriate for use during transactions, where the connection needs to be managed externally. The lambda is configured
   * to bind parameters to its SQL, execute the query, and map the result. The operations are performed on virtual threads
   * for improved concurrency and resource utilization.
   *
   * @return A {@code ClosableStatement} representing the JDBC query operation with a duration, input, and output. Note:
   * The operations are performed on virtual threads for improved concurrency and resource utilization.
   * @see QueryStm#buildClosable()
   */
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
