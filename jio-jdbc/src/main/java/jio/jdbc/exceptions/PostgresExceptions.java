package jio.jdbc.exceptions;

import java.net.ConnectException;
import jio.Fun;
import org.postgresql.util.PSQLException;

import java.util.function.Predicate;
import org.postgresql.util.PSQLState;

/**
 * Utility class for handling PostgresSQL-specific exceptions and defining predicates for common exception scenarios. It
 * provides predicates for identifying different types of exceptions related to PostgresSQL database operations.
 */
public final class PostgresExceptions {

  /**
   * Predicate to check if the given exception indicates a connection refused error. This predicate can be used
   * to filter or handle exceptions related to connection errors.
   *
   * <p>The predicate checks if the ultimate cause of the given exception is an instance of {@link ConnectException}
   * and if the message of the {@link ConnectException} is "Connection refused".</p>
   *
   * @see ConnectException
   * @see Fun#findUltimateCause(Throwable)
   */
  public static final Predicate<Exception> IS_CONNECTION_REFUSED =
      e -> Fun.findUltimateCause(e) instanceof ConnectException ce
          && "Connection refused".equals(ce.getMessage());

  private static final String QUERY_CANCELED_CODE = "57014";

  /**
   * Predicate for identifying exceptions related to statement timeout. Turns out the server cancels the query when the
   * query timeout expires
   */
  public static final Predicate<Exception> IS_QUERY_CANCELED =
      isPostgresExc(e -> QUERY_CANCELED_CODE.equals(e.getSQLState()));

  private PostgresExceptions() {
  }

  /**
   * Returns a predicate for identifying PostgreSQL exceptions based on the provided predicate for
   * {@link PSQLException}.
   *
   * <p>The predicate checks if the ultimate cause of the given exception is an instance of
   * {@link PSQLException} that satisfies the provided predicate.</p>
   *
   * @param predicate Predicate for {@link PSQLException}
   * @return A predicate for identifying PostgreSQL exceptions.
   * @see PSQLState
   * @see Fun#findUltimateCause(Throwable)
   */
  public static Predicate<Exception> isPostgresExc(Predicate<PSQLException> predicate) {
    return e -> Fun.findUltimateCause(e) instanceof PSQLException psql && predicate.test(psql);
  }

  /**
   * Predicate to check if the given exception is a connection error specific to PostgreSQL. This predicate can be used
   * to filter or handle exceptions related to database connections.
   *
   * <p>The connection error states checked by this predicate include:
   * {@link PSQLState#CONNECTION_UNABLE_TO_CONNECT}, {@link PSQLState#CONNECTION_DOES_NOT_EXIST},
   * {@link PSQLState#CONNECTION_REJECTED}, {@link PSQLState#CONNECTION_FAILURE}, and
   * {@link PSQLState#CONNECTION_FAILURE_DURING_TRANSACTION}.
   * </p>
   *
   * <p>Note: The predicate checks the ultimate cause of the exception using
   * {@link Fun#findUltimateCause(Throwable)}.</p>
   *
   * @see PSQLException
   * @see PSQLState#isConnectionError(String)
   * @see Fun#findUltimateCause(Throwable)
   */
  public final static Predicate<Exception> IS_CONNECTION_ERROR =
      isPostgresExc(e -> Fun.findUltimateCause(e) instanceof PSQLException psql
          && PSQLState.isConnectionError(psql.getSQLState()));


}
