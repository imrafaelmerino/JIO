package jio.jdbc;

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;

@Label("JDBC Transaction")
@Name("jio.jdbc.Tx")
@Category({"JIO", "DATABASE", "JDBC"})
@Description("JDBC transaction performed by jio-jdbc")
@StackTrace(value = false)
final class TxExecutedEvent extends Event {


  static final String RESULT_FIELD = "result";
  static final String SAVEPOINT_FIELD = "savePoint";
  static final String EXCEPTION_FIELD = "exception";
  static final String LABEL_FIELD = "label";
  static final String TX_COUNTER_FIELD = "txCounter";

  /**
   * the result of the exchange: a success if a response is received or an exception
   */
  String result;
  /**
   * the exception in case of one happens during the exchange
   */
  String exception;

  /**
   * Short label to identify the transaction
   */
  String label;

  String savePoint;

  long txCounter = EventCounter.COUNTER.incrementAndGet();

  enum RESULT {
    SUCCESS, FAILURE, PARTIAL_SUCCESS
  }

}
