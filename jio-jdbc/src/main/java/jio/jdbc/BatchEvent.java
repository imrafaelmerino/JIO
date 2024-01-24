package jio.jdbc;

import jdk.jfr.*;

/**
 * Event that is created and written to the Flight Recorder system when a request response is received or an exception
 * happens during the exchange
 */
@Label("JDBC Batch Operation")
@Name("jio.jdbc.BatchStm")
@Category({"JIO", "DATABASE", "JDBC"})
@Description("Duration, result, batch size, rows updated and other info related to batch operations performed by jio-jdbc")
final class BatchEvent extends StmEvent {

  static final String BATCH_SIZE_FIELD = "batchSize";
  static final String ROWS_AFFECTED_FIELD = "rowsAffected";
  static final String STM_SIZE_FIELD = "totalStms";
  static final String EXECUTED_BATCHES_FIELD = "executedBatches";

  int batchSize;
  int totalStms;
  int rowsAffected;
  int executedBatches;

}
