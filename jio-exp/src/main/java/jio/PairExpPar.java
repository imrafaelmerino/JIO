package jio;

import static java.util.Objects.requireNonNull;

import fun.tuple.Pair;
import java.util.Objects;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import jio.Result.Failure;
import jio.Result.Success;

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
  Result<Pair<First, Second>> reduceExp() {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      Subtask<Result<First>> first = scope.fork(_1);
      Subtask<Result<Second>> second = scope.fork(_2);
      scope.join()
           .throwIfFailed();
      return new Success<>(Pair.of(first.get()
                                        .getOutput(),
                                   second.get()
                                         .getOutput())
      );
    } catch (Exception e) {
      return new Failure<>(e);
    }
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
