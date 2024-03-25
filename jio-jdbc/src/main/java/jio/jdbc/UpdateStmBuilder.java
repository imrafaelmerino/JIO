package jio.jdbc;

import java.time.Duration;
import java.util.Objects;
import jio.Lambda;

/**
 * Builder class for creating update JDBC update statement, which must be an SQL Data Manipulation Language (DML)
 * statement, such as INSERT, UPDATE or DELETE; or an SQL statement that returns nothing, such as a DDL statement.
 *
 * <p>
 * This builder allows for the construction of {@code UpdateStm} instances to perform SQL update statements with
 * customizable parameters. It provides methods to configure various aspects of the update operation, such as SQL
 * statement, parameter setter, timeout, and Java Flight Recorder (JFR) event recording. The update operation is *
 * designed to be executed on virtual threads for improved concurrency and resource utilization.
 * </p>
 *
 * @param <Params> The type of input parameters for the update operation.
 */
public final class UpdateStmBuilder<Params> {

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
   * @param <Params>  The type of the parameters for the update operation.
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
   * Builds and returns a {@code Lambda} representing an update statement operation on a database. The lambda is
   * configured to bind parameters to its SQL, execute the update statement, and returns the affected rows as a result.
   * The JDBC connection is automatically obtained from the datasource and closed, which means that it cannot be used
   * for transactions where the connection can't be closed before committing or doing rollback.
   *
   * @param datasourceBuilder The {@code DatasourceBuilder} used to obtain the datasource and connections.
   * @return A {@code Lambda} representing the update statement. Note: The operations are performed by virtual threads.
   * @see UpdateStm#buildAutoClosable(DatasourceBuilder)
   */
  public Lambda<Params, Integer> buildAutoClosable(DatasourceBuilder datasourceBuilder) {
    return new UpdateStm<>(timeout,
                           sql,
                           setParams,
                           enableJFR,
                           label)
        .buildAutoClosable(datasourceBuilder);
  }

  /**
   * Builds and returns a closable update statement, allowing custom handling of the JDBC connection. This method is
   * appropriate for use during transactions, where the connection needs to be managed externally. The lambda is
   * configured to bind parameters to its SQL, execute the update statement, and return the affected rows as a result.
   *
   * @return A {@code ClosableStatement} representing the update statement. Note: The operations are performed by
   * virtual threads.
   * @see UpdateStm#buildClosable()
   */
  public ClosableStatement<Params, Integer> buildClosable() {
    return new UpdateStm<>(timeout,
                           sql,
                           setParams,
                           enableJFR,
                           label)
        .buildClosable();
  }

}
