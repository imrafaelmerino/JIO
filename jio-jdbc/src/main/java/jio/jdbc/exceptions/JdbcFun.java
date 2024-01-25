package jio.jdbc.exceptions;

import java.sql.SQLException;
import java.sql.SQLTransientException;
import java.util.Optional;
import java.util.function.Function;
import jio.ExceptionFun;

public final class JdbcFun {

  private JdbcFun() {
  }

  /**
   * Function that finds the cause in the exception chain that is an instance of {@link SQLException}.
   *
   * @see SQLException
   */
  public static final Function<Throwable, Optional<SQLException>> findSqlExcRecursively =
      e -> ExceptionFun.findCauseRecursively(exc -> exc instanceof SQLException)
                       .apply(e)
                       .map(exc -> ((SQLException) exc));

  /**
   * Function that finds the cause in the exception chain that is an instance of {@link SQLTransientException}.
   *
   * @see SQLTransientException
   */
  public static final Function<Throwable, Optional<SQLTransientException>> findSqlTransientExcRecursively =
      e -> ExceptionFun.findCauseRecursively(exc -> exc instanceof SQLTransientException)
                       .apply(e)
                       .map(exc -> ((SQLTransientException) exc));

}
