package jio.jdbc;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import jio.Lambda;

/**
 * Builder class for creating batch operations on a JDBC database using a lambda-based approach.
 *
 * @param <Params> The type of the input parameters for the batch operation.
 */
public final class BatchStmBuilder<Params> {

  private final ParamsSetter<Params> setter;
  private final String sql;

  private boolean enableJFR = true;
  private final Duration timeout;
  private String label;

  private BatchStmBuilder(ParamsSetter<Params> setter,
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
  public static <I> BatchStmBuilder<I> of(String sql,
                                          ParamsSetter<I> setter,
                                          Duration timeout) {
    return new BatchStmBuilder<>(setter,
                                 sql,
                                 timeout);
  }

  /**
   * Specifies whether to continue inserting other batches if one fails.
   *
   * @param continueOnError If true, the batch operation continues with the next batch even if one fails.
   * @return This BatchStmBuilder instance for method chaining.
   */
  public BatchStmBuilder<Params> continueOnError(boolean continueOnError) {
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
  public BatchStmBuilder<Params> withEventLabel(String label) {
    this.label = Objects.requireNonNull(label);
    return this;
  }

  /**
   * Sets the size of each batch in the batch operation.
   *
   * @param batchSize The size of each batch.
   * @return This BatchStmBuilder instance for method chaining.
   */
  public BatchStmBuilder<Params> withBatchSize(int batchSize) {
    this.batchSize = batchSize;
    return this;
  }


  /**
   * Disables the recording of Java Flight Recorder (JFR) events for the JDBC query execution.
   *
   * @return This {@code QueryOneStmBuilder} instance with JFR event recording disabled.
   */
  public BatchStmBuilder<Params> withoutRecordedEvents() {
    this.enableJFR = false;
    return this;
  }

  /**
   * Builds a JdbcLambda representing the batch operation based on the specified settings.
   *
   * @return A JdbcLambda instance for the batch operation.
   */
  public Lambda<List<Params>, BatchResult> buildAutoClosable(DatasourceBuilder datasourceBuilder) {
    return new BatchStm<>(timeout,
                          setter,
                          sql,
                          continueOnError,
                          batchSize,
                          enableJFR,
                          label).buildAutoClosable(datasourceBuilder);
  }

  public ClosableStatement<List<Params>, BatchResult> buildClosable() {
    return new BatchStm<>(timeout,
                          setter,
                          sql,
                          continueOnError,
                          batchSize,
                          enableJFR,
                          label).buildClosable();
  }
}
