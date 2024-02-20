package jio;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

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
  CompletableFuture<Boolean> reduceExp() {
    return get(exps);
  }

  private CompletableFuture<Boolean> get(final List<IO<Boolean>> exps) {

    return exps.size() == 1 ? exps.get(0)
                                  .get() : exps.get(0)
                                               .get()
                                               .thenCompose(bool -> bool ? CompletableFuture.completedFuture(true)
                                                   : get(exps.subList(1,
                                                                      exps.size())));
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
                                     context));

  }
}
