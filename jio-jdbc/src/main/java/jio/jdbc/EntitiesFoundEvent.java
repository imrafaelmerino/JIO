package jio.jdbc;

import java.util.concurrent.atomic.AtomicLong;
import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;

/**
 * Event that is created and written to the Flight Recorder system when a request response is received or an exception
 * happens during the exchange
 */
@Label("JDBC Statement")
@Name("jio.jdbc.QueryStm")
@Category({"JIO", "DATABASE", "JDBC"})
@Description("JDBC query statements performed by jio-jdbc")
@StackTrace(value = false)

final class EntitiesFoundEvent extends Event {

  static final String QUERY_COUNTER_FIELD = "queryCounter";
  static final String ROWS_RETURNED_FIELD = "rowsReturned";
  static final String FETCH_SIZE_FIELD = "fetchSize";
  public int fetchSize;
  int rowsReturned;
  long queryCounter = EventCounter.COUNTER.incrementAndGet();
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
