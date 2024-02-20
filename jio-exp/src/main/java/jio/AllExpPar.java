package jio;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

final class AllExpPar extends AllExp {

  public AllExpPar(final List<IO<Boolean>> exps,
                   final Function<EvalExpEvent, BiConsumer<Boolean, Throwable>> debugger) {
    super(debugger,
          exps);
  }

  @Override
  public AllExp retryEach(final Predicate<? super Throwable> predicate,
                          final RetryPolicy policy) {
    requireNonNull(predicate);
    requireNonNull(policy);
    return new AllExpPar(exps.stream()
                             .map(it -> it.retry(predicate,
                                                 policy))
                             .toList(),
                         jfrPublisher);
  }

  @Override
  @SuppressWarnings("unchecked")
  CompletableFuture<Boolean> reduceExp() {
    CompletableFuture<Boolean>[] cfs = exps.stream()
                                           .map(Supplier::get)
                                           .toArray(CompletableFuture[]::new);

    return CompletableFuture.allOf(cfs)
                            .thenApply(l -> Arrays.stream(cfs)
                                                  .allMatch(CompletableFuture::join));
  }

  @Override
  public AllExp debugEach(final EventBuilder<Boolean> builder) {
    Objects.requireNonNull(builder);
    return new AllExpPar(DebuggerHelper.debugConditions(exps,
                                                        builder),
                         getJFRPublisher(builder));
  }

  @Override
  public AllExp debugEach(final String context) {
    return debugEach(EventBuilder.of(this.getClass()
                                         .getSimpleName(),
                                     context));

  }

}
