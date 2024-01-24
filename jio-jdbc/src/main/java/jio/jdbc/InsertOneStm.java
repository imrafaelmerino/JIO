package jio.jdbc;

import jio.IO;
import jio.Lambda;

import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;

/**
 * A class representing a generic update operation with a generated key in a relational database using JDBC. The class
 * is designed to execute an SQL update statement, set parameters, and retrieve the generated key. The operation is
 * wrapped with Java Flight Recorder (JFR) events.
 *
 * <p>
 * Note: The operation is executed on a virtual thread.
 * </p>
 *
 * @param <Params> The type of the input object for setting parameters in the update statement.
 * @param <Output> The type of the output object generated from the ResultSet.
 */
final class InsertOneStm<Params, Output> implements JdbcLambda<Params, Output> {

  final Duration timeout;

  final String sql;

  final ParamsSetter<Params> setParams;

  final BiFunction<Params, Integer, ResultSetMapper<Output>> mapResult;
  private final boolean enableJFR;
  private final String label;

  InsertOneStm(Duration timeout,
               String sql,
               ParamsSetter<Params> setParams,
               BiFunction<Params, Integer, ResultSetMapper<Output>> mapResult,
               boolean enableJFR,
               String label) {
    this.timeout = timeout;
    this.sql = sql;
    this.setParams = setParams;
    this.mapResult = mapResult;
    this.enableJFR = enableJFR;
    this.label = label;
  }

  /**
   * Applies the update operation to a datasource, setting parameters, executing the update statement, and retrieving
   * the generated key from the ResultSet.
   *
   * @param dsb The {@code DatasourceBuilder} for obtaining the datasource.
   * @return A {@code BiLambda} representing the update operation with a duration, input, and output.
   */
  @Override
  public Lambda<Params, Output> apply(DatasourceBuilder dsb) {
    return params ->
        IO.task(() -> JfrEventDecorator.decorateInsertOneStm(() -> {
                                                               try (var connection = dsb.get()
                                                                                        .getConnection()
                                                               ) {
                                                                 try (var ps = connection.prepareStatement(sql,
                                                                                                           Statement.RETURN_GENERATED_KEYS)
                                                                 ) {
                                                                   ps.setQueryTimeout((int) timeout.toSeconds());
                                                                   int unused = setParams.apply(params)
                                                                                         .apply(ps);
                                                                   assert unused > 0;
                                                                   int numRowsAffected = ps.executeUpdate();
                                                                   try (ResultSet resultSet = ps.getGeneratedKeys()) {
                                                                     if (resultSet.next()) {
                                                                       return mapResult.apply(params,
                                                                                              numRowsAffected)
                                                                                       .apply(resultSet);
                                                                     }
                                                                     throw new ColumnNotGeneratedException(sql);
                                                                   }
                                                                 }
                                                               }
                                                             },
                                                             sql,
                                                             enableJFR,
                                                             label),
                Executors.newVirtualThreadPerTaskExecutor());
  }
}
