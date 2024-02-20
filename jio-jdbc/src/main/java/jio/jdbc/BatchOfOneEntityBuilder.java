package jio.jdbc;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import jio.Lambda;

/**
 * Builder class for creating batch operations of the same SQL statement (either INSERT, UPDATE or DELETE) with multiple
 * parameters.
 *
 * @param <Params> The type of the input parameters for the batch operation.
 */
public final class BatchOfOneEntityBuilder<Params> {

  private final ParamsSetter<Params> setter;
  private final String sql;

  private boolean enableJFR = true;
  private final Duration timeout;
  private String label;

  private BatchOfOneEntityBuilder(ParamsSetter<Params> setter,
                                  String sql,
                                  Duration timeout) {
    this.setter = Objects.requireNonNull(setter);
    this.sql = Objects.requireNonNull(sql);
    this.timeout = Objects.requireNonNull(timeout);
  }

  private boolean continueOnError = false; // Indicates whether to continue inserting other batches if one fails.
  private int batchSize = 100; // The size of each batch.

  /**
   * Creates a new instance of BatchStmBuilder with the specified SQL statement and setter function.
   *
   * @param sql     The SQL statement for the batch operation.
   * @param setter  A function to set parameters on a {@link java.sql.PreparedStatement}.
   * @param timeout statement timeout
   * @param <I>     The type of input elements for the batch operation.
   * @return A new instance of BatchStmBuilder.
   */
  public static <I> BatchOfOneEntityBuilder<I> of(String sql,
                                                  ParamsSetter<I> setter,
                                                  Duration timeout) {
    return new BatchOfOneEntityBuilder<>(setter,
                                         sql,
                                         timeout);
  }

  /**
   * Specifies whether to continue inserting other batches if one fails. Defaults to false
   *
   * @param continueOnError If true, the batch operation continues with the next batch even if one fails.
   * @return This {@code BatchStmBuilder} instance for method chaining.
   */
  public BatchOfOneEntityBuilder<Params> continueOnError(boolean continueOnError) {
    this.continueOnError = continueOnError;
    return this;
  }

  /**
   * Sets a label for the Java Flight Recorder (JFR) event associated with this database query statement builder. The
   * label provides a descriptive identifier for the event and can be useful for tracking and analyzing events.
   *
   * @param label The label to be assigned to the JFR event.
   * @return This {@code QueryStmBuilder} instance with the specified event label.
   */
  public BatchOfOneEntityBuilder<Params> withEventLabel(String label) {
    this.label = Objects.requireNonNull(label);
    return this;
  }

  /**
   * Sets the size of each batch in the batch operation. Defaults to 100
   *
   * @param batchSize The size of each batch.
   * @return This BatchStmBuilder instance for method chaining.
   */
  public BatchOfOneEntityBuilder<Params> withBatchSize(int batchSize) {
    this.batchSize = batchSize;
    return this;
  }

  /**
   * Disables the recording of Java Flight Recorder (JFR) events for the JDBC query execution.
   *
   * @return This {@code QueryOneStmBuilder} instance with JFR event recording disabled.
   */
  public BatchOfOneEntityBuilder<Params> withoutRecordedEvents() {
    this.enableJFR = false;
    return this;
  }

  /**
   * Builds and returns a {@code Lambda} representing the JDBC batch operation configured with the specified settings.
   * The resulting lambda is suitable for automatic resource management (ARM) and is configured to execute the batch
   * operation, process the result, and close the associated JDBC resources. The operations are performed on virtual
   * threads for improved concurrency and resource utilization.
   *
   * @param datasourceBuilder The {@code DatasourceBuilder} used to obtain the datasource and connections.
   * @return A {@code Lambda} representing the JDBC batch operation with a duration, input, and output. Note: The
   *         operations are performed on virtual threads for improved concurrency and resource utilization.
   * @see BatchOfOneEntity#buildAutoClosable(DatasourceBuilder)
   */
  public Lambda<List<Params>, BatchResult> buildAutoClosable(DatasourceBuilder datasourceBuilder) {
    return new BatchOfOneEntity<>(timeout,
                                  setter,
                                  sql,
                                  continueOnError,
                                  batchSize,
                                  enableJFR,
                                  label).buildAutoClosable(datasourceBuilder);
  }

  /**
   * Builds and returns a {@code ClosableStatement} representing a JDBC batch operation on a database. This method is
   * appropriate for use during transactions, where the connection needs to be managed externally. The lambda is
   * configured to bind parameters to its SQL, execute the batch operation, and map the result. The operations are
   * performed on virtual threads for improved concurrency and resource utilization.
   *
   * @return A {@code ClosableStatement} representing the JDBC batch operation with a duration, input, and output. Note:
   *         The operations are performed on virtual threads for improved concurrency and resource utilization.
   * @see BatchOfOneEntity#buildClosable()
   */
  public ClosableStatement<List<Params>, BatchResult> buildClosable() {
    return new BatchOfOneEntity<>(timeout,
                                  setter,
                                  sql,
                                  continueOnError,
                                  batchSize,
                                  enableJFR,
                                  label).buildClosable();
  }
}
