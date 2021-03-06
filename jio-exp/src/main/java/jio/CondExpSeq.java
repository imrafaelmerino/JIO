package jio;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

final class CondExpSeq<O> extends CondExp<O> {

    final List<IO<Boolean>> tests;
    final List<Supplier<IO<O>>> consequences;
    final Supplier<IO<O>> otherwise;

    public CondExpSeq(List<IO<Boolean>> tests,
                      List<Supplier<IO<O>>> consequences,
                      Supplier<IO<O>> otherwise,
                      Function<ExpEvent,BiConsumer<O, Throwable>> logger
                     ) {
        super(logger);
        this.tests = tests;
        this.consequences = consequences;
        this.otherwise = otherwise;
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
    public CondExp<O> debugEach(
                              final EventBuilder<O> messageBuilder
                               ) {
        Objects.requireNonNull(messageBuilder);
        return new CondExpSeq<>(LoggerHelper.debugConditions(
                                                             tests,
                                                             this.getClass().getSimpleName() + "-test",
                                                             messageBuilder.context
                                                            ),
                                LoggerHelper.debugSuppliers(
                                                            consequences,
                                                            this.getClass().getSimpleName() + "-consequence",
                                                            messageBuilder.context
                                                           ),
                                LoggerHelper.debugSupplier(
                                                           otherwise,
                                                           this.getClass().getSimpleName() + "-otherwise",
                                                           messageBuilder.context
                                                          ),
                                getJFRPublisher(
                                               messageBuilder
                                               )
        );
    }


    @Override
    public CondExp<O> debugEach(final String context) {
        return debugEach(
                EventBuilder.<O>ofExp(this.getClass().getSimpleName())
                            .setContext(context)
                        );

    }

}
