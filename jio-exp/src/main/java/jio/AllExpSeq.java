package jio;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import jio.Result.Failure;
import jio.Result.Success;

final class AllExpSeq extends AllExp {

  public AllExpSeq(List<IO<Boolean>> exps,
                   Function<EvalExpEvent, BiConsumer<Boolean, Throwable>> debugger
                  ) {
    super(debugger,
          exps
         );
  }

  @Override
  public AllExp retryEach(final Predicate<? super Throwable> predicate,
                          final RetryPolicy policy
                         ) {
    requireNonNull(predicate);
    requireNonNull(policy);

    return new AllExpSeq(exps.stream()
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

  private Result<Boolean> get(List<IO<Boolean>> exps) {
    var result = true;
    for (IO<Boolean> exp : exps) {
      try {
        if (result) {
          result = exp.call()
                      .getOutputOrThrow();
        } else {
          return new Success<>(false);
        }
      } catch (Exception e) {
        return new Failure<>(e);
      }
    }
    return new Success<>(result);

  }

  @Override
  public AllExp debugEach(final EventBuilder<Boolean> builder) {
    Objects.requireNonNull(builder);
    return new AllExpSeq(DebuggerHelper.debugConditions(exps,
                                                        builder
                                                       ),
                         getJFRPublisher(builder)
    );
  }

  @Override
  public AllExp debugEach(final String context) {
    return debugEach(EventBuilder.of(this.getClass()
                                         .getSimpleName(),
                                     context
                                    ));

  }
}
