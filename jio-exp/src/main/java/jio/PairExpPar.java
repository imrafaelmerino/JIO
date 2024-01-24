package jio;

import fun.tuple.Pair;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

final class PairExpPar<First, Second> extends PairExp<First, Second> {

  public PairExpPar(final IO<First> _1,
                    final IO<Second> _2,
                    final Function<EvalExpEvent, BiConsumer<Pair<First, Second>, Throwable>> debugger
                   ) {
    super(debugger,
          _1,
          _2);
  }

  @Override
  public PairExp<First, Second> retryEach(final Predicate<? super Throwable> predicate,
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
  CompletableFuture<Pair<First, Second>> reduceExp() {
    CompletableFuture<First> a = _1.get();
    CompletableFuture<Second> b = _2.get();
    return a.thenCombine(b,
                         Pair::of
                        );
  }


  @Override
  public PairExp<First, Second> debugEach(final EventBuilder<Pair<First, Second>> eventBuilder
                                         ) {
    Objects.requireNonNull(eventBuilder);
    return new PairExpPar<>(DebuggerHelper.debugIO(_1,
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
                            getJFRPublisher(eventBuilder)
    );
  }


  @Override
  public PairExp<First, Second> debugEach(final String context) {
    return this.debugEach(EventBuilder.of(this.getClass()
                                              .getSimpleName(),
                                          context));

  }


}
