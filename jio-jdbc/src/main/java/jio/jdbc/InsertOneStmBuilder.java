package jio.jdbc;

import java.time.Duration;
import java.util.Objects;
import java.util.function.BiFunction;
import jio.Lambda;

/**
 * Builder class for creating update and generate key operations in a JDBC context.
 *
 * @param <Params> The type of input parameters for the insert operation.
 * @param <Output> The type of the output result from the insert operation.
 */
public final class InsertOneStmBuilder<Params, Output> {

  private final String sql;

  private final Duration timeout;

  private final ParamsSetter<Params> setParams;
  private final BiFunction<Params, Integer, ResultSetMapper<Output>> mapResult;
  private boolean enableJFR = true;
  private String label;

  private InsertOneStmBuilder(String sql,
                              Duration timeout,
                              ParamsSetter<Params> setParams,
                              BiFunction<Params, Integer, ResultSetMapper<Output>> mapResult) {
    this.sql = Objects.requireNonNull(sql);
    this.timeout = timeout;
    this.setParams = Objects.requireNonNull(setParams);
    this.mapResult = Objects.requireNonNull(mapResult);
  }

  /**
   * Creates a new instance of UpdateGenStmBuilder with the specified SQL statement, parameter setter, and result
   * mapper.
   *
   * @param sql       The SQL statement for the update operation.
   * @param setParams A function to set parameters on a {@link java.sql.PreparedStatement}.
   * @param mapResult A function to map the result set to the desired output type. Takes the input params and the number
   *                  of rows affected by the insert op(either one or zero)
   * @param timeout   The time the driver will wait for a statement to execute
   * @param <Params>  The type of input elements for the update operation.
   * @param <Output>  The type of the output result from the update operation.
   * @return A new instance of UpdateGenStmBuilder.
   */
  public static <Params, Output> InsertOneStmBuilder<Params, Output> of(String sql,
                                                                        ParamsSetter<Params> setParams,
                                                                        BiFunction<Params, Integer, ResultSetMapper<Output>> mapResult,
                                                                        Duration timeout) {
    return new InsertOneStmBuilder<>(sql,
                                     timeout,
                                     setParams,
                                     mapResult);
  }

  /**
   * Sets a label for the Java Flight Recorder (JFR) event associated with this database query statement builder. The
   * label provides a descriptive identifier for the event and can be useful for tracking and analyzing events.
   *
   * @param label The label to be assigned to the JFR event.
   * @return This {@code QueryStmBuilder} instance with the specified event label.
   */
  public InsertOneStmBuilder<Params, Output> withEventLabel(String label) {
    this.label = Objects.requireNonNull(label);
    return this;
  }

  /**
   * Disables recording of Java Flight Recorder (JFR) events for the update operation.
   *
   * @return This UpdateGenStmBuilder instance for method chaining.
   */
  public InsertOneStmBuilder<Params, Output> withoutRecordedEvents() {
    this.enableJFR = false;
    return this;
  }

  /**
   * Builds a JdbcLambda representing the update and generate key operation based on the specified settings.
   *
   * @return A JdbcLambda instance for the update and generate key operation.
   */
  public Lambda<Params, Output> buildAutoClosable(DatasourceBuilder datasourceBuilder) {
    return new InsertOneStm<>(timeout,
                              sql,
                              setParams,
                              mapResult,
                              enableJFR,
                              label)
        .buildAutoClosableStm(datasourceBuilder);
  }

  public ClosableStatement<Params, Output> buildClosable() {
    return new InsertOneStm<>(timeout,
                              sql,
                              setParams,
                              mapResult,
                              enableJFR,
                              label)
        .buildClosableStm();
  }
}
