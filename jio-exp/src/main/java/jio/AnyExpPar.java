package jio;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;


final class AnyExpPar extends AnyExp {


    public AnyExpPar(final List<IO<Boolean>> exps,
                     final Function<ExpEvent, BiConsumer<Boolean, Throwable>> debugger
                    ) {
        super(debugger, exps);
    }

    @Override
    public AnyExp retryEach(final Predicate<? super Throwable> predicate,
                            final RetryPolicy policy
                           ) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(policy);
        return new AnyExpPar(exps.stream()
                                 .map(it -> it.retry(predicate,
                                                     policy
                                                    ))
                                 .toList(),
                             jfrPublisher
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    CompletableFuture<Boolean> reduceExp() {
        CompletableFuture<Boolean>[] cfs = exps.stream()
                                               .map(Supplier::get)
                                               .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(cfs)
                                .thenApply(l -> Arrays.stream(cfs)
                                                      .anyMatch(CompletableFuture::join)
                                          );
    }

    @Override
    public AnyExp debugEach(final EventBuilder<Boolean> eventBuilder) {
        Objects.requireNonNull(eventBuilder);
        return new AnyExpPar(DebuggerHelper.debugConditions(exps,
                                                            eventBuilder
                                                           ),
                             getJFRPublisher(eventBuilder)
        );
    }


    @Override
    public AnyExp debugEach(final String context) {
        return debugEach(new EventBuilder<>(this.getClass().getSimpleName(), context));

    }
}
