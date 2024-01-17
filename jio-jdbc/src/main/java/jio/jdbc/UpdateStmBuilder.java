package jio.jdbc;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public final class UpdateStmBuilder<I, O> implements Supplier<UpdateStm<I, O>> {
    private final String sql;
    private final ParamsSetter<I> setParams;
    private final Function<I, ResultSetMapper<O>> mapResult;
    private boolean enableJFR;
    private UpdateStmBuilder(String sql, ParamsSetter<I> setParams, Function<I, ResultSetMapper<O>> mapResult) {
        this.sql = Objects.requireNonNull(sql);
        this.setParams = Objects.requireNonNull(setParams);
        this.mapResult = Objects.requireNonNull(mapResult);
    }

    public static <I, O> UpdateStmBuilder<I, O> of(String sql, ParamsSetter<I> setParams, Function<I, ResultSetMapper<O>> mapResult) {
        return new UpdateStmBuilder<>(sql, setParams, mapResult);
    }


    public UpdateStmBuilder<I, O> withoutRecordedEvents() {
        this.enableJFR = false;
        return this;
    }

    @Override
    public UpdateStm<I, O> get() {
        return new UpdateStm<>(sql, setParams, mapResult, enableJFR);
    }
}