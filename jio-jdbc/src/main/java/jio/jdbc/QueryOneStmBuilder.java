package jio.jdbc;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Builder class for constructing instances of {@link QueryOneStm}, which represents a JDBC query
 * operation returning a single result. This builder allows customization of the SQL query, parameter setting,
 * result mapping, and the option to disable Java Flight Recorder (JFR) event recording for the query execution.
 *
 * @param <I> The type of input parameters for the JDBC query.
 * @param <O> The type of the output result for the JDBC query.
 */
public final class QueryOneStmBuilder<I, O> implements Supplier<QueryOneStm<I, O>> {

    private final String sqlQuery;
    private final ParamsSetter<I> setter;
    private final ResultSetMapper<O> mapper;
    private boolean enableJFR = true;

    private QueryOneStmBuilder(String sqlQuery, ParamsSetter<I> setter, ResultSetMapper<O> mapper) {
        this.sqlQuery = Objects.requireNonNull(sqlQuery);
        this.setter = Objects.requireNonNull(setter);
        this.mapper = Objects.requireNonNull(mapper);
    }

    /**
     * Creates a new instance of {@code QueryOneStmBuilder} with the specified SQL query,
     * parameter setter, and result mapper.
     *
     * @param sqlQuery The SQL query string.
     * @param setter   The parameter setter for the SQL query.
     * @param mapper   The result mapper for mapping query results.
     * @param <I>      The type of input parameters for the JDBC query.
     * @param <O>      The type of the output result for the JDBC query.
     * @return A new instance of {@code QueryOneStmBuilder}.
     */
    public static <I, O> QueryOneStmBuilder<I, O> of(String sqlQuery, ParamsSetter<I> setter, ResultSetMapper<O> mapper) {
        return new QueryOneStmBuilder<>(sqlQuery, setter, mapper);
    }

    /**
     * Disables the recording of Java Flight Recorder (JFR) events for the JDBC query execution.
     *
     * @return This {@code QueryOneStmBuilder} instance with JFR event recording disabled.
     */
    public QueryOneStmBuilder<I, O> withoutRecordedEvents() {
        this.enableJFR = false;
        return this;
    }

    /**
     * Builds and returns a new instance of {@link QueryOneStm} based on the configured parameters.
     *
     * @return A new instance of {@code QueryOneStm}.
     */
    @Override
    public QueryOneStm<I, O> get() {
        return new QueryOneStm<>(sqlQuery, setter, mapper, enableJFR);
    }
}