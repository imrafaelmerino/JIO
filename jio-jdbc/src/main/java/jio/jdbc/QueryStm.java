package jio.jdbc;

import jio.BiLambda;
import jio.IO;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
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
public final class QueryStm<I, O> implements Function<DatasourceBuilder, BiLambda<Duration, I, List<O>>> {


    private final ResultSetMapper<O> mapper;
    private final String sql;
    private final Function<I, PrStmSetter> setter;
    private final int fetchSize;
    private final boolean enableJFR;

    /**
     * Constructs a {@code QueryStm} with specified parameters.
     *
     * @param sqlQuery  The SQL query to execute.
     * @param setter    The parameter setter for the SQL query.
     * @param mapper    The result set mapper for processing query results.
     * @param fetchSize The fetch size for the query results.
     * @param enableJFR Indicates whether to enable Java Flight Recorder integration.
     */
    QueryStm(String sqlQuery, ParamsSetter<I> setter, ResultSetMapper<O> mapper, int fetchSize, boolean enableJFR) {
        this.sql = sqlQuery;
        this.mapper = mapper;
        this.setter = setter;
        this.fetchSize = fetchSize;
        this.enableJFR = enableJFR;
    }


    /**
     * Applies the specified {@code DatasourceBuilder} to create a lambda function for executing the SQL query.
     *
     * @param dsb The datasource builder for obtaining database connections.
     * @return A lambda function for executing the SQL query and processing results.
     */
    @Override
    public BiLambda<Duration, I, List<O>> apply(DatasourceBuilder dsb) {
        return (timeout, input) -> {
            return IO.task(() -> {
                try (var connection = dsb.get().getConnection()) {
                    try (var ps = connection.prepareStatement(sql)) {
                        return JfrEventDecorator.decorate(() -> {
                            var unused = setter.apply(input).apply(ps);
                            ps.setQueryTimeout((int) timeout.toSeconds());
                            ps.setFetchSize(fetchSize);
                            var rs = ps.executeQuery();
                            List<O> result = new ArrayList<>();
                            while (rs.next()) result.add(mapper.apply(rs));
                            return result;
                        }, sql, enableJFR);
                    }
                }
            }, Executors.newVirtualThreadPerTaskExecutor());
        };
    }
}
