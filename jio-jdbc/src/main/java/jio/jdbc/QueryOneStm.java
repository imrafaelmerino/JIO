package jio.jdbc;

import jio.IO;
import jio.Lambda;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * Represents a utility class for executing parameterized SQL queries and processing the results, specifically designed
 * for queries that retrieve at most one row from the database. Optionally integrates with Java Flight Recorder (JFR)
 * for monitoring.
 *
 * <p>
 * Note: The operation is executed on a virtual thread.
 * </p>
 *
 * @param <Params> Type of the input parameters for the SQL query.
 * @param <Output> Type of the object produced by the result set mapper.
 */
final class QueryOneStm<Params, Output> {

  /**
   * Represents the maximum time in seconds that the SQL execution should wait.
   */
  final Duration timeout;


  /**
   * The result set mapper for processing query results.
   */
  private final ResultSetMapper<Output> mapper;

  /**
   * The SQL query to execute.
   */
  private final String sql;

  /**
   * The parameter setter for the SQL query.
   */
  private final Function<Params, StatementSetter> setter;
  /**
   * Flag indicating whether Java Flight Recorder (JFR) events should be enabled.
   */
  private final boolean enableJFR;
  /**
   * The label to identify the query in Java Flight Recording.
   */
  private final String label;

  /**
   * Constructs a {@code QueryOneStm} with specified parameters.
   *
   * @param timeout   The maximum time in seconds that the SQL execution should wait.
   * @param sqlQuery  The SQL query to execute.
   * @param setter    The parameter setter for the SQL query.
   * @param mapper    The result-set mapper for processing query results.
   * @param enableJFR Indicates whether to enable Java Flight Recorder integration.
   * @param label     The label to identify the query in Java Flight Recording.
   */
  QueryOneStm(Duration timeout,
              String sqlQuery,
              ParamsSetter<Params> setter,
              ResultSetMapper<Output> mapper,
              boolean enableJFR,
              String label) {
    this.timeout = timeout;
    this.sql = sqlQuery;
    this.mapper = mapper;
    this.setter = setter;
    this.enableJFR = enableJFR;
    this.label = label;
  }


  /**
   * Creates a {@code Lambda} representing a query operation on a database. The lambda is configured to bind parameters
   * to its sql, execute the query, and map the result. The JDBC connection is automatically obtained from the
   * datasource and closed, which means that con not be used * for transactions where the connection can't be closed
   * before committing o doing rollback.
   *
   * @param datasourceBuilder The {@code DatasourceBuilder} used to obtain the datasource and connections.
   * @return A {@code Lambda} that, when invoked, performs the query operation. Note: The operations are performed by
   * virtual threads.
   * @see #buildClosable() for using query statements during transactions
   */
  Lambda<Params, Output> buildAutoClosable(DatasourceBuilder datasourceBuilder) {
    return input ->
        IO.task(() -> {
                  try (var connection = datasourceBuilder.get()
                                                         .getConnection()
                  ) {
                    try (var ps = connection.prepareStatement(sql)) {
                      return JfrEventDecorator.decorateQueryOneStm(
                          () -> {
                            var unused = setter.apply(input)
                                               .apply(ps);
                            ps.setQueryTimeout((int) timeout.toSeconds());
                            ps.setFetchSize(1);
                            var rs = ps.executeQuery();
                            return rs.next() ? mapper.apply(rs) : null;
                          },
                          sql,
                          enableJFR,
                          label);
                    }
                  }
                },
                Executors.newVirtualThreadPerTaskExecutor());
  }

  /**
   * Builds a closable query, allowing custom handling of the JDBC connection. This method is appropriate for use during
   * transactions, where the connection needs to be managed externally. The lambda is configured to bind parameters to
   * its SQL, execute the query, and map the result.
   *
   * @return A {@code ClosableStatement} representing the query operation with a duration, input, and output. Note: The
   * operations are performed by virtual threads.
   */
  ClosableStatement<Params, Output> buildClosable() {
    return (params, connection) ->
        IO.task(() -> {
                  try (var ps = connection.prepareStatement(sql)) {
                    return JfrEventDecorator.decorateQueryOneStm(
                        () -> {
                          var unused = setter.apply(params)
                                             .apply(ps);
                          ps.setQueryTimeout((int) timeout.toSeconds());
                          ps.setFetchSize(1);
                          var rs = ps.executeQuery();
                          return rs.next() ? mapper.apply(rs) : null;
                        },
                        sql,
                        enableJFR,
                        label);

                  }
                },
                Executors.newVirtualThreadPerTaskExecutor());
  }
}
