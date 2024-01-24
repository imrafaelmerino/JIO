package jio.jdbc;

import jio.IO;
import jio.Lambda;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

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
final class QueryStm<Params, Output> implements JdbcLambda<Params, List<Output>> {

  final Duration timeout;

  private final ResultSetMapper<Output> mapper;
  private final String sql;
  private final ParamsSetter<Params> setter;
  private final int fetchSize;
  private final boolean enableJFR;
  private final String label;

  /**
   * Constructs a {@code QueryStm} with specified parameters.
   *
   * @param sqlQuery  The SQL query to execute.
   * @param setter    The parameter setter for the SQL query.
   * @param mapper    The result set mapper for processing query results.
   * @param fetchSize The fetch size for the query results.
   * @param enableJFR Indicates whether to enable Java Flight Recorder integration.
   * @param label     The label to identify the statement
   */
  QueryStm(Duration timeout,
           String sqlQuery,
           ParamsSetter<Params> setter,
           ResultSetMapper<Output> mapper,
           int fetchSize,
           boolean enableJFR,
           String label) {
    this.timeout = timeout;
    this.sql = sqlQuery;
    this.mapper = mapper;
    this.setter = setter;
    this.fetchSize = fetchSize;
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
  public Lambda<Params, List<Output>> apply(DatasourceBuilder dsb) {
    return params -> IO.task(() -> {
                               try (var connection = dsb.get()
                                                        .getConnection()
                               ) {
                                 try (var ps = connection.prepareStatement(sql)) {
                                   return JfrEventDecorator.decorateQueryStm(() -> {
                                                                               var unused = setter.apply(params)
                                                                                                  .apply(ps);
                                                                               ps.setQueryTimeout((int) timeout.toSeconds());
                                                                               ps.setFetchSize(fetchSize);
                                                                               var rs = ps.executeQuery();
                                                                               List<Output> result = new ArrayList<>();
                                                                               while (rs.next()) {
                                                                                 result.add(mapper.apply(rs));
                                                                               }
                                                                               return result;
                                                                             },
                                                                             sql,
                                                                             enableJFR,
                                                                             label,
                                                                             fetchSize);
                                 }
                               }
                             },
                             Executors.newVirtualThreadPerTaskExecutor());
  }
}
