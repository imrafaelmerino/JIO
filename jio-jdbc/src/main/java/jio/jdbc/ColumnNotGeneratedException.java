package jio.jdbc;

import java.sql.SQLException;

/**
 * Exception thrown to indicate that no auto-generated key was produced by the executed SQL statement. This exception is
 * typically used when expecting auto-generated keys from an INSERT operation, but none are available.
 */
@SuppressWarnings("serial")
public final class ColumnNotGeneratedException extends SQLException {

  /**
   * Constructs a new {@code ColumnNotGeneratedException} with a detail message indicating the SQL statement for which
   * no generated key was produced.
   *
   * @param sql The SQL statement that did not produce any generated key.
   */
  ColumnNotGeneratedException(String sql) {
    super("No generated key by the sql `%s`".formatted(sql));
  }
}
