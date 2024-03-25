package jio.jdbc;

import java.sql.Connection;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import jio.IO;
import jio.Lambda;

/**
 * A class representing a generic update statement in a relational database using JDBC. The class is designed to execute
 * an SQL update statement, bind parameters to the SQL and map the result. The operation, by default, creates a Java
 * Flight Recorder (JFR) event.
 *
 * @param <Params> The type of the input object for setting parameters in the SQL.
 * @see InsertOneEntity for using insert operationg that insert at most one row into the database and may generate some
 * keys like ids or timestamps that can be returned
 */
final class UpdateStm<Params> {

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
   * Flag indicating whether Java Flight Recorder (JFR) events should be enabled.
   */
  private final boolean enableJFR;

  /**
   * The label to identify the update statement in Java Flight Recording. It is used as a field in the JFR event for
   * distinguishing operations from each other.
   */
  private final String label;

  /**
   * Constructs an {@code UpdateStm} with the specified SQL statement, parameter setter, result mapper, and the option
   * to enable or disable JFR events.
   *
   * @param sql       The SQL update statement.
   * @param setter    The parameter setter for setting parameters in the update statement.
   * @param enableJFR Flag indicating whether to enable JFR events.
   * @param label     The label to identify the update statement in Java Flight Recording
   */
  UpdateStm(Duration timeout,
            String sql,
            ParamsSetter<Params> setter,
            boolean enableJFR,
            String label) {
    this.timeout = timeout;
    this.sql = Objects.requireNonNull(sql);
    this.setter = Objects.requireNonNull(setter);
    this.enableJFR = enableJFR;
    this.label = label;
  }

  /**
   * Creates a {@code Lambda} representing an update statement operation on a database. The lambda is configured to bind
   * parameters to its sql, execute the update statement, and returns the affected rows as a result. The JDBC connection
   * is automatically obtained from the datasource and closed, which means that con not be used for transactions where
   * the connection can't be closed before committing o doing rollback.
   *
   * @param datasourceBuilder The {@code DatasourceBuilder} used to obtain the datasource and connections.
   * @return A {@code Lambda} representing update statement. Note: The operations are performed by virtual threads.
   * @see #buildClosable() for using update statements during transactions
   */

  Lambda<Params, Integer> buildAutoClosable(DatasourceBuilder datasourceBuilder) {
    return params -> {
      Callable<Integer> callable = () -> {
        try (var connection = datasourceBuilder.get()
                                               .getConnection()
        ) {
          return updateStm(params,
                           connection);
        }
      };
      return IO.task(callable);
    };
  }

  private int updateStm(final Params params,
                        final Connection connection) throws Exception {
    try (var statement = connection.prepareStatement(sql)
    ) {
      return JfrEventDecorator.decorateUpdateStm(
          () -> {

            statement.setQueryTimeout((int) timeout.toSeconds());
            int unused = setter.apply(params)
                               .apply(statement);
            assert unused > 0;
            return statement.executeUpdate();

          },
          sql,
          enableJFR,
          label);
    }
  }

  /**
   * Builds a closable update statement, allowing custom handling of the JDBC connection. This method is appropriate for
   * use during transactions, where the connection needs to be managed externally. The lambda is configured to bind
   * parameters to its SQL, execute the update statement, and return the affected rows as a result.
   *
   * @return A {@code ClosableStatement} representing the update statement. Note: The operations are performed by
   * virtual threads.
   */
  ClosableStatement<Params, Integer> buildClosable() {
    return (params,
            connection) -> {
      Callable<Integer> callable = () -> updateStm(params,
                                                   connection);

      return IO.task(callable);
    };
  }
}
