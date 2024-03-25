package jio;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import jio.Result.Failure;
import jio.Result.Success;

final class AnyExpPar extends AnyExp {

  public AnyExpPar(final List<IO<Boolean>> exps,
                   final Function<EvalExpEvent, BiConsumer<Boolean, Throwable>> debugger
                  ) {
    super(debugger,
          exps);
  }

  @Override
  public AnyExp retryEach(final Predicate<? super Throwable> predicate,
                          final RetryPolicy policy
                         ) {
    Objects.requireNonNull(predicate);
    Objects.requireNonNull(policy);
    return new AnyExpPar(exps.stream()
                             .map(it -> it.retry(predicate,
                                                 policy
                                                ))
                             .toList(),
                         jfrPublisher
    );
  }

  @Override
  Result<Boolean> reduceExp() {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      List<Subtask<Result<Boolean>>> computed = new ArrayList<>(exps.size());
      for (var task : exps) {
        computed.add(scope.fork(task));
      }
      try {
        scope.join()
             .throwIfFailed();
        return new Success<>(computed.stream()
                                     .anyMatch(task -> task.get()
                                                           .equals(Result.TRUE)));  // Throws if none of the subtasks completed successfully
      } catch (Exception e) {
        return new Failure<>(e);
      }
    }
  }

  @Override
  public AnyExp debugEach(final EventBuilder<Boolean> eventBuilder) {
    Objects.requireNonNull(eventBuilder);
    return new AnyExpPar(DebuggerHelper.debugConditions(exps,
                                                        eventBuilder
                                                       ),
                         getJFRPublisher(eventBuilder)
    );
  }

  @Override
  public AnyExp debugEach(final String context) {
    return debugEach(EventBuilder.of(this.getClass()
                                         .getSimpleName(),
                                     context));

  }
}
