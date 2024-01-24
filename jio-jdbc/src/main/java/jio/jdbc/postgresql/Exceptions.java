package jio.jdbc.postgresql;

import org.postgresql.util.PSQLException;

import java.sql.SQLTransientConnectionException;
import java.util.function.Predicate;

/**
 * Utility class for handling PostgreSQL-specific exceptions and defining predicates for common exception scenarios. It
 * provides predicates for identifying different types of exceptions related to PostgreSQL database operations.
 */
public final class Exceptions {

  /**
   * Predicate for identifying exceptions related to connection timeout.
   */
  public static final Predicate<Exception> CONNECTION_TIMEOUT =
      e -> e instanceof SQLTransientConnectionException;

  private static final String UNABLE_TO_ESTABLISH_SQL_CONNECTION = "08001";

  /**
   * Predicate for identifying exceptions related to the inability to establish a SQL connection.
   */
  public static final Predicate<Exception> UNABLE_TO_ESTABLISH_CONNECTION =
      isPSQLExc(e -> UNABLE_TO_ESTABLISH_SQL_CONNECTION.equals(e.getSQLState()));

  private static final String QUERY_CANCELED_CODE = "57014";

  /**
   * Predicate for identifying exceptions related to statement timeout.
   */
  public static final Predicate<Exception> STM_TIMEOUT =
      isPSQLExc(e -> QUERY_CANCELED_CODE.equals(e.getSQLState()));

  private Exceptions() {
  }

  /**
   * Returns a predicate for identifying PostgreSQL exceptions based on the provided predicate for
   * {@link PSQLException}.
   *
   * @param p Predicate for {@link PSQLException}
   * @return A predicate for identifying PostgreSQL exceptions.
   */
  public static Predicate<Exception> isPSQLExc(Predicate<PSQLException> p) {
    return e -> e instanceof PSQLException psql && p.test(psql);
  }
}
