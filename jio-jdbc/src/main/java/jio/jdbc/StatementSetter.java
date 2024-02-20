package jio.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A functional interface for setting parameters in a {@link java.sql.PreparedStatement}.
 * <p>
 * The {@code apply} method is responsible for setting parameters in a {@code PreparedStatement} starting from the
 * specified index. The functional nature allows for chaining multiple parameter setting operations using the
 * {@code then} method. The default method {@code apply(PreparedStatement ps)} is provided as a convenience, starting
 * parameter setting from index 1.
 */
@FunctionalInterface
public interface StatementSetter {

  /**
   * Sets parameters in a {@link java.sql.PreparedStatement}.
   *
   * @param paramPosition     The starting index for setting parameters.
   * @param preparedStatement The prepared statement to set parameters in.
   * @return The index indicating the next position for setting parameters.
   * @throws SQLException If a database access error occurs.
   */
  int apply(int paramPosition,
            PreparedStatement preparedStatement) throws SQLException;

  /**
   * Chains another {@code PrStmSetter} to this setter.
   *
   * @param stmSetter Another {@code PrStmSetter} to be applied after this setter.
   * @return A new {@code PrStmSetter} representing the combined operation.
   */
  default StatementSetter then(StatementSetter stmSetter) {
    return (int paramPosition,
            PreparedStatement ps) -> stmSetter.apply(this.apply(paramPosition,
                                                                ps),
                                                     ps);
  }

  /**
   * Sets parameters in a {@link java.sql.PreparedStatement} starting from index 1.
   *
   * @param ps The prepared statement to set parameters in.
   * @return The index indicating the next position for setting parameters.
   * @throws SQLException If a database access error occurs.
   */
  default int apply(PreparedStatement ps) throws SQLException {
    return this.apply(1,
                      ps);
  }
}