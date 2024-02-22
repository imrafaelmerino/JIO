package jio;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;

/**
 * Represents a builder to create JFR {@link jdk.jfr.consumer.RecordedEvent} from computations performed by the JIO API.
 * Some event fields can be customized. The event message of a successful computation is by default the string
 * representation of the result and can be customized with the method {@link #withSuccessOutput(Function)}. The failure
 * message of a fail computation is by default <code>exception.getClass().getName():exception.getMessage()</code> and
 * can be customized with the method {@link #withFailureOutput(Function)}.
 * <p>
 * Expressions made up of different subexpressions generate different JFR events that can be correlated with a context
 * specified with the constructor {@link EventBuilder#EventBuilder(String, String)}.
 *
 * @param <Output> the type of the result of a computation in case of success
 * @see IO#debug(EventBuilder)
 * @see Exp#debugEach(EventBuilder)
 * @see Exp#debugEach(String)
 */
public final class EventBuilder<Output> {

  final String exp;
  final String context;
  Function<Output, String> successValue = val -> val == null ? "null" : val.toString();
  Function<Throwable, String> failureMessage = e -> ExceptionFun.findUltimateCause(e)
                                                                .toString();

  private EventBuilder(final String exp,
                       final String context
                      ) {
    this.exp = requireNonNull(exp);
    if (exp.isBlank() || exp.isEmpty()) {
      throw new IllegalArgumentException("exp must be a legible string");
    }
    this.context = requireNonNull(context);
  }

  /**
   * Creates a new instance of {@code EventBuilder} with the specified expression and context.
   *
   * @param exp      the expression for the event
   * @param context  the context for the event
   * @param <Output> the type of the result of a computation in case of success
   * @return a new {@code EventBuilder} instance
   */
  public static <Output> EventBuilder<Output> of(final String exp,
                                                 final String context
                                                ) {
    return new EventBuilder<>(exp,
                              context);
  }

  /**
   * Creates a new instance of {@code EventBuilder} with the specified expression and an empty context.
   *
   * @param exp the expression for the event
   * @param <O> the type of the result of a computation in case of success
   * @return a new {@code EventBuilder} instance
   */
  public static <O> EventBuilder<O> of(final String exp
                                      ) {
    return EventBuilder.of(exp,
                           "");
  }

  /**
   * Set the function that takes the result of the expression and produces the event output. By default, the output of the
   * event is <code>result.toString()</code>.
   *
   * @param successValue a function that takes the result of the expression and produces the event output
   * @return this event builder
   */
  public EventBuilder<Output> withSuccessOutput(final Function<Output, String> successValue) {
    this.successValue = requireNonNull(successValue);
    return this;
  }

  /**
   * Set the function that produces the event failure message from the exception produced by an expression. By default,
   * the event failure message is
   * <code>exception.getClass().getName:exception.getMessage</code>.
   *
   * @param failureMessage a function that produces the event failure message from the exception
   * @return this event builder
   */
  public EventBuilder<Output> withFailureOutput(final Function<Throwable, String> failureMessage) {
    this.failureMessage = requireNonNull(failureMessage);
    return this;
  }

  EvalExpEvent updateSuccessfulEvent(final Output output,
                                     final EvalExpEvent event) {
    event.result = EvalExpEvent.RESULT.SUCCESS.name();
    event.value = successValue.apply(output);
    event.context = context;
    event.expression = exp;
    return event;
  }

  EvalExpEvent updateFailureEvent(final Throwable exc,
                                  final EvalExpEvent event) {
    var cause = ExceptionFun.findUltimateCause(exc);
    event.result = EvalExpEvent.RESULT.FAILURE.name();
    event.context = context;
    event.expression = exp;
    event.exception = failureMessage.apply(cause);
    return event;
  }

  void commitSuccess(final Output output,
                     final EvalExpEvent event) {
    if (event.shouldCommit()) {
      updateSuccessfulEvent(output,
                            event).commit();
    }
  }

  void commitFailure(final Throwable exc,
                     final EvalExpEvent event) {
    if (event.shouldCommit()) {
      updateFailureEvent(exc,
                         event).commit();
    }
  }
}
