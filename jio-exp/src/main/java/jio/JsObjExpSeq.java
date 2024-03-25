package jio;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import jio.Result.Failure;
import jio.Result.Success;
import jsonvalues.JsObj;
import jsonvalues.JsValue;

/**
 * Represents a supplier of a completable future which result is a json object. It has the same recursive structure as a
 * json object. Each key has a completable future associated that it's executed asynchronously. When all the futures are
 * completed, all the results are combined into a json object.
 */
final class JsObjExpSeq extends JsObjExp {

  public JsObjExpSeq(final Map<String, IO<? extends JsValue>> bindings,
                     final Function<EvalExpEvent, BiConsumer<JsObj, Throwable>> debugger
                    ) {
    super(bindings,
          debugger);
  }

  JsObjExpSeq() {
    super(new LinkedHashMap<>(),
          null);
  }

  /**
   * returns a new object future inserting the given future at the given key
   *
   * @param key the given key
   * @param exp the given effect
   * @return a new JsObjFuture
   */
  @Override
  public JsObjExpSeq set(final String key,
                         final IO<? extends JsValue> exp
                        ) {
    var xs = new HashMap<>(bindings);
    xs.put(requireNonNull(key),
           requireNonNull(exp)
          );
    return new JsObjExpSeq(xs,
                           jfrPublisher);
  }

  /**
   * it triggers the execution of all the completable futures, combining the results into a JsObj
   *
   * @return a CompletableFuture of a json object
   */
  @Override
  Result<JsObj> reduceExp() {

    JsObj result = JsObj.empty();
    for (var entry : bindings.entrySet()) {
      try {
        result = result.set(entry.getKey(),
                            entry.getValue()
                                 .call()
                                 .getOutputOrThrow());
      } catch (Exception e) {
        return new Failure<>(e);
      }
    }

    return new Success<>(result);
  }

  @Override
  public JsObjExp retryEach(final Predicate<? super Throwable> predicate,
                            final RetryPolicy policy
                           ) {
    Objects.requireNonNull(policy);
    Objects.requireNonNull(predicate);

    return new JsObjExpSeq(bindings.entrySet()
                                   .stream()
                                   .collect(Collectors.toMap(Map.Entry::getKey,
                                                             e -> e.getValue()
                                                                   .retry(predicate,
                                                                          policy
                                                                         )
                                                            )
                                           ),
                           jfrPublisher
    );
  }

  @Override
  public JsObjExp debugEach(final EventBuilder<JsObj> eventBuilder
                           ) {
    Objects.requireNonNull(eventBuilder);
    return new JsObjExpSeq(debugJsObj(bindings,
                                      eventBuilder
                                     ),
                           getJFRPublisher(eventBuilder)
    );
  }

  @Override
  public JsObjExp debugEach(final String context) {
    return this.debugEach(EventBuilder.of(this.getClass()
                                              .getSimpleName(),
                                          context));

  }
}
