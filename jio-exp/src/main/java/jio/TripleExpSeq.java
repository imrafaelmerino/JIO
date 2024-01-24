package jio;

import fun.tuple.Triple;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

final class TripleExpSeq<First, Second, Third> extends TripleExp<First, Second, Third> {

  public TripleExpSeq(final IO<First> _1,
                      final IO<Second> _2,
                      final IO<Third> _3,
                      final Function<EvalExpEvent, BiConsumer<Triple<First, Second, Third>, Throwable>> debugger
                     ) {
    super(_1,
          _2,
          _3,
          debugger);
  }

  @Override
  public TripleExp<First, Second, Third> retryEach(final Predicate<? super Throwable> predicate,
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
  CompletableFuture<Triple<First, Second, Third>> reduceExp() {
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

  @Override
  public TripleExp<First, Second, Third> debugEach(final EventBuilder<Triple<First, Second, Third>> eventBuilder) {
    Objects.requireNonNull(eventBuilder);
    return new TripleExpSeq<>(DebuggerHelper.debugIO(_1,
                                                     String.format("%s[1]",
                                                                   eventBuilder.exp
                                                                  ),
                                                     eventBuilder.context

                                                    ),
                              DebuggerHelper.debugIO(_2,
                                                     String.format("%s[2]",
                                                                   eventBuilder.exp
                                                                  ),
                                                     eventBuilder.context

                                                    ),
                              DebuggerHelper.debugIO(_3,
                                                     String.format("%s[3]",
                                                                   eventBuilder.exp
                                                                  ),
                                                     eventBuilder.context
                                                    ),
                              getJFRPublisher(eventBuilder)
    );
  }


  @Override
  public TripleExp<First, Second, Third> debugEach(final String context) {
    return this.debugEach(EventBuilder.of(this.getClass()
                                              .getSimpleName(),
                                          context));

  }
}
