package jio;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import jio.Result.Failure;
import jio.Result.Success;

/**
 * Abstract base class representing an expression that can be composed of different operands, which can be either
 * expressions or IO effects. Operands are used to build complex computations and workflows.
 *
 * @param <Output> the type of the result returned by this expression when it succeeds
 */
sealed abstract class Exp<Output> extends IO<Output>
    permits AllExp, AnyExp, CondExp, IfElseExp, JsArrayExp, JsObjExp, ListExp, PairExp, SwitchExp, TripleExp {

  final Function<EvalExpEvent, BiConsumer<Output, Throwable>> jfrPublisher;

  Exp(Function<EvalExpEvent, BiConsumer<Output, Throwable>> jfrPublisher) {
    this.jfrPublisher = jfrPublisher;
  }

  Function<EvalExpEvent, BiConsumer<Output, Throwable>> getJFRPublisher(final EventBuilder<Output> builder) {
    return event -> (val,
                     exc) -> {
      event.end();
      if (exc == null) {
        builder.commitSuccess(val,
                              event);
      } else {
        builder.commitFailure(exc,
                              event);
      }
    };
  }

  @Override
  public Result<Output> call() {
    if (jfrPublisher == null) {
      return reduceExp();
    }
    EvalExpEvent event = new EvalExpEvent();
    event.begin();
    Result<Output> result = reduceExp();
    switch (result) {
      case Success<Output>(Output output) -> jfrPublisher.apply(event)
                                                         .accept(output,
                                                                 null);
      case Failure<Output>(Throwable exception) -> jfrPublisher.apply(event)
                                                               .accept(null,
                                                                       exception);
    }
    return result;

  }

  abstract Result<Output> reduceExp();

  /**
   * Defines a strategy for retrying each operand of this expression when a specified condition is met, based on the
   * given retry policy.
   *
   * @param predicate the condition to evaluate whether to retry an operand
   * @param policy    the retry policy specifying the behavior for each retry attempt
   * @return a new expression with retry behavior applied to each operand
   */
  abstract Exp<Output> retryEach(final Predicate<? super Throwable> predicate,
                                 final RetryPolicy policy
                                );

  /**
   * Defines a strategy for retrying each operand of this expression based on the given retry policy.
   *
   * @param policy the retry policy specifying the behavior for each retry attempt
   * @return a new expression with retry behavior applied to each operand
   */
  abstract Exp<Output> retryEach(final RetryPolicy policy);

  /**
   * Attaches a debug mechanism to each operand of this expression, allowing you to monitor and log the execution of
   * each operand individually.
   *
   * @param messageBuilder the builder for creating debug events for each operand
   * @return a new expression with debug behavior applied to each operand
   */
  abstract Exp<Output> debugEach(final EventBuilder<Output> messageBuilder);

  /**
   * Attaches a debug mechanism to each operand of this expression, allowing you to monitor and log the execution of
   * each operand individually.
   *
   * @param context a descriptive context for the debug events of each operand
   * @return a new expression with debug behavior applied to each operand
   */
  abstract Exp<Output> debugEach(final String context);

}
