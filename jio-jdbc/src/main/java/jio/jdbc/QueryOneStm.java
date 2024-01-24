package jio.jdbc;

import jio.IO;
import jio.Lambda;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * Represents a utility class for executing parameterized SQL queries and processing the results. Optionally integrates
 * with Java Flight Recorder for monitoring.
 *
 * <p>
 * Note: The operation is executed on a virtual thread.
 * </p>
 *
 * @param <Params> Type of the input parameters for the SQL query.
 * @param <Output> Type of the objects produced by the result set mapper.
 */
final class QueryOneStm<Params, Output> implements JdbcLambda<Params, Output> {

  final Duration timeout;

  private final ResultSetMapper<Output> mapper;
  private final String sql;
  private final Function<Params, StatementSetter> setter;
  private final boolean enableJFR;
  private final String label;

  /**
   * Constructs a {@code QueryStm} with specified parameters.
   *
   * @param sqlQuery  The SQL query to execute.
   * @param setter    The parameter setter for the SQL query.
   * @param mapper    The result set mapper for processing query results.
   * @param enableJFR Indicates whether to enable Java Flight Recorder integration.
   * @param label     The label to identify the query in Java Flight Recording
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
   * Applies the specified {@code DatasourceBuilder} to create a lambda function for executing the SQL query.
   *
   * @param dsb The datasource builder for obtaining database connections.
   * @return A lambda function for executing the SQL query and processing results.
   */
  @Override
  public Lambda<Params, Output> apply(DatasourceBuilder dsb) {
    return input -> IO.task(() -> {
                              try (var connection = dsb.get()
                                                       .getConnection()
                              ) {
                                try (var ps = connection.prepareStatement(sql)) {
                                  return JfrEventDecorator.decorateQueryOneStm(() -> {
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
}
