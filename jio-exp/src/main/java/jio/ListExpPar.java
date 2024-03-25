package jio;

import static java.util.Objects.requireNonNull;

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

final class ListExpPar<Elem> extends ListExp<Elem> {

  ListExpPar(final List<IO<Elem>> list,
             final Function<EvalExpEvent, BiConsumer<List<Elem>, Throwable>> debugger
            ) {
    super(list,
          debugger);
  }

  @Override
  public ListExp<Elem> append(final IO<Elem> val) {
    var xs = new ArrayList<>(list);
    xs.add(requireNonNull(val));
    return new ListExpPar<>(xs,
                            jfrPublisher);
  }

  @Override
  public ListExp<Elem> tail() {
    return new ListExpPar<>(list.subList(1,
                                         list.size()),
                            jfrPublisher
    );
  }

  @Override
  public ListExp<Elem> retryEach(final Predicate<? super Throwable> predicate,
                                 final RetryPolicy policy
                                ) {
    requireNonNull(policy);
    requireNonNull(predicate);

    return new ListExpPar<>(list.stream()
                                .map(it -> it.retry(predicate,
                                                    policy
                                                   )
                                    )
                                .toList(),
                            jfrPublisher
    );
  }

  @Override
  Result<List<Elem>> reduceExp() {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

      List<Subtask<Result<Elem>>> xs = list.stream()
                                           .map(scope::fork)
                                           .toList();
      scope.join()
           .throwIfFailed();
      List<Elem> result = new ArrayList<>();
      for (Subtask<Result<Elem>> task : xs) {
        Elem call = task.get()
                        .getOutputOrThrow();
        result.add(call);
      }
      return new Success<>(result);

    } catch (Exception e) {
      return new Failure<>(e);
    }
  }

  @Override
  public ListExp<Elem> debugEach(final EventBuilder<List<Elem>> eventBuilder
                                ) {
    Objects.requireNonNull(eventBuilder);
    return new ListExpPar<>(DebuggerHelper.debugList(list,
                                                     eventBuilder.exp,
                                                     eventBuilder.context
                                                    ),
                            getJFRPublisher(eventBuilder)
    );
  }

  @Override
  public ListExp<Elem> debugEach(String context) {
    return debugEach(EventBuilder.of(this.getClass()
                                         .getSimpleName(),
                                     context));

  }
}
