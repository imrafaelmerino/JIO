
package jio.jdbc;

import jdk.jfr.*;

/**
 * Event that is created and written to the Flight Recorder system when a request response is received or an exception
 * happens during the exchange
 */
@Label("JDBC Statement")
@Name("jio.jdbc.UpdateStm")
@Category({"JIO", "DATABASE", "JDBC"})
@Description("JDBC update statements performed by jio-jdbc")
final class UpdateStmEvent extends StmEvent {

  static final String ROWS_AFFECTED_FIELD = "rowsAffected";
  int rowsAffected;
}
