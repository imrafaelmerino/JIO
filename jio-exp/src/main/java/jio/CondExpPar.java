package jio;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;


final class CondExpPar<O> extends CondExp<O> {

    private final List<IO<Boolean>> tests;
    private final List<Supplier<IO<O>>> consequences;
    private final Supplier<IO<O>> otherwise;

    public CondExpPar(final List<IO<Boolean>> tests, List<Supplier<IO<O>>> consequences,
                      final Supplier<IO<O>> otherwise,
                      final Function<ExpEvent, BiConsumer<O, Throwable>> debugger
                     ) {
        super(debugger);
        this.tests = tests;
        this.consequences = consequences;
        this.otherwise = otherwise;
    }

    @Override
    CompletableFuture<O> reduceExp() {
        @SuppressWarnings("unchecked")
        CompletableFuture<Boolean>[] cfs = tests.stream()
                                                .map(Supplier::get)
                                                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(cfs)
                                .thenCompose($ -> getFirstThatIsTrueOrDefault(cfs));
    }

    private CompletableFuture<O> getFirstThatIsTrueOrDefault(CompletableFuture<Boolean>[] cfs) {
        List<Boolean> predicatesResults = Arrays.stream(cfs)
                                                .map(CompletableFuture::join)
                                                .toList();
        for (int i = 0; i < predicatesResults.size(); i++) {
            if (predicatesResults.get(i)) return consequences.get(i).get().get();
        }
        return otherwise.get().get();
    }


    @Override
    public CondExp<O> retryEach(final Predicate<? super Throwable> predicate,
                                final RetryPolicy policy
                               ) {
        requireNonNull(predicate);
        requireNonNull(policy);
        return new CondExpPar<>(tests.stream()
                                     .map(it -> it.retry(predicate,
                                                         policy
                                                        ))
                                     .collect(Collectors.toList()),
                                consequences
                                        .stream()
                                        .map(Fun.mapSupplier(it -> it.retry(predicate, policy)))
                                        .toList(),
                                otherwise,
                                jfrPublisher
        );
    }


    @Override
    public CondExp<O> debugEach(final EventBuilder<O> eventBuilder
                               ) {
        Objects.requireNonNull(eventBuilder);
        return new CondExpPar<>(DebuggerHelper.debugConditions(tests,
                                                               new EventBuilder<>("%s-test".formatted(eventBuilder.exp),
                                                                                eventBuilder.context)
                                                              ),
                                DebuggerHelper.debugSuppliers(consequences,
                                                              "%s-consequence".formatted(eventBuilder.exp),
                                                              eventBuilder.context
                                                             ),
                                DebuggerHelper.debugSupplier(
                                        otherwise,
                                        "%s-otherwise".formatted(eventBuilder.exp),
                                        eventBuilder.context
                                                            ),
                                getJFRPublisher(eventBuilder)
        );
    }


    @Override
    public CondExp<O> debugEach(final String context) {
        return debugEach(new EventBuilder<>(this.getClass().getSimpleName(), context));


    }
}
