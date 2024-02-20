package jio.jdbc;

/**
 * A record representing the successful outcome of a JDBC batch operation. It includes the number of rows affected by
 * the batch operation.
 *
 * @param rowsAffected The number of rows affected by the batch operation.
 */
public record BatchSuccess(int rowsAffected) implements BatchResult {

}
