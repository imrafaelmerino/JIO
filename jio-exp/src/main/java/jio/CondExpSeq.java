package jio;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

final class CondExpSeq<Output> extends CondExp<Output> {

  final List<IO<Boolean>> tests;
  final List<Supplier<IO<Output>>> consequences;
  final Supplier<IO<Output>> otherwise;

  public CondExpSeq(List<IO<Boolean>> tests,
                    List<Supplier<IO<Output>>> consequences,
                    Supplier<IO<Output>> otherwise,
                    Function<EvalExpEvent, BiConsumer<Output, Throwable>> debugger
                   ) {
    super(debugger);
    this.tests = tests;
    this.consequences = consequences;
    this.otherwise = otherwise;
  }

  private static <O> CompletableFuture<O> get(List<IO<Boolean>> tests,
                                              List<Supplier<IO<O>>> consequences,
                                              Supplier<IO<O>> otherwise,
                                              int condTestedSoFar
                                             ) {
    return condTestedSoFar == tests.size() ? otherwise.get()
                                                      .get() : tests.get(condTestedSoFar)
                                                                    .get()
                                                                    .thenCompose(result -> result ? consequences.get(
                                                                                                                    condTestedSoFar)
                                                                                                                .get()
                                                                                                                .get()
                                                                                                  : get(tests,
                                                                                                        consequences,
                                                                                                        otherwise,
                                                                                                        condTestedSoFar
                                                                                                        + 1)
                                                                                );

  }

  @Override
  CompletableFuture<Output> reduceExp() {
    return get(tests,
               consequences,
               otherwise,
               0
              );
  }

  @Override
  public CondExp<Output> retryEach(final Predicate<? super Throwable> predicate,
                                   final RetryPolicy policy
                                  ) {
    requireNonNull(predicate);
    requireNonNull(policy);
    return new CondExpSeq<>(tests.stream()
                                 .map(it -> it.retry(predicate,
                                                     policy
                                                    ))
                                 .collect(Collectors.toList()),
                            consequences.stream()
                                        .map(Fun.mapSupplier(it -> it.retry(predicate,
                                                                            policy)))
                                        .toList(),
                            otherwise,
                            jfrPublisher
    );
  }

  @Override
  public CondExp<Output> debugEach(final EventBuilder<Output> eventBuilder) {
    Objects.requireNonNull(eventBuilder);
    return new CondExpSeq<>(DebuggerHelper.debugConditions(tests,
                                                           EventBuilder.of("%s-test".formatted(eventBuilder.exp),
                                                                           eventBuilder.context)
                                                          ),
                            DebuggerHelper.debugSuppliers(consequences,
                                                          "%s-consequence".formatted(eventBuilder.exp),
                                                          eventBuilder.context
                                                         ),
                            DebuggerHelper.debugSupplier(otherwise,
                                                         "%s-otherwise".formatted(eventBuilder.exp),
                                                         eventBuilder.context
                                                        ),
                            getJFRPublisher(eventBuilder)
    );
  }

  @Override
  public CondExp<Output> debugEach(final String context) {
    return debugEach(
        EventBuilder.of(this.getClass()
                            .getSimpleName(),
                        context));

  }

}
