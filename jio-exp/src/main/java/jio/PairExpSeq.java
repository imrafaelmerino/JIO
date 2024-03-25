package jio;

import static java.util.Objects.requireNonNull;

import fun.tuple.Pair;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import jio.Result.Failure;

final class PairExpSeq<First, Second> extends PairExp<First, Second> {

  public PairExpSeq(final IO<First> _1,
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
    return new PairExpSeq<>(_1.retry(predicate,
                                     policy
                                    ),
                            _2.retry(predicate,
                                     policy
                                    ),
                            jfrPublisher
    );
  }

  @Override
  Result<Pair<First, Second>> reduceExp() {
    try {
      var first = _1.call()
                    .getOutputOrThrow();
      var second = _2.call()
                     .getOutputOrThrow();
      return new Result.Success<>(Pair.of(first,
                                          second
                                         ));
    } catch (Exception e) {
      return new Failure<>(e);
    }
  }

  @Override
  public PairExp<First, Second> debugEach(final EventBuilder<Pair<First, Second>> eventBuilder) {
    Objects.requireNonNull(eventBuilder);
    return new PairExpSeq<>(DebuggerHelper.debugIO(_1,
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
                                          context)
                         );

  }
}
