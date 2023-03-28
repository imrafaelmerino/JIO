package jio;

import fun.tuple.Triple;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

final class TripleExpSeq<A, B, C> extends TripleExp<A, B, C> {

    @Override
    public TripleExp<A, B, C> retryEach(final Predicate<Throwable> predicate,
                                        final RetryPolicy policy
                                       ) {
        requireNonNull(predicate);
        requireNonNull(policy);
        return new TripleExpSeq<>(_1.retry(predicate,
                                           policy
                                          ),
                                  _2.retry(predicate,
                                           policy
                                          ),
                                  _3.retry(predicate,
                                           policy
                                          ),
                                  jfrPublisher
        );
    }

    @Override
    CompletableFuture<Triple<A, B, C>> reduceExp() {
        return _1.get()
                 .thenCompose(first -> _2.get()
                                         .thenCompose(second -> _3.get()
                                                                  .thenApply(third -> Triple.of(first,
                                                                                                second,
                                                                                                third
                                                                                               )
                                                                            )
                                                     )
                             );
    }

    public TripleExpSeq(final IO<A> _1,
                        final IO<B> _2,
                        final IO<C> _3,
                        final Function<ExpEvent, BiConsumer<Triple<A, B, C>, Throwable>> logger
                       ) {
        super(_1, _2, _3, logger);
    }

    @Override
    public TripleExp<A, B, C> debugEach(
            final EventBuilder<Triple<A, B, C>> builder
                                       ) {
        Objects.requireNonNull(builder);
        return new TripleExpSeq<>(LoggerHelper.debugIO(_1,
                                                       String.format("%s[1]",
                                                                     this.getClass().getSimpleName()
                                                                    ),
                                                       builder.context

                                                      ),
                                  LoggerHelper.debugIO(_2,
                                                       String.format("%s[2]",
                                                                     this.getClass().getSimpleName()
                                                                    ),
                                                       builder.context

                                                      ),
                                  LoggerHelper.debugIO(_3,
                                                       String.format("%s[3]",
                                                                     this.getClass().getSimpleName()
                                                                    ),
                                                       builder.context
                                                      ),
                                  getJFRPublisher(builder)
        );
    }


    @Override
    public TripleExp<A, B, C> debugEach(final String context) {
        return this.debugEach(
                EventBuilder.<Triple<A, B, C>>ofExp(this.getClass().getSimpleName())
                            .setContext(context)
                             );

    }
}
