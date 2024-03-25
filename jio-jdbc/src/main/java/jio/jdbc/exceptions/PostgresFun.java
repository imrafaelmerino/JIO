package jio.jdbc.exceptions;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import jio.ExceptionFun;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

/**
 * The {@code PostgresExceptions} class serves as a utility for handling PostgreSQL-specific exceptions and defining
 * predicates for common exception scenarios. It provides functions and predicates to identify and extract information
 * from exceptions related to PostgresSQL database operations.
 *
 * <p>The class includes the following methods and predicates:</p>
 *
 * <ul>
 * <li>{@link #findPSQLExceptionRecursively}: A function to find instances of {@link PSQLException} in the exception
 * chain.</li>
 * <li>{@link #findPSQLExceptionRecursively(Predicate)}: A predicate to check if the given exception is a
 * {@link PSQLException}
 * with a specified SQL state.</li>
 * <li>{@link #HAS_QUERY_CANCELED}: A predicate to identify exceptions related to statement timeout, where the server
 * cancels
 * the query when the query timeout expires.</li>
 * <li>{@link #HAS_CONNECTION_ERROR}: A predicate to check if the given exception is a connection error specific to
 * PostgreSQL,
 * including states such as {@link PSQLState#CONNECTION_UNABLE_TO_CONNECT}, {@link PSQLState#CONNECTION_DOES_NOT_EXIST},
 * {@link PSQLState#CONNECTION_REJECTED}, {@link PSQLState#CONNECTION_FAILURE}, and
 * {@link PSQLState#CONNECTION_FAILURE_DURING_TRANSACTION}.</li>
 * </ul>
 *
 * <p>The class is final and cannot be instantiated, as it only provides static utility methods.</p>
 *
 * <p>Additionally, it includes constants for specific PostgreSQL error codes, such as {@code QUERY_CANCELED_CODE}.</p>
 *
 * @see PSQLException
 * @see PSQLState
 * @see ExceptionFun
 * @see ExceptionFun#findUltimateCause(Throwable)
 * @see PSQLState#isConnectionError(String)
 */
public final class PostgresFun {

  /**
   * Function to find the cause in the exception chain that is an instance of {@link PSQLException}.
   *
   * @see PSQLException
   */
  public static final Function<Throwable, Optional<PSQLException>> findPSQLExceptionRecursively = e -> ExceptionFun.findCauseRecursively(exc -> exc instanceof PSQLException)
                                                                                                                   .apply(e)
                                                                                                                   .map(it -> ((PSQLException) it));

  /**
   * Predicate to check if the given exception is a {@link PSQLException} with a specified SQL state.
   *
   * @param sqlStatePredicate The predicate to test the SQL state of the {@link PSQLException}.
   * @return A predicate that checks if the SQL state of the {@link PSQLException} satisfies the provided predicate.
   * @see PSQLException
   * @see PSQLState
   */
  public static Predicate<Throwable> findPSQLExceptionRecursively(Predicate<String> sqlStatePredicate) {
    return e -> findPSQLExceptionRecursively.apply(e)
                                            .map(exc -> sqlStatePredicate.test(exc.getSQLState()))
                                            .orElse(false);
  }

  private static final String QUERY_CANCELED_CODE = "57014";

  /**
   * Predicate for identifying exceptions related to statement timeout. Turns out the server cancels the query when the
   * query timeout expires.
   *
   * @see PSQLException
   */
  public static final Predicate<Throwable> HAS_QUERY_CANCELED = findPSQLExceptionRecursively(QUERY_CANCELED_CODE::equals);

  private PostgresFun() {
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
   * {@link ExceptionFun#findUltimateCause(Throwable)}.</p>
   *
   * @see PSQLException
   * @see PSQLState#isConnectionError(String)
   * @see ExceptionFun#findUltimateCause(Throwable)
   */
  public final static Predicate<Throwable> HAS_CONNECTION_ERROR = findPSQLExceptionRecursively(PSQLState::isConnectionError);

}
