package jio.jdbc;

import jio.BiLambda;
import jio.IO;

import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A class representing a generic update operation with a generated key in a relational database using JDBC. The class
 * is designed to execute an SQL update statement, set parameters, and retrieve the generated key. The operation is
 * wrapped with Java Flight Recorder (JFR) events.
 *
 * <p>
 * Note: The operation is executed on a virtual thread.
 * </p>
 *
 * @param <I> The type of the input object for setting parameters in the update statement.
 * @param <O> The type of the output object generated from the ResultSet.
 */
public final class UpdateGenStm<I, O> implements Function<DatasourceBuilder, BiLambda<Duration, I, O>> {

    final String sql;

    final ParamsSetter<I> setParams;

    final BiFunction<I, Integer, ResultSetMapper<O>> mapResult;
    private final boolean enableJFR;

    UpdateGenStm(String sql, ParamsSetter<I> setParams, BiFunction<I,Integer, ResultSetMapper<O>> mapResult, boolean enableJFR) {
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
        return (timeout, req) -> IO.task(() -> {
            return JfrEventDecorator.decorate(() -> {
                try (var connection = dsb.get().getConnection()) {
                    try (var ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setQueryTimeout((int) timeout.toSeconds());
                        int unused = setParams.apply(req).apply(ps);
                        assert unused > 0;
                        int n = ps.executeUpdate();
                        try (ResultSet resultSet = ps.getGeneratedKeys()) {
                            if (resultSet.next()) return mapResult.apply(req,n).apply(resultSet);
                            throw new ColumnNotGeneratedException(sql);
                        }
                    }
                }
            }, sql, enableJFR);
        }, Executors.newVirtualThreadPerTaskExecutor());
    }
}
