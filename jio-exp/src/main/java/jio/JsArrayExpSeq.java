package jio;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import jio.Result.Failure;
import jio.Result.Success;
import jsonvalues.JsArray;
import jsonvalues.JsValue;

/**
 * Represents a supplier of a completable future which result is a json array. It has the same recursive structure as a
 * json array. Each index of the array is a completable future that it's executed asynchronously. When all the futures
 * are completed, all the results are combined into a json array.
 */

final class JsArrayExpSeq extends JsArrayExp {

  public JsArrayExpSeq(final List<IO<JsValue>> list,
                       final Function<EvalExpEvent, BiConsumer<JsArray, Throwable>> debugger
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
    List<JsValue> xs = new ArrayList<>(list.size());
    for (var entry : list) {
      try {
        xs.add(entry.call()
                    .getOutputOrThrow()
              );
      } catch (Exception e) {
        return new Failure<>(e);
      }
    }

    return new Success<>(JsArray.ofIterable(xs));
  }

  @Override
  public JsArrayExp retryEach(final Predicate<? super Throwable> predicate,
                              final RetryPolicy policy
                             ) {
    requireNonNull(predicate);
    requireNonNull(policy);
    return new JsArrayExpSeq(
        list.stream()
            .map(it -> it.retry(predicate,
                                policy
                               ))
            .collect(Collectors.toList()),
        jfrPublisher
    );
  }

  @Override
  public JsArrayExp debugEach(final EventBuilder<JsArray> eventBuilder) {
    Objects.requireNonNull(eventBuilder);
    return new JsArrayExpSeq(debugJsArray(list,
                                          eventBuilder
                                         ),
                             getJFRPublisher(eventBuilder)
    );

  }

  @Override
  public JsArrayExp debugEach(final String context) {
    return debugEach(EventBuilder.of(this.getClass()
                                         .getSimpleName(),
                                     context));

  }
}
