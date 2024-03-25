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
import java.util.stream.Collectors;
import jio.Result.Failure;
import jio.Result.Success;
import jsonvalues.JsArray;
import jsonvalues.JsValue;

final class JsArrayExpPar extends JsArrayExp {

  public JsArrayExpPar(List<IO<JsValue>> list,
                       Function<EvalExpEvent, BiConsumer<JsArray, Throwable>> debugger
                      ) {
    super(list,
          debugger);
  }

  /**
   * it triggers the execution of all the completable futures, combining the results into a JsArray
   *
   * @return a CompletableFuture of a json array
   */
  @Override
  Result<JsArray> reduceExp() {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

      List<Subtask<Result<JsValue>>> xs = list.stream()
                                              .map(scope::fork)
                                              .toList();
      scope.join()
           .throwIfFailed();
      List<JsValue> result = new ArrayList<>();
      for (var task : xs) {
        JsValue call = task.get()
                           .getOutputOrThrow();
        result.add(call);
      }
      return new Success<>(JsArray.ofIterable(result));

    } catch (Exception e) {
      return new Failure<>(e);
    }
  }

  @Override
  public JsArrayExp retryEach(final Predicate<? super Throwable> predicate,
                              final RetryPolicy policy
                             ) {
    requireNonNull(predicate);
    requireNonNull(policy);

    return new JsArrayExpPar(list.stream()
                                 .map(it -> it.retry(predicate,
                                                     policy
                                                    )
                                     )
                                 .collect(Collectors.toList()),
                             jfrPublisher
    );
  }

  @Override
  public JsArrayExp debugEach(final EventBuilder<JsArray> eventBuilder
                             ) {
    Objects.requireNonNull(eventBuilder);
    return new JsArrayExpPar(debugJsArray(list,
                                          eventBuilder
                                         ),
                             getJFRPublisher(eventBuilder)
    );
  }

  @Override
  public JsArrayExp debugEach(final String context) {
    return this.debugEach(EventBuilder.of(this.getClass()
                                              .getSimpleName(),
                                          context));

  }
}
