
package jio.jdbc;

import java.util.concurrent.atomic.AtomicLong;
import jdk.jfr.*;

/**
 * Event that is created and written to the Flight Recorder system when a request response is received or an exception
 * happens during the exchange
 */
@Label("JDBC Statement")
@Name("jio.jdbc.UpdateStm")
@Category({"JIO", "DATABASE", "JDBC"})
@Description("JDBC update statements performed by jio-jdbc")
@StackTrace(value = false)
final class UpdateStmExecutedEvent extends Event {

  static final String UPDATE_COUNTER_FIELD = "updateCounter";
  static final String ROWS_AFFECTED_FIELD = "rowsAffected";
  int rowsAffected;

  long updateCounter = EventCounter.COUNTER.incrementAndGet();

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

  enum RESULT {
    SUCCESS, FAILURE
  }
}
