package jio.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A functional interface for setting parameters in a {@link java.sql.PreparedStatement}.
 * <p>
 * The {@code apply} method is responsible for setting parameters in a {@code PreparedStatement} starting
 * from the specified index. The functional nature allows for chaining multiple parameter setting operations
 * using the {@code then} method. The default method {@code apply(PreparedStatement ps)} is provided as a
 * convenience, starting parameter setting from index 1.
 */
@FunctionalInterface
public interface PrStmSetter {

    /**
     * Sets parameters in a {@link java.sql.PreparedStatement}.
     *
     * @param n                    The starting index for setting parameters.
     * @param preparedStatement   The prepared statement to set parameters in.
     * @return The index indicating the next position for setting parameters.
     * @throws SQLException If a database access error occurs.
     */
    int apply(int n, PreparedStatement preparedStatement) throws SQLException;

    /**
     * Chains another {@code PrStmSetter} to this setter.
     *
     * @param p Another {@code PrStmSetter} to be applied after this setter.
     * @return A new {@code PrStmSetter} representing the combined operation.
     */
    default PrStmSetter then(PrStmSetter p) {
        return (int n, PreparedStatement ps) -> p.apply(this.apply(n, ps), ps);
    }

    /**
     * Sets parameters in a {@link java.sql.PreparedStatement} starting from index 1.
     *
     * @param ps The prepared statement to set parameters in.
     * @return The index indicating the next position for setting parameters.
     * @throws SQLException If a database access error occurs.
     */
    default int apply(PreparedStatement ps) throws SQLException {
        return this.apply(1, ps);
    }
}