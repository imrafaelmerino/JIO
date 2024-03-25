package jio.jdbc;

import java.sql.Connection;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import jio.IO;
import jio.Lambda;

/**
 * A class representing a query update statement in a relational database using JDBC. The class is designed to execute
 * an SQL query, bind parameters to the SQL and map the result-set into an object. The operation, by default, creates a
 * Java Flight Recorder (JFR) event.
 *
 * @param <Filter>  The type of the input object for setting parameters in the SQL.
 * @param <Entity>> The type of the output object, mapped from the ResultSet.
 * @see FindOneEntity for using queries that retrieve at most one row from the database
 */
final class FindEntities<Filter, Entity> {

  /**
   * Represents the maximum time in seconds that the SQL execution should wait.
   */
  final Duration timeout;

  private final ResultSetMapper<List<Entity>> mapper;
  /**
   * The SQL update statement.
   */
  private final String sql;
  /**
   * The parameter setter for binding parameters in the SQL.
   */
  private final ParamsSetter<Filter> setter;
  /**
   * The fetch size for the query results.
   */
  private final int fetchSize;
  /**
   * Flag indicating whether Java Flight Recorder (JFR) events should be enabled.
   */
  private final boolean enableJFR;
  /**
   * The label to identify the update statement in Java Flight Recording. It is used as a field in the JFR event for
   * distinguishing operations from each other.
   */
  private final String label;

  /**
   * Constructs a {@code QueryStm} with specified parameters.
   *
   * @param timeout   The maximum time in seconds that the SQL execution should wait.
   * @param sql       The SQL query to execute.
   * @param setter    The parameter setter for the SQL query.
   * @param mapper    The result-set mapper for processing query results.
   * @param fetchSize The fetch size for the query results.
   * @param enableJFR Indicates whether to enable Java Flight Recorder integration.
   * @param label     The label to identify the query statement.
   */
  FindEntities(Duration timeout,
               String sql,
               ParamsSetter<Filter> setter,
               ResultSetMapper<List<Entity>> mapper,
               int fetchSize,
               boolean enableJFR,
               String label) {
    this.timeout = timeout;
    this.sql = sql;
    this.mapper = mapper;
    this.setter = setter;
    this.fetchSize = fetchSize;
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
   *         virtual threads.
   * @see #buildClosable() for using query statements during transactions
   */
  Lambda<Filter, List<Entity>> buildAutoClosable(DatasourceBuilder datasourceBuilder) {
    return params -> {
      Callable<List<Entity>> callable = () -> {
        try (var connection = datasourceBuilder.get()
                                               .getConnection()
        ) {
          return find(params,
                      connection);
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
  ClosableStatement<Filter, List<Entity>> buildClosable() {
    return (params,
            connection) -> {
      Callable<List<Entity>> callable = () -> find(params,
                                                   connection);
      return IO.task(callable);
    };
  }

  private List<Entity> find(final Filter params,
                            final Connection connection) throws Exception {
    try (var ps = connection.prepareStatement(sql)) {
      return JfrEventDecorator.decorateQueryStm(
                                                () -> {
                                                  var unused = setter.apply(params)
                                                                     .apply(ps);
                                                  ps.setQueryTimeout((int) timeout.toSeconds());
                                                  ps.setFetchSize(fetchSize);
                                                  var rs = ps.executeQuery();
                                                  return mapper.apply(rs);
                                                },
                                                sql,
                                                enableJFR,
                                                label,
                                                fetchSize);
    }
  }
}
