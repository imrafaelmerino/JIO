package jio;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;
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
final class JsObjExpPar extends JsObjExp {

  public JsObjExpPar(Map<String, IO<? extends JsValue>> bindings,
                     Function<EvalExpEvent, BiConsumer<JsObj, Throwable>> debugger
                    ) {
    super(bindings,
          debugger);
  }

  JsObjExpPar() {
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
  public JsObjExpPar set(final String key,
                         final IO<? extends JsValue> exp
                        ) {
    var xs = new HashMap<>(bindings);
    xs.put(requireNonNull(key),
           requireNonNull(exp)
          );
    return new JsObjExpPar(xs,
                           jfrPublisher);
  }

  /**
   * it triggers the execution of all the completable futures, combining the results into a JsObj
   *
   * @return a CompletableFuture of a json object
   */
  @Override
  Result<JsObj> reduceExp() {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

      List<String> keys = bindings.keySet()
                                  .stream()
                                  .toList();

      Map<String, Subtask<Result<? extends JsValue>>> tasks = keys.stream()
                                                                  .collect(Collectors.toMap(it -> it,
                                                                                            it -> scope.fork(bindings.get(it))
                                                                                           ));

      try {
        scope.join()
             .throwIfFailed();
        JsObj json = JsObj.empty();
        for (var entry : tasks.entrySet()) {
          json = json.set(entry.getKey(),
                          entry.getValue()
                               .get()
                               .getOutputOrThrow());
        }
        return new Success<>(json);

      } catch (Exception e) {
        return new Failure<>(e);
      }

    }
  }

  @Override
  public JsObjExp retryEach(final Predicate<? super Throwable> predicate,
                            final RetryPolicy policy
                           ) {
    Objects.requireNonNull(predicate);
    Objects.requireNonNull(policy);

    return new JsObjExpPar(bindings.entrySet()
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
  public JsObjExp debugEach(EventBuilder<JsObj> eventBuilder
                           ) {
    Objects.requireNonNull(eventBuilder);
    return new JsObjExpPar(debugJsObj(bindings,
                                      eventBuilder
                                     ),
                           getJFRPublisher(eventBuilder)
    );
  }

  @Override
  public JsObjExp debugEach(String context) {
    return debugEach(EventBuilder.of(this.getClass()
                                         .getSimpleName(),
                                     context));

  }
}
