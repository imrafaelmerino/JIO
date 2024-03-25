package jio;

import static java.util.Objects.requireNonNull;

import fun.tuple.Triple;
import java.util.Objects;
import java.util.concurrent.StructuredTaskScope;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import jio.Result.Failure;
import jio.Result.Success;

final class TripleExpPar<First, Second, Third> extends TripleExp<First, Second, Third> {

  public TripleExpPar(final IO<First> _1,
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
    return new TripleExpPar<>(_1.retry(predicate,
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
  Result<Triple<First, Second, Third>> reduceExp() {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      var first = scope.fork(_1);
      var second = scope.fork(_2);
      var third = scope.fork(_3);
      scope.join()
           .throwIfFailed();
      return new Success<>(Triple.of(first.get()
                                          .getOutputOrThrow(),
                                     second.get()
                                           .getOutputOrThrow(),
                                     third.get()
                                          .getOutputOrThrow())
      );
    } catch (Exception e) {
      return new Failure<>(e);
    }
  }

  @Override
  public TripleExp<First, Second, Third> debugEach(final EventBuilder<Triple<First, Second, Third>> eventBuilder
                                                  ) {
    Objects.requireNonNull(eventBuilder);
    return new TripleExpPar<>(DebuggerHelper.debugIO(_1,
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
