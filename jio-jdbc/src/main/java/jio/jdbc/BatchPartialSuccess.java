package jio.jdbc;

import java.sql.SQLException;
import java.util.List;

/**
 * Represents the result of a batch operation in a JDBC context.
 *
 * @param totalStms       The total number of statements processed across all batches.
 * @param batchSize       The size of each batch.
 * @param executedBatches The number of batches that were executed.
 * @param rowsAffected    The total number of rows affected in the database during the batch operation.
 * @param errors          A list of SQLException instances representing errors that occurred during the execution.
 */
public record BatchPartialSuccess(int totalStms,
                                  int batchSize,
                                  int executedBatches,
                                  int rowsAffected,
                                  List<SQLException> errors) implements BatchResult {

}
