package jio.jdbc.exceptions;

import java.sql.SQLTransientConnectionException;
import java.util.function.Predicate;
import jio.Fun;

public final class HikariExceptions {

  private HikariExceptions() {
  }

  /**
   * Predicate to check if the given exception is thrown by Hikari when the connection timeout expires. This predicate
   * can be used to identify exceptions related to connection timeouts in a connection pool.
   *
   * <p>Hikari throws the exception {@link SQLTransientConnectionException} when the connection timeout expires.</p>
   *
   * @see SQLTransientConnectionException
   */
  public static final Predicate<Exception> IS_POOL_CONNECTION_TIMEOUT =
      e -> Fun.findUltimateCause(e) instanceof SQLTransientConnectionException;
}
