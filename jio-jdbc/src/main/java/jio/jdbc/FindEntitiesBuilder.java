package jio.jdbc;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
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
 * @param <Filter> The type of input elements for the query operation.
 * @param <Entity> The type of the output results from the query operation.
 */
public final class FindEntitiesBuilder<Filter, Entity> {

  private static final int DEFAULT_FETCH_SIZE = 1000;
  private final Duration timeout;

  private final String sqlQuery;
  private final ParamsSetter<Filter> setter;
  private final ResultSetMapper<List<Entity>> mapper;
  private int fetchSize = DEFAULT_FETCH_SIZE;

  private String label;
  private boolean enableJFR = true;

  private static final String SELECT_REGEX = "\\s*SELECT\\s+.*";
  private static final Pattern PATTERN = Pattern.compile(SELECT_REGEX,
                                                         Pattern.CASE_INSENSITIVE);

  /**
   * Constructs a QueryStmBuilder instance.
   *
   * @param timeout  query timeout
   * @param sqlQuery The SQL query statement for the query operation.
   * @param setter   A function to set parameters on a {@link java.sql.PreparedStatement}.
   * @param mapper   A function to map the result set to the desired output type.
   */
  private FindEntitiesBuilder(Duration timeout,
                              String sqlQuery,
                              ParamsSetter<Filter> setter,
                              ResultSetMapper<List<Entity>> mapper) {
    this.timeout = Objects.requireNonNull(timeout);
    this.sqlQuery = Objects.requireNonNull(sqlQuery);
    if (!PATTERN.matcher(sqlQuery)
                .matches()) {
      throw new IllegalArgumentException("`sql` must match the pattern `%s`".formatted(SELECT_REGEX));
    }
    this.setter = Objects.requireNonNull(setter);
    this.mapper = Objects.requireNonNull(mapper);
  }

  /**
   * Creates a new instance of QueryStmBuilder with the specified SQL query statement, parameter setter, and result
   * mapper.
   *
   * @param <Filter> The type of input elements for the query operation.
   * @param <Entity> The type of the output result from the query operation.
   * @param sqlQuery The SQL query statement for the query operation.
   * @param timeout  The time the driver will wait for a statement to execute
   * @param setter   A function to set parameters on a {@link java.sql.PreparedStatement}.
   * @param mapper   A function to map the result set to the desired output type.
   * @return A new instance of QueryStmBuilder.
   */
  public static <Filter, Entity> FindEntitiesBuilder<Filter, Entity> of(String sqlQuery,
                                                                        ParamsSetter<Filter> setter,
                                                                        ResultSetMapper<List<Entity>> mapper,
                                                                        Duration timeout) {
    return new FindEntitiesBuilder<>(timeout,
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
  public FindEntitiesBuilder<Filter, Entity> withFetchSize(int fetchSize) {
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
  public FindEntitiesBuilder<Filter, Entity> withEventLabel(String label) {
    this.label = Objects.requireNonNull(label);
    return this;
  }

  /**
   * Disables the recording of Java Flight Recorder (JFR) events for the JDBC query execution.
   *
   * @return This QueryStmBuilder instance with JFR event recording disabled.
   */
  public FindEntitiesBuilder<Filter, Entity> withoutRecordedEvents() {
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
   *         threads for improved concurrency and resource utilization.
   * @see FindEntities#buildAutoClosable(DatasourceBuilder)
   */
  public Lambda<Filter, List<Entity>> buildAutoClosable(DatasourceBuilder datasourceBuilder) {
    return new FindEntities<>(timeout,
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
   * @see FindEntities#buildClosable()
   */
  public ClosableStatement<Filter, List<Entity>> buildClosable() {
    return new FindEntities<>(timeout,
                              sqlQuery,
                              setter,
                              mapper,
                              fetchSize,
                              enableJFR,
                              label).buildClosable();
  }
}
