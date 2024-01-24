package jio.jdbc;

import jdk.jfr.*;

/**
 * Event that is created and written to the Flight Recorder system when a request response is received or an exception
 * happens during the exchange
 */
@Label("JDBC Statement")
@Name("jio.jdbc.QueryStm")
@Category({"JIO", "DATABASE", "JDBC"})
@Description("JDBC query statements performed by jio-jdbc")
final class QueryStmEvent extends StmEvent {

  static final String ROWS_RETURNED_FIELD = "rowsReturned";
  static final String FETCH_SIZE_FIELD = "fetchSize";
  public int fetchSize;
  int rowsReturned;

}
