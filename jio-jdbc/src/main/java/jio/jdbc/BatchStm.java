package jio.jdbc;

import jio.IO;
import jio.Lambda;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Represents a JDBC batch operation for inserting or updating multiple records in a database.
 *
 * @param <Params> The type of input elements for the batch operation.
 */
class BatchStm<Params> {

  final Duration timeout;
  final ParamsSetter<Params> setter;
  final String sql;
  final boolean continueOnError;
  final int batchSize;
  private final boolean enableJFR;
  private final String label;


  /**
   * Constructs a BatchStm instance.
   *
   * @param setter          A function to set parameters on a {@link java.sql.PreparedStatement}.
   * @param sql             The SQL statement for the batch operation.
   * @param continueOnError If true, the batch operation continues with the next batch even if one fails.
   * @param label           The label to identify the batch
   * @param batchSize       The size of each batch.
   */
  BatchStm(Duration timeout,
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
   * Applies this function to the given DatasourceBuilder and returns a new function that represents the batch
   * operation.
   *
   * @param builder The DatasourceBuilder for obtaining a connection to the database.
   * @return A BiLambda representing the batch operation.
   */
  public Lambda<List<Params>, BatchResult> buildAutoClosable(DatasourceBuilder builder) {
    return inputs -> IO.task(
        () -> JfrEventDecorator.decorateBatch(
            () -> {
              try (var connection = builder.get()
                                           .getConnection()
              ) {
                connection.setAutoCommit(false);
                try (var ps = connection.prepareStatement(sql)) {
                  ps.setQueryTimeout((int) timeout.toSeconds());
                  return process(inputs,
                                 ps,
                                 connection);
                }
              }
            },
            sql,
            enableJFR,
            label),
        Executors.newVirtualThreadPerTaskExecutor()
                            );
  }

  public ClosableStatement<List<Params>, BatchResult> buildClosable() {
    return inputs -> connection ->
        IO.task(
            () -> JfrEventDecorator.decorateBatch(
                () -> {
                  connection.setAutoCommit(false);
                  try (var ps = connection.prepareStatement(sql)) {
                    ps.setQueryTimeout((int) timeout.toSeconds());
                    return process(inputs,
                                   ps,
                                   connection);
                  }
                },
                sql,
                enableJFR,
                label),
            Executors.newVirtualThreadPerTaskExecutor()
               );
  }

  private BatchResult process(List<Params> params,
                              PreparedStatement ps,
                              Connection connection) throws SQLException {
    List<SQLException> errors = new ArrayList<>();
    int executedBatches = 0, rowsAffected = 0, batchSizeCounter = 0;
    for (int i = 0; i < params.size(); i++) {
      try {
        setter.apply(params.get(i))
              .apply(ps);
        ps.addBatch();
        batchSizeCounter++;
        if (batchSizeCounter == batchSize || i == params.size() - 1) {
          executedBatches++;
          int[] xs = ps.executeBatch();
          for (int code : xs) {
            if (code >= 0) {
              rowsAffected += code;
            }
          }
          connection.commit();
          ps.clearBatch();
          batchSizeCounter = 0;  // Reset batchSizeCounter after each batch
        }
      } catch (SQLException e) {
        errors.add(e);
        if (continueOnError) {
          ps.clearBatch();
          batchSizeCounter = 0;
        } else {
          return new BatchResult(params.size(),
                                 batchSize,
                                 executedBatches,
                                 rowsAffected,
                                 errors);
        }
      }
    }
    return new BatchResult(params.size(),
                           batchSize,
                           executedBatches,
                           rowsAffected,
                           errors);
  }
}
