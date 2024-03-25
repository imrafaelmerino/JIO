package jio;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import jio.Result.Failure;

final class CondExpPar<Output> extends CondExp<Output> {

  private final List<IO<Boolean>> tests;
  private final List<Supplier<IO<Output>>> consequences;
  private final Supplier<IO<Output>> otherwise;

  public CondExpPar(final List<IO<Boolean>> tests,
                    final List<Supplier<IO<Output>>> consequences,
                    final Supplier<IO<Output>> otherwise,
                    final Function<EvalExpEvent, BiConsumer<Output, Throwable>> debugger
                   ) {
    super(debugger);
    this.tests = tests;
    this.consequences = consequences;
    this.otherwise = otherwise;
  }

  @Override
  Result<Output> reduceExp() {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

      List<Subtask<Result<Boolean>>> tasks = tests.stream()
                                                  .map(scope::fork)
                                                  .toList();

      try {
        scope.join()
             .throwIfFailed();
        return getFirstThatIsTrueOrDefault(tasks);
      } catch (Exception e) {
        return new Failure<>(e);
      }
    }
  }

  private Result<Output> getFirstThatIsTrueOrDefault(List<Subtask<Result<Boolean>>> tasks) throws Exception {

    for (int i = 0; i < tasks.size(); i++) {
      if (tasks.get(i)
               .get()
               .getOutputOrThrow()) {
        return consequences.get(i)
                           .get()
                           .call();
      }
    }
    return otherwise.get()
                    .call();
  }

  @Override
  public CondExp<Output> retryEach(final Predicate<? super Throwable> predicate,
                                   final RetryPolicy policy
                                  ) {
    requireNonNull(predicate);
    requireNonNull(policy);
    return new CondExpPar<>(tests.stream()
                                 .map(it -> it.retry(predicate,
                                                     policy
                                                    ))
                                 .collect(Collectors.toList()),
                            consequences
                                .stream()
                                .map(Fun.mapSupplier(it -> it.retry(predicate,
                                                                    policy)))
                                .toList(),
                            otherwise,
                            jfrPublisher
    );
  }

  @Override
  public CondExp<Output> debugEach(final EventBuilder<Output> eventBuilder
                                  ) {
    Objects.requireNonNull(eventBuilder);
    return new CondExpPar<>(DebuggerHelper.debugConditions(tests,
                                                           EventBuilder.of("%s-test".formatted(eventBuilder.exp),
                                                                           eventBuilder.context)
                                                          ),
                            DebuggerHelper.debugSuppliers(consequences,
                                                          "%s-consequence".formatted(eventBuilder.exp),
                                                          eventBuilder.context
                                                         ),
                            DebuggerHelper.debugSupplier(
                                otherwise,
                                "%s-otherwise".formatted(eventBuilder.exp),
                                eventBuilder.context
                                                        ),
                            getJFRPublisher(eventBuilder)
    );
  }

  @Override
  public CondExp<Output> debugEach(final String context) {
    return debugEach(EventBuilder.of(this.getClass()
                                         .getSimpleName(),
                                     context));

  }
}
