package jio.jdbc;

import java.sql.SQLException;

/**
 * Represents the result of a batch operation in a JDBC context.
 *
 * @param totalStms       The total number of statements processed across all batches.
 * @param batchSize       The size of each batch.
 * @param executedBatches The number of batches that were executed.
 * @param rowsAffected    The total number of rows affected in the database during the batch operation.
 * @param error           The failure that stopped the execution of the batch
 */
public record BatchFailure(int totalStms,
                           int batchSize,
                           int executedBatches,
                           int rowsAffected,
                           SQLException error) implements BatchResult {

}
