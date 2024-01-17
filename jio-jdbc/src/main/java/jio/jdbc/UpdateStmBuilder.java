package jio.jdbc;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public final class UpdateStmBuilder<I, O> implements Supplier<JdbcLambda<I, O>> {
    private final String sql;
    private final ParamsSetter<I> setParams;
    private final BiFunction<I, Integer, O> mapResult;
    private boolean enableJFR = true;

    private UpdateStmBuilder(String sql, ParamsSetter<I> setParams, BiFunction<I, Integer, O> mapResult) {
        this.sql = Objects.requireNonNull(sql);
        this.setParams = Objects.requireNonNull(setParams);
        this.mapResult = Objects.requireNonNull(mapResult);
    }

    public static <I, O> UpdateStmBuilder<I, O> of(String sql, ParamsSetter<I> setParams, BiFunction<I, Integer, O> mapResult) {
        return new UpdateStmBuilder<>(sql, setParams, mapResult);
    }


    public UpdateStmBuilder<I, O> withoutRecordedEvents() {
        this.enableJFR = false;
        return this;
    }

    @Override
    public JdbcLambda<I, O> get() {
        return new UpdateStm<>(sql, setParams, mapResult, enableJFR);
    }
}