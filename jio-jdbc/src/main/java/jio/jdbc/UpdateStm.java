package jio.jdbc;

import jio.IO;
import jio.Lambda;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executors;

/**
 * A class representing a generic update operation with a generated key in a relational database using JDBC. The class
 * is designed to execute an SQL update statement, set parameters, and retrieve the generated key. The operation is
 * wrapped with Java Flight Recorder (JFR) events.
 *
 * @param <Params> The type of the input object for setting parameters in the update statement.
 */
final class UpdateStm<Params> {

  final Duration timeout;

  /**
   * The SQL update statement.
   */
  final String sql;

  /**
   * The parameter setter for setting parameters in the update statement.
   */
  final ParamsSetter<Params> setParams;


  /**
   * Flag indicating whether Java Flight Recorder (JFR) events should be enabled.
   */
  private final boolean enableJFR;
  private final String label;


  /**
   * Constructs an {@code UpdateGenStm} with the specified SQL statement, parameter setter, result mapper, and the
   * option to enable or disable JFR events.
   *
   * @param sql       The SQL update statement.
   * @param setParams The parameter setter for setting parameters in the update statement.
   * @param enableJFR Flag indicating whether to enable JFR events.
   * @param label     The label to identify the update statement in Java Flight Recording
   */
  UpdateStm(Duration timeout,
            String sql,
            ParamsSetter<Params> setParams,
            boolean enableJFR,
            String label) {
    this.timeout = timeout;
    this.sql = Objects.requireNonNull(sql);
    this.setParams = Objects.requireNonNull(setParams);
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
  public Lambda<Params, Integer> buildAutoClosableStm(DatasourceBuilder dsb) {
    return params ->
        IO.task(() -> JfrEventDecorator.decorateUpdateStm(
                    () -> {
                      try (var connection = dsb.get()
                                               .getConnection()
                      ) {
                        try (var ps = connection.prepareStatement(sql)
                        ) {
                          ps.setQueryTimeout((int) timeout.toSeconds());
                          int unused = setParams.apply(params)
                                                .apply(ps);
                          assert unused > 0;
                          return ps.executeUpdate();
                        }
                      }
                    },
                    sql,
                    enableJFR,
                    label),
                Executors.newVirtualThreadPerTaskExecutor());
  }

  public ClosableStatement<Params, Integer> buildClosableStm() {
    return params -> connection ->
        IO.task(() -> JfrEventDecorator.decorateUpdateStm(
                    () -> {
                      try (var ps = connection.prepareStatement(sql)
                      ) {
                        ps.setQueryTimeout((int) timeout.toSeconds());
                        int unused = setParams.apply(params)
                                              .apply(ps);
                        assert unused > 0;
                        return ps.executeUpdate();
                      }

                    },
                    sql,
                    enableJFR,
                    label),
                Executors.newVirtualThreadPerTaskExecutor());
  }
}
