package jio.jdbc;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public final class UpdateGenStmBuilder<I, O> implements Supplier<UpdateGenStm<I, O>> {
    private final String sql;
    private final ParamsSetter<I> setParams;
    private final BiFunction<I, Integer, ResultSetMapper<O>> mapResult;
    private boolean enableJFR;

    private UpdateGenStmBuilder(String sql, ParamsSetter<I> setParams, BiFunction<I, Integer, ResultSetMapper<O>> mapResult) {
        this.sql = Objects.requireNonNull(sql);
        this.setParams = Objects.requireNonNull(setParams);
        this.mapResult = Objects.requireNonNull(mapResult);
    }

    public static <I, O> UpdateGenStmBuilder<I, O> of(String sql, ParamsSetter<I> setParams, BiFunction<I, Integer, ResultSetMapper<O>> mapResult) {
        return new UpdateGenStmBuilder<>(sql, setParams, mapResult);
    }

    public UpdateGenStmBuilder<I, O> withoutRecordedEvents() {
        this.enableJFR = false;
        return this;
    }

    @Override
    public UpdateGenStm<I, O> get() {
        return new UpdateGenStm<>(sql, setParams, mapResult, enableJFR);
    }
}