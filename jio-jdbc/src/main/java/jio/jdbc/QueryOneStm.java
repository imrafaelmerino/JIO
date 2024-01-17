package jio.jdbc;

import jio.BiLambda;
import jio.IO;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * Represents a utility class for executing parameterized SQL queries and processing the results. Optionally integrates
 * with Java Flight Recorder for monitoring.
 *
 * <p>
 * Note: The operation is executed on a virtual thread.
 * </p>
 *
 * @param <I> Type of the input parameters for the SQL query.
 * @param <O> Type of the objects produced by the result set mapper.
 */
public final class QueryOneStm<I, O> implements JdbcLambda<I, O> {


    private final ResultSetMapper<O> mapper;
    private final String sql;
    private final Function<I, PrStmSetter> setter;
    private final boolean enableJFR;

    /**
     * Constructs a {@code QueryStm} with specified parameters.
     *
     * @param sqlQuery  The SQL query to execute.
     * @param setter    The parameter setter for the SQL query.
     * @param mapper    The result set mapper for processing query results.
     * @param enableJFR Indicates whether to enable Java Flight Recorder integration.
     */
    QueryOneStm(String sqlQuery, ParamsSetter<I> setter, ResultSetMapper<O> mapper, boolean enableJFR) {
        this.sql = sqlQuery;
        this.mapper = mapper;
        this.setter = setter;
        this.enableJFR = enableJFR;
    }


    /**
     * Applies the specified {@code DatasourceBuilder} to create a lambda function for executing the SQL query.
     *
     * @param dsb The datasource builder for obtaining database connections.
     * @return A lambda function for executing the SQL query and processing results.
     */
    @Override
    public BiLambda<Duration, I, O> apply(DatasourceBuilder dsb) {
        return (timeout, input) -> {
            return IO.task(() -> {
                try (var connection = dsb.get().getConnection()) {
                    try (var ps = connection.prepareStatement(sql)) {
                        return JfrEventDecorator.decorate(() -> {
                            var unused = setter.apply(input).apply(ps);
                            ps.setQueryTimeout((int) timeout.toSeconds());
                            ps.setFetchSize(1);
                            var rs = ps.executeQuery();
                            return rs.next() ? mapper.apply(rs) : null;
                        }, sql, enableJFR);
                    }
                }
            }, Executors.newVirtualThreadPerTaskExecutor());
        };
    }
}
