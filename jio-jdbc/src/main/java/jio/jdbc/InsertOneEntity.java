package jio.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Function;
import jio.IO;
import jio.Lambda;

/**
 * A class representing a generic insert operation with a generated key in a relational database using JDBC. The class
 * is designed to execute an SQL update statement, set parameters, and retrieve the generated key. The operation is
 * wrapped with Java Flight Recorder (JFR) events.
 *
 * <p>
 * Note: The operation is executed on a virtual thread.
 * </p>
 *
 * @param <Params> The type of the input object for setting parameters in the update statement.
 * @param <Output> The type of the output object generated from the ResultSet and the generated keys.
 */
final class InsertOneEntity<Params, Output> {

  /**
   * Represents the maximum time in seconds that the SQL execution should wait.
   */
  final Duration timeout;
  /**
   * The SQL update statement.
   */
  final String sql;
  /**
   * The parameter setter for binding parameters in the sql.
   */
  final ParamsSetter<Params> setter;

  /**
   * Mapper to produce an output from the rows affected (0 or 1), the input params and result-set containing the
   * generated keys
   */
  final Function<Params, ResultSetMapper<Output>> mapResult;

  /**
   * Flag indicating whether Java Flight Recorder (JFR) events should be enabled.
   */
  private final boolean enableJFR;

  /**
   * The label to identify the update statement in Java Flight Recording. It is used as a field in the JFR event for
   * distinguishing operations from each other.
   */
  private final String label;

  InsertOneEntity(Duration timeout,
                  String sql,
                  ParamsSetter<Params> setter,
                  Function<Params, ResultSetMapper<Output>> mapResult,
                  boolean enableJFR,
                  String label) {
    this.timeout = timeout;
    this.sql = sql;
    this.setter = setter;
    this.mapResult = mapResult;
    this.enableJFR = enableJFR;
    this.label = label;
  }

  /**
   * Creates a {@code Lambda} representing an insert statement operation on a database. The lambda is configured to bind
   * parameters to its sql, execute the insert statement, and map the rows affected (0 or 1) and the generated keys into
   * an output object. The JDBC connection is automatically obtained from the datasource and closed, which means that
   * con not be used for transactions where the connection can't be closed before committing o doing rollback.
   *
   * @param datasourceBuilder The {@code DatasourceBuilder} used to obtain the datasource and connections.
   * @return A {@code Lambda} representing the insert statement. Note: The operations are performed by virtual threads.
   * @see #buildClosable() for using insert statements during transactions
   */
  Lambda<Params, Output> buildAutoClosable(DatasourceBuilder datasourceBuilder) {
    return params -> {
      Callable<Output> callable = () -> {
        try (var connection = datasourceBuilder.get()
                                               .getConnection()
        ) {
          return insertOne(params,
                           connection);
        }
      };
      return IO.task(callable);
    };
  }

  /**
   * Builds a closable insert statement, allowing custom handling of the JDBC connection. This method is appropriate for
   * use during transactions, where the connection needs to be managed externally. The lambda is configured to bind
   * parameters to its SQL, execute the insert statement, and map the rows affected (0 or 1) and the generated keys into
   * an output object
   *
   * @return A {@code ClosableStatement} representing the insert statement. Note: The operations are performed by
   *         virtual threads.
   */
  ClosableStatement<Params, Output> buildClosable() {
    return (params,
            connection) -> {
      Callable<Output> callable = () -> insertOne(params,
                                                  connection);
      return IO.task(callable);
    };
  }

  private Output insertOne(final Params params,
                           final Connection connection) throws Exception {
    try (var ps = connection.prepareStatement(sql,
                                              Statement.RETURN_GENERATED_KEYS)
    ) {
      return JfrEventDecorator.decorateInsertOneStm(
                                                    () -> {
                                                      ps.setQueryTimeout((int) timeout.toSeconds());
                                                      int unused = setter.apply(params)
                                                                         .apply(ps);
                                                      assert unused > 0;
                                                      int numRowsAffected = ps.executeUpdate();
                                                      assert numRowsAffected >= 0;
                                                      try (ResultSet resultSet = ps.getGeneratedKeys()) {
                                                        if (resultSet.next()) {
                                                          return mapResult.apply(params)
                                                                          .apply(resultSet);
                                                        }
                                                        throw new ColumnNotGeneratedException(sql);
                                                      }

                                                    },
                                                    sql,
                                                    enableJFR,
                                                    label);
    }
  }
}
