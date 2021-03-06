package jio;

import fun.tuple.Pair;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

final class PairExpPar<A, B> extends PairExp<A, B> {

    public PairExpPar(final IO<A> _1, IO<B> _2,
                      final Function<ExpEvent, BiConsumer<Pair<A, B>, Throwable>> logger
                     ) {
        super(logger, _1, _2);
    }

    @Override
    public PairExp<A, B> retryEach(final Predicate<Throwable> predicate,
                                   final RetryPolicy policy
                                  ) {
        requireNonNull(predicate);
        requireNonNull(policy);
        return new PairExpPar<>(_1.retry(predicate,
                                         policy
                                        ),
                                _2.retry(predicate,
                                         policy
                                        ),
                                jfrPublisher
        );
    }

    @Override
    CompletableFuture<Pair<A, B>> reduceExp() {
        CompletableFuture<A> a = _1.get();
        CompletableFuture<B> b = _2.get();
        return a.thenCombine(b,
                             Pair::of
                            );
    }


    @Override
    public PairExp<A, B> debugEach(final EventBuilder<Pair<A, B>> messageBuilder
                                  ) {
        Objects.requireNonNull(messageBuilder);
        return new PairExpPar<>(LoggerHelper.debugIO(_1,
                                                     String.format("%s[1]",
                                                                   this.getClass().getSimpleName()
                                                                  ),
                                                     messageBuilder.context
                                                    ),
                                LoggerHelper.debugIO(_2,
                                                     String.format("%s[2]",
                                                                   this.getClass().getSimpleName()
                                                                  ),
                                                     messageBuilder.context

                                                    ),
                                getJFRPublisher(messageBuilder
                                               )
        );
    }


    @Override
    public PairExp<A, B> debugEach(final String context) {
        return this.debugEach(EventBuilder.<Pair<A, B>>ofExp(this.getClass().getSimpleName())
                                          .setContext(context)
                             );

    }
}
