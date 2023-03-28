package jio.test.stub.value;

import jio.IO;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executor;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

final class FailureStub<O> extends IOStub<O> implements Supplier<IO<O>> {
    private final IntFunction<Duration> delay;
    private int counter;
    private final IntFunction<Throwable> getError;
    private final O value;
    private final IO<O> effect;

    FailureStub(final IntFunction<Throwable> getError,
                final O value,
                final Executor executor
               ) {
        this(requireNonNull(getError),
             n -> Duration.of(0,
                              ChronoUnit.MILLIS
                             ),
             value,
             executor
            );
    }

    FailureStub(final IntFunction<Throwable> getError,
                final IntFunction<Duration> delay,
                final O value,
                final Executor executor
               ) {
        this.executor = executor;
        this.getError = requireNonNull(getError);
        this.delay = requireNonNull(delay);
        this.value = value;
        this.effect = IO.effect(() -> {
            counter += 1;
            long millis = delay.apply(counter)
                               .toMillis();
            final Throwable ex = getError.apply(counter);
            return ex != null ?
                    delayExc(millis,
                             ex
                            ) :
                    delayValue(millis,
                               value
                              );

        });
    }

    @Override
    public IO<O> get() {
        FailureStub<O> stub = new FailureStub<>(getError,
                                                delay,
                                                value,
                                                executor
        );
        return wrapStub(stub.effect,()->stub.counter);
    }
}
