package jio;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import jsonvalues.JsArray;
import jsonvalues.JsValue;

/**
 * Represents a sequence of effects that produce JSON values ({@link JsValue}) and combines them into a JSON array. This
 * abstraction allows for the evaluation of these effects either in parallel or sequentially. If any of the effects
 * fail, the entire expression fails.
 */
public abstract sealed class JsArrayExp extends Exp<JsArray> permits JsArrayExpPar, JsArrayExpSeq {

  final List<IO<JsValue>> list;

  JsArrayExp(List<IO<JsValue>> list,
             Function<EvalExpEvent, BiConsumer<JsArray, Throwable>> debugger
            ) {
    super(debugger);
    this.list = list;
  }

  /**
   * Creates a JsArray expression that evaluates the effects sequentially, one after the other. If any effect fails, the
   * entire expression fails, and subsequent effects are not evaluated.
   *
   * @param effects the effects to be evaluated sequentially
   * @return a JsArrayExp for sequential evaluation
   */
  @SafeVarargs
  public static JsArrayExp seq(final IO<JsValue>... effects) {
    var list = new ArrayList<IO<JsValue>>();
    for (var other : requireNonNull(effects)) {
      list.add(requireNonNull(other));
    }
    return new JsArrayExpSeq(list,
                             null);
  }

  /**
   * Creates a JsArray expression that evaluates all the effects in parallel. If any effect fails, the entire expression
   * fails.
   *
   * @param effects the effects to be evaluated in parallel
   * @return a JsArrayExp for parallel evaluation
   */
  @SafeVarargs
  public static JsArrayExp par(final IO<JsValue>... effects) {
    var list = new ArrayList<IO<JsValue>>();
    for (var other : requireNonNull(effects)) {
      list.add(requireNonNull(other));
    }
    return new JsArrayExpPar(list,
                             null);
  }

  List<IO<JsValue>> debugJsArray(List<IO<JsValue>> exps,
                                 EventBuilder<JsArray> eventBuilder
                                ) {
    return IntStream.range(0,
                           exps.size())
                    .mapToObj(i -> DebuggerHelper.debugIO(exps.get(i),
                                                          String.format("%s[%s]",
                                                                        eventBuilder.exp,
                                                                        i
                                                                       ),
                                                          eventBuilder.context

                                                         )
                             )
                    .collect(Collectors.toList());
  }

  @Override
  public abstract JsArrayExp retryEach(final Predicate<? super Throwable> predicate,
                                       final RetryPolicy policy
                                      );

  @Override
  public abstract JsArrayExp debugEach(final EventBuilder<JsArray> messageBuilder
                                      );

  @Override
  public abstract JsArrayExp debugEach(final String context);

  @Override
  public JsArrayExp retryEach(RetryPolicy policy) {
    return retryEach(_ -> true,
                     policy);
  }

}
