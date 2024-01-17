package jio.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A functional interface for mapping rows from a {@link java.sql.ResultSet} to objects of type {@code T}.
 *
 * @param <T> The type of objects produced by the result set mapper.
 */
@FunctionalInterface
public interface ResultSetMapper<T> {

    /**
     * Applies the mapping function to the given {@code ResultSet} to produce an object of type {@code T}.
     *
     * @param rs The result set to map to an object.
     * @return An object of type {@code T} resulting from the mapping.
     * @throws SQLException If a database access error occurs or this method is called on a closed result set.
     */
    T apply(ResultSet rs) throws SQLException;
}
