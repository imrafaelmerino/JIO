package jio.test.stub.effect;

import jio.IO;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executor;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

final class ValStub<O> extends IOStub<O> implements Supplier<IO<O>> {

    private int counter;
    private final IntFunction<O> getValue;
    private final IntFunction<Duration> delay;
    private final IO<O> effect;

    ValStub(final IntFunction<O> getValue,
            final IntFunction<Duration> delay,
            final Executor executor
           ) {
        this.executor = executor;
        this.getValue = requireNonNull(getValue);
        this.delay = requireNonNull(delay);
        this.effect = IO.effect(() -> {
            counter += 1;
            return delayValue(delay.apply(counter).toMillis(),
                              getValue.apply(counter)
                             );
        });

    }

    ValStub(final IntFunction<O> getValue,
            final Executor executor
           ) {
        this(getValue,
             $ -> Duration.of(0,
                              ChronoUnit.MILLIS
                             ),
             executor
            );

    }

    @Override
    public IO<O> get() {
        var stub = new ValStub<>(getValue, delay, executor);
        return wrapStub(stub.effect,
                        ()->
                                       stub.counter);
    }


}
