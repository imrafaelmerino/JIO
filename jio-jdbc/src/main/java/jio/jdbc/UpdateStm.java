package jio.jdbc;

import jio.BiLambda;
import jio.IO;

import java.sql.Statement;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;

/**
 * A class representing a generic update operation with a generated key in a relational database using JDBC. The class
 * is designed to execute an SQL update statement, set parameters, and retrieve the generated key. The operation is
 * wrapped with Java Flight Recorder (JFR) events.
 *
 * @param <I> The type of the input object for setting parameters in the update statement.
 * @param <O> The type of the output object generated from the ResultSet.
 */
final class UpdateStm<I, O> implements JdbcLambda<I, O> {

    /**
     * The SQL update statement.
     */
    final String sql;

    /**
     * The parameter setter for setting parameters in the update statement.
     */
    final ParamsSetter<I> setParams;

    /**
     * The function to map the ResultSet to the output object.
     */
    final BiFunction<I, Integer, O> mapResult;

    /**
     * Flag indicating whether Java Flight Recorder (JFR) events should be enabled.
     */
    private final boolean enableJFR;


    /**
     * Constructs an {@code UpdateGenStm} with the specified SQL statement, parameter setter, result mapper, and the
     * option to enable or disable JFR events.
     *
     * @param sql       The SQL update statement.
     * @param setParams The parameter setter for setting parameters in the update statement.
     * @param mapResult The function to map the ResultSet to the output object.
     * @param enableJFR Flag indicating whether to enable JFR events.
     */
    UpdateStm(String sql, ParamsSetter<I> setParams, BiFunction<I, Integer, O> mapResult, boolean enableJFR) {
        this.sql = Objects.requireNonNull(sql);
        this.setParams = Objects.requireNonNull(setParams);
        this.mapResult = mapResult;
        this.enableJFR = enableJFR;
    }

    /**
     * Applies the update operation to a datasource, setting parameters, executing the update statement, and retrieving
     * the generated key from the ResultSet.
     *
     * @param dsb The {@code DatasourceBuilder} for obtaining the datasource.
     * @return A {@code BiLambda} representing the update operation with a duration, input, and output.
     */
    @Override
    public BiLambda<Duration, I, O> apply(DatasourceBuilder dsb) {
        return (timeout, input) -> IO.task(() -> {
            return JfrEventDecorator.decorate(() -> {
                try (var connection = dsb.get().getConnection()) {
                    try (var ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setQueryTimeout((int) timeout.toSeconds());
                        int unused = setParams.apply(input).apply(ps);
                        assert unused > 0;
                        return mapResult.apply(input, ps.executeUpdate());
                    }
                }
            }, sql, enableJFR);
        }, Executors.newVirtualThreadPerTaskExecutor());
    }
}
