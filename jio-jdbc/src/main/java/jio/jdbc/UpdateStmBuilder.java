package jio.jdbc;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Builder class for creating update operations in a JDBC context.
 *
 * @param <Params> The type of input parameters for the update operation.
 */
public final class UpdateStmBuilder<Params> implements Supplier<JdbcLambda<Params, Integer>> {

  private final Duration timeout;

  private final String sql;
  private final ParamsSetter<Params> setParams;
  private boolean enableJFR = true;
  private String label;

  private UpdateStmBuilder(Duration timeout,
                           String sql,
                           ParamsSetter<Params> setParams) {
    this.timeout = timeout;
    this.sql = Objects.requireNonNull(sql);
    this.setParams = Objects.requireNonNull(setParams);
  }

  /**
   * Creates a new instance of UpdateStmBuilder with the specified SQL statement, parameter setter, and result mapper.
   *
   * @param sql       The SQL statement for the update operation.
   * @param setParams A function to set parameters on a {@link java.sql.PreparedStatement}.
   * @param timeout   The time the driver will wait for a statement to execute
   * @param <Params>  The type of input elements for the update operation.
   * @return A new instance of UpdateStmBuilder.
   */
  public static <Params> UpdateStmBuilder<Params> of(String sql,
                                                     ParamsSetter<Params> setParams,
                                                     Duration timeout) {
    return new UpdateStmBuilder<>(timeout,
                                  sql,
                                  setParams);
  }

  /**
   * Disables recording of Java Flight Recorder (JFR) events for the update operation.
   *
   * @return This UpdateStmBuilder instance for method chaining.
   */
  public UpdateStmBuilder<Params> withoutRecordedEvents() {
    this.enableJFR = false;
    return this;
  }

  /**
   * Sets a label for the Java Flight Recorder (JFR) event associated with this database query statement builder. The
   * label provides a descriptive identifier for the event and can be useful for tracking and analyzing events.
   *
   * @param label The label to be assigned to the JFR event.
   * @return This {@code QueryStmBuilder} instance with the specified event label.
   */
  public UpdateStmBuilder<Params> withEventLabel(String label) {
    this.label = Objects.requireNonNull(label);
    return this;
  }

  /**
   * Builds a JdbcLambda representing the update operation based on the specified settings.
   *
   * @return A JdbcLambda instance for the update operation.
   */
  @Override
  public JdbcLambda<Params, Integer> get() {
    return new UpdateStm<>(timeout,
                           sql,
                           setParams,
                           enableJFR,
                           label);
  }
}
