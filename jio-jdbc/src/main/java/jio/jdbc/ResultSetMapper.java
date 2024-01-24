package jio.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A functional interface for mapping rows from a {@link java.sql.ResultSet} to objects of type {@code T}.
 *
 * @param <Output> The type of objects produced by the result set mapper.
 */
@FunctionalInterface
public interface ResultSetMapper<Output> {

  /**
   * Applies the mapping function to the given {@code ResultSet} to produce an object of type {@code T}.
   *
   * @param resultSet The result set to map to an object.
   * @return An object of type {@code T} resulting from the mapping.
   */
  Output apply(ResultSet resultSet) throws SQLException;
}
