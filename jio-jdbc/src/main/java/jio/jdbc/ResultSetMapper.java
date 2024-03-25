package jio.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A functional interface for mapping rows from a {@link java.sql.ResultSet} to objects of type {@code T}.
 *
 * @param <Entity>> The entity produced by the result set mapper.
 */
@FunctionalInterface
public interface ResultSetMapper<Entity> {

  /**
   * Mapper to handle a resul-set that contains at most one row. The `map` function doesn't have to call the `next`
   * method, just to collect the data from the column names.
   *
   * @param map      the map function (
   * @param <Entity> the type of the entity
   * @return the Entity
   */
  static <Entity> ResultSetMapper<Entity> ONE_ROW(ResultSetMapper<Entity> map) {
    return resultSet -> resultSet.next() ? map.apply(resultSet) : null;
  }

  /**
   * Creates a ResultSetMapper to handle a result set where each row represents an entity. The `map` function is called
   * for each row to collect entities into a list. The `next` method doesn't need to be called within the map function,
   * as it is automatically handled by this mapper.
   *
   * @param map      the map function to extract an entity from a result set row.
   * @param <Entity> the type of the entity.
   * @return a ResultSetMapper for handling one row per entity.
   */
  static <Entity> ResultSetMapper<List<Entity>> ONE_ROW_PER_ENTITY(ResultSetMapper<Entity> map) {
    return resultSet -> {
      List<Entity> entities = new ArrayList<>();
      while (resultSet.next()) {
        entities.add(map.apply(resultSet));
      }
      return entities;
    };
  }

  /**
   * Applies the mapping function to the given {@code ResultSet} to produce an object of type {@code T}.
   *
   * @param resultSet The result set to map to an object.
   * @return An entity resulting from the mapping.
   * @throws SQLException if a failure happens while reading the ResultSet
   */
  Entity apply(ResultSet resultSet) throws SQLException;
}
