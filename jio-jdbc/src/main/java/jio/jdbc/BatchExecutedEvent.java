package jio.jdbc;

import java.util.concurrent.atomic.AtomicLong;
import jdk.jfr.*;

/**
 * Event that is created and written to the Flight Recorder system when a request response is received or an exception
 * happens during the exchange
 */
@Label("JDBC Batch Operation")
@Name("jio.jdbc.BatchStm")
@Category({"JIO", "DATABASE", "JDBC"})
@Description("Duration, result, batch size, rows updated and other info related to batch operations performed by jio-jdbc")
@StackTrace(value = false)
final class BatchExecutedEvent extends Event {

  private static final AtomicLong counter = new AtomicLong(0);

  static final String BATCH_SIZE_FIELD = "batchSize";
  static final String ROWS_AFFECTED_FIELD = "rowsAffected";
  static final String STM_SIZE_FIELD = "totalStms";
  static final String EXECUTED_BATCHES_FIELD = "executedBatches";

  static final String BATCH_COUNTER_FIELD = "batchCounter";

  long batchCounter = counter.incrementAndGet();

  int batchSize;
  int totalStms;
  int rowsAffected;
  int executedBatches;

  enum RESULT {
    SUCCESS, FAILURE, PARTIAL_SUCCESS
  }

  static final String RESULT_FIELD = "result";
  static final String SQL_FIELD = "sql";
  static final String EXCEPTION_FIELD = "exception";
  static final String LABEL_FIELD = "label";

  /**
   * the method of the request
   */
  String sql;

  /**
   * the result of the exchange: a success if a response is received or an exception
   */
  String result;
  /**
   * the exception in case of one happens during the exchange
   */
  String exception;

  /**
   * Short label to identify the statement
   */
  String label;


}
