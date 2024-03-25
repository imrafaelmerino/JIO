package jio.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import jio.IO;
import jio.Lambda;

/**
 * Represents a JDBC batch operation for inserting or updating multiple records in a database.
 *
 * @param <Params> The type of input elements for the batch operation.
 */
class BatchOfOneEntity<Params> {

  final Duration timeout;
  final ParamsSetter<Params> setter;
  final String sql;
  final boolean continueOnError;
  final int batchSize;
  private final boolean enableJFR;
  private final String label;

  /**
   * Constructs a {@code BatchStm} instance with the specified settings.
   *
   * @param timeout         The maximum time in seconds that the batch operation should wait.
   * @param setter          A function to set parameters on a {@link PreparedStatement}.
   * @param sql             The SQL statement for the batch operation.
   * @param continueOnError If true, the batch operation continues with the next batch even if one fails.
   * @param batchSize       The size of each batch.
   * @param enableJFR       Flag indicating whether Java Flight Recorder (JFR) events should be enabled.
   * @param label           The label to identify the batch operation in Java Flight Recording.
   */
  BatchOfOneEntity(Duration timeout,
                   ParamsSetter<Params> setter,
                   String sql,
                   boolean continueOnError,
                   int batchSize,
                   boolean enableJFR,
                   String label) {
    this.timeout = timeout;
    this.setter = setter;
    this.sql = sql;
    this.continueOnError = continueOnError;
    this.batchSize = batchSize;
    this.enableJFR = enableJFR;
    this.label = label;
  }

  /**
   * Builds and returns a {@code Lambda} representing the JDBC batch operation configured with the specified settings.
   * The resulting lambda is suitable for automatic resource management (ARM) and is configured to execute the batch
   * operation, process the result, and close the associated JDBC resources. The operations are performed on virtual
   * threads for improved concurrency and resource utilization.
   *
   * @param builder The {@code DatasourceBuilder} used to obtain the datasource and connections.
   * @return A {@code Lambda} representing the JDBC batch operation with a duration, input, and output. Note: The
   * operations are performed on virtual threads for improved concurrency and resource utilization.
   * @see BatchOfOneEntity#buildAutoClosable(DatasourceBuilder)
   */
  public Lambda<List<Params>, BatchResult> buildAutoClosable(DatasourceBuilder builder) {
    return inputs -> {
      Callable<BatchResult> callable = () -> {
        try (var connection = builder.get()
                                     .getConnection()
        ) {
          return process(connection,
                         inputs);
        }
      };

      return IO.task(callable);
    };
  }

  /**
   * Builds and returns a {@code ClosableStatement} representing a JDBC batch operation on a database. This method is
   * appropriate for use during transactions, where the connection needs to be managed externally. The lambda is
   * configured to bind parameters to its SQL, execute the batch operation, and map the result. The operations are
   * performed on virtual threads for improved concurrency and resource utilization.
   *
   * @return A {@code ClosableStatement} representing the JDBC batch operation with a duration, input, and output. Note:
   * The operations are performed on virtual threads for improved concurrency and resource utilization.
   * @see BatchOfOneEntity#buildClosable()
   */
  public ClosableStatement<List<Params>, BatchResult> buildClosable() {
    return (params, connection) -> {
      Callable<BatchResult> callable = () -> process(connection,
                                                     params);
      return IO.task(callable);
    };
  }

  private BatchResult process(Connection connection,
                              List<Params> inputs) throws Exception {
    return JfrEventDecorator.decorateBatch(
        () -> {
          try (var ps = connection.prepareStatement(sql)) {
            ps.setQueryTimeout((int) timeout.toSeconds());
            List<SQLException> errors = new ArrayList<>();
            int executedBatches = 0, rowsAffected = 0, batchSizeCounter = 0;
            for (int i = 0; i < inputs.size(); i++) {
              try {
                setter.apply(inputs.get(i))
                      .apply(ps);
                ps.addBatch();
                batchSizeCounter++;
                if (batchSizeCounter == batchSize || i == inputs.size() - 1) {
                  executedBatches++;
                  int[] xs = ps.executeBatch();
                  for (int code : xs) {
                    if (code >= 0) {
                      rowsAffected += code;
                    }
                  }
                  ps.clearBatch();
                  batchSizeCounter = 0;  // Reset batchSizeCounter after each batch
                }
              } catch (SQLException e) {
                if (continueOnError) {
                  errors.add(e);
                  ps.clearBatch();
                  batchSizeCounter = 0;
                } else {
                  return new BatchFailure(inputs.size(),
                                          batchSize,
                                          executedBatches,
                                          rowsAffected,
                                          e);
                }
              }
            }
            if (errors.isEmpty()) {
              return new BatchSuccess(rowsAffected);
            } else {
              return new BatchPartialSuccess(inputs.size(),
                                             batchSize,
                                             executedBatches,
                                             rowsAffected,
                                             errors);
            }
          }

        },
        sql,
        enableJFR,
        label);

  }
}
