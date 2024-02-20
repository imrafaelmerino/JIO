package jio.jdbc.exceptions;

import java.sql.SQLException;
import java.sql.SQLTransientException;
import java.util.Optional;
import java.util.function.Function;
import jio.ExceptionFun;

/**
 * A utility class providing functions related to JDBC exceptions.
 */
public final class JdbcFun {

  private JdbcFun() {
  }

  /**
   * Function that finds the cause in the exception chain that is an instance of {@link SQLException}.
   *
   * <p>
   * This function is useful for extracting the root {@link SQLException} from a nested exception chain.
   * </p>
   *
   * @see SQLException
   */
  public static final Function<Throwable, Optional<SQLException>> findSqlExcRecursively = e -> ExceptionFun.findCauseRecursively(exc -> exc instanceof SQLException)
                                                                                                           .apply(e)
                                                                                                           .map(exc -> ((SQLException) exc));

  /**
   * Function that finds the cause in the exception chain that is an instance of {@link SQLTransientException}.
   *
   * <p>
   * This function is specifically designed for transient SQL exceptions, which might indicate temporary issues such as
   * network problems. Transient exceptions are often considered for retrying the operation.
   * </p>
   *
   * @see SQLTransientException
   */
  public static final Function<Throwable, Optional<SQLTransientException>> findSqlTransientExcRecursively = e -> ExceptionFun.findCauseRecursively(exc -> exc instanceof SQLTransientException)
                                                                                                                             .apply(e)
                                                                                                                             .map(exc -> ((SQLTransientException) exc));

}
