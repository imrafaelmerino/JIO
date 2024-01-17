package jio.jdbc;

import java.util.Objects;
import java.util.function.Supplier;

public final class QueryStmBuilder<I, O> implements Supplier<QueryStm<I, O>> {
    private static final int DEFAULT_FETCH_SIZE = 1000;

    private final String sqlQuery;
    private final ParamsSetter<I> setter;
    private final ResultSetMapper<O> mapper;
    private int fetchSize = DEFAULT_FETCH_SIZE;
    private boolean enableJFR = true;

    private QueryStmBuilder(String sqlQuery, ParamsSetter<I> setter, ResultSetMapper<O> mapper) {
        this.sqlQuery = Objects.requireNonNull(sqlQuery);
        this.setter = Objects.requireNonNull(setter);
        this.mapper = Objects.requireNonNull(mapper);
    }

    public static <I, O> QueryStmBuilder<I, O> of(String sqlQuery, ParamsSetter<I> setter, ResultSetMapper<O> mapper) {
        return new QueryStmBuilder<>(sqlQuery, setter, mapper);
    }

    public QueryStmBuilder<I, O> withFetchSize(int fetchSize) {
        if (fetchSize <= 0) throw new IllegalArgumentException("fetchSize <=0");
        this.fetchSize = fetchSize;
        return this;
    }

    public QueryStmBuilder<I, O> withoutRecordedEvents() {
        this.enableJFR = false;
        return this;
    }

    @Override
    public QueryStm<I, O> get() {
        return new QueryStm<>(sqlQuery, setter, mapper, fetchSize, enableJFR);
    }
}