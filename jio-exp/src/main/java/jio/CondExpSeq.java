package jio;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

final class CondExpSeq<O> extends CondExp<O> {

    final List<IO<Boolean>> tests;
    final List<Supplier<IO<O>>> consequences;
    final Supplier<IO<O>> otherwise;

    public CondExpSeq(List<IO<Boolean>> tests,
                      List<Supplier<IO<O>>> consequences,
                      Supplier<IO<O>> otherwise,
                      Function<ExpEvent, BiConsumer<O, Throwable>> debugger
                     ) {
        super(debugger);
        this.tests = tests;
        this.consequences = consequences;
        this.otherwise = otherwise;
    }

    private static <O> CompletableFuture<O> get(List<IO<Boolean>> tests,
                                                List<Supplier<IO<O>>> consequences,
                                                Supplier<IO<O>> otherwise,
                                                int condTestedSoFar
                                               ) {
        return condTestedSoFar == tests.size() ?
                otherwise.get().get() :
                tests.get(condTestedSoFar).get()
                     .thenCompose(result -> result ?
                                          consequences.get(condTestedSoFar).get().get() :
                                          get(tests, consequences, otherwise, condTestedSoFar + 1)
                                 );

    }

    @Override
    CompletableFuture<O> reduceExp() {
        return get(tests,
                   consequences,
                   otherwise,
                   0
                  );
    }

    @Override
    public CondExp<O> retryEach(final Predicate<Throwable> predicate,
                                final RetryPolicy policy
                               ) {
        requireNonNull(predicate);
        requireNonNull(policy);
        return new CondExpSeq<>(tests.stream()
                                     .map(it -> it.retry(predicate,
                                                         policy
                                                        ))
                                     .collect(Collectors.toList()),
                                consequences.stream()
                                            .map(Fun.mapSupplier(it -> it.retry(predicate, policy)))
                                            .toList(),
                                otherwise,
                                jfrPublisher
        );
    }

    @Override
    public CondExp<O> debugEach(final EventBuilder<O> eventBuilder) {
        Objects.requireNonNull(eventBuilder);
        return new CondExpSeq<>(LoggerHelper.debugConditions(tests,
                                                             new EventBuilder<>("%s-test".formatted(eventBuilder.exp),
                                                                                eventBuilder.context)
                                                            ),
                                LoggerHelper.debugSuppliers(consequences,
                                                            "%s-consequence".formatted(eventBuilder.exp),
                                                            eventBuilder.context
                                                           ),
                                LoggerHelper.debugSupplier(otherwise,
                                                           "%s-otherwise".formatted(eventBuilder.exp),
                                                           eventBuilder.context
                                                          ),
                                getJFRPublisher(eventBuilder)
        );
    }


    @Override
    public CondExp<O> debugEach(final String context) {
        return debugEach(
                new EventBuilder<>(this.getClass().getSimpleName(), context));

    }

}
