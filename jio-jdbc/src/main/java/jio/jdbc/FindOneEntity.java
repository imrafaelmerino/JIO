package jio.jdbc;

import java.sql.Connection;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Function;
import jio.IO;
import jio.Lambda;

/**
 * Represents a query operation that retrieves at most one entity from the database based on specified filters. This
 * class is designed for scenarios where the result set may contain one, zero, or multiple rows (representing the same
 * entity). It's important to note that handling this variability in the result set is the responsibility of the result
 * set mapper associated with the query.
 * <p>
 * Note: The operation is executed on a virtual thread.
 * </p>
 *
 * @param <Filter> Type of the input parameters for the SQL query.
 * @param <Entity> Type of the object produced by the result set mapper.
 */
final class FindOneEntity<Filter, Entity> {

  /**
   * Represents the maximum time in seconds that the SQL execution should wait.
   */
  final Duration timeout;

  /**
   * The result-set mapper for processing query results.
   */
  private final ResultSetMapper<Entity> mapper;

  /**
   * The SQL query to execute.
   */
  private final String sql;

  /**
   * The parameter setter for the SQL query.
   */
  private final Function<Filter, StatementSetter> setter;
  /**
   * Flag indicating whether Java Flight Recorder (JFR) events should be enabled.
   */
  private final boolean enableJFR;
  /**
   * The label to identify the query in Java Flight Recording.
   */
  private final String label;

  /**
   * The fetch size for the query results.
   */
  private final int fetchSize;

  /**
   * Constructs a {@code QueryOneStm} with specified parameters.
   *
   * @param timeout   The maximum time in seconds that the SQL execution should wait.
   * @param sqlQuery  The SQL query to execute.
   * @param setter    The parameter setter for the SQL query.
   * @param mapper    The result-set mapper for processing query results.
   * @param fetchSize The fetch size for the query results.
   * @param enableJFR Indicates whether to enable Java Flight Recorder integration.
   * @param label     The label to identify the query in Java Flight Recording.
   */
  FindOneEntity(Duration timeout,
                String sqlQuery,
                ParamsSetter<Filter> setter,
                ResultSetMapper<Entity> mapper,
                int fetchSize,
                boolean enableJFR,
                String label) {
    this.timeout = timeout;
    this.sql = sqlQuery;
    this.mapper = mapper;
    this.setter = setter;
    this.enableJFR = enableJFR;
    this.label = label;
    this.fetchSize = fetchSize;
  }

  /**
   * Creates a {@code Lambda} representing a query operation on a database. The lambda is configured to bind parameters
   * to its sql, execute the query, and map the result. The JDBC connection is automatically obtained from the
   * datasource and closed, which means that con not be used * for transactions where the connection can't be closed
   * before committing o doing rollback.
   *
   * @param datasourceBuilder The {@code DatasourceBuilder} used to obtain the datasource and connections.
   * @return A {@code Lambda} that, when invoked, performs the query operation. Note: The operations are performed by
   *         virtual threads.
   * @see #buildClosable() for using query statements during transactions
   */
  Lambda<Filter, Entity> buildAutoClosable(DatasourceBuilder datasourceBuilder) {
    return input -> {
      Callable<Entity> callable = () -> {
        try (var connection = datasourceBuilder.get()
                                               .getConnection()
        ) {
          return findOne(connection,
                         input,
                         fetchSize);
        }
      };
      return IO.task(callable);
    };
  }

  /**
   * Builds a closable query, allowing custom handling of the JDBC connection. This method is appropriate for use during
   * transactions, where the connection needs to be managed externally. The lambda is configured to bind parameters to
   * its SQL, execute the query, and map the result.
   *
   * @return A {@code ClosableStatement} representing the query operation with a duration, input, and output. Note: The
   *         operations are performed by virtual threads.
   */
  ClosableStatement<Filter, Entity> buildClosable() {
    return (filters,
            connection) -> {
      Callable<Entity> callable = () -> findOne(connection,
                                                filters,
                                                1);
      return IO.task(callable);
    };
  }

  private Entity findOne(final Connection connection,
                         final Filter input,
                         final int fetchSize) throws Exception {
    try (var ps = connection.prepareStatement(sql)) {
      return JfrEventDecorator.decorateQueryOneStm(
                                                   () -> {
                                                     var unused = setter.apply(input)
                                                                        .apply(ps);
                                                     ps.setQueryTimeout((int) timeout.toSeconds());
                                                     ps.setFetchSize(fetchSize);
                                                     var rs = ps.executeQuery();
                                                     return mapper.apply(rs);
                                                   },
                                                   sql,
                                                   enableJFR,
                                                   label);
    }
  }
}
