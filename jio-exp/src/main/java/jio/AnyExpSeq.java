package jio;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import jio.Result.Failure;
import jio.Result.Success;

final class AnyExpSeq extends AnyExp {

  public AnyExpSeq(final List<IO<Boolean>> exps,
                   final Function<EvalExpEvent, BiConsumer<Boolean, Throwable>> debugger
                  ) {
    super(debugger,
          exps);
  }

  @Override
  public AnyExp retryEach(final Predicate<? super Throwable> predicate,
                          final RetryPolicy policy
                         ) {
    requireNonNull(predicate);
    requireNonNull(policy);
    return new AnyExpSeq(exps.stream()
                             .map(it -> it.retry(predicate,
                                                 policy
                                                ))
                             .toList(),
                         jfrPublisher
    );
  }

  @Override
  Result<Boolean> reduceExp() {
    return get(exps);
  }

  private Result<Boolean> get(final List<IO<Boolean>> exps) {

    var result = false;
    for (IO<Boolean> exp : exps) {
      try {
        if (result) {
          return new Success<>(true);
        } else {
          result = exp.call()
                      .getOutputOrThrow();
        }
      } catch (Exception e) {
        return new Failure<>(e);
      }
    }
    return new Success<>(result);
  }

  @Override
  public AnyExp debugEach(final EventBuilder<Boolean> eventBuilder) {
    Objects.requireNonNull(eventBuilder);
    return new AnyExpSeq(DebuggerHelper.debugConditions(exps,
                                                        eventBuilder
                                                       ),
                         getJFRPublisher(eventBuilder)
    );
  }

  @Override
  public AnyExp debugEach(final String context) {
    return debugEach(EventBuilder.of(this.getClass()
                                         .getSimpleName(),
                                     context)
                    );

  }
}
