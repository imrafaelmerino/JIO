package jio;


import java.util.function.Function;

import static java.util.Objects.requireNonNull;

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
 * @param <O> the type of the result of a computation in case of success
 * @see IO#debug(EventBuilder)
 * @see Exp#debugEach(EventBuilder)
 * @see Exp#debugEach(String)
 */
public final class EventBuilder<O> {

    final String exp;
    final String context;
    Function<O, String> successValue = val -> val == null ? "null" : val.toString();
    Function<Throwable, String> failureMessage =
            e -> String.format("%s:%s",
                               e.getClass().getName(),
                               e.getMessage()
                              );

    private EventBuilder(final String exp,
                         final String context
                        ) {
        this.exp = requireNonNull(exp);
        if (exp.isBlank() || exp.isEmpty()) throw new IllegalArgumentException("exp must be a legible string");
        this.context = requireNonNull(context);
    }

    /**
     * Creates a new instance of {@code EventBuilder} with the specified expression and context.
     *
     * @param exp     the expression for the event
     * @param context the context for the event
     * @param <O>     the type of the result of a computation in case of success
     * @return a new {@code EventBuilder} instance
     */
    public static <O> EventBuilder<O> of(final String exp,
                                         final String context
                                        ) {
        return new EventBuilder<>(exp, context);
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
        return EventBuilder.of(exp, "");
    }

    private static Throwable findUltimateCause(Throwable exception) {
        Throwable ultimateCause = exception;

        // Iterate through the exception chain until the ultimate cause is found
        while (ultimateCause.getCause() != null) {
            ultimateCause = ultimateCause.getCause();
        }

        return ultimateCause;
    }

    /**
     * Set the function that takes the result of the expression and produces the event value. By default, the value of
     * the event is <code>result.toString()</code>.
     *
     * @param successValue a function that takes the result of the expression and produces the event value
     * @return this event builder
     */
    public EventBuilder<O> withSuccessOutput(final Function<O, String> successValue) {
        this.successValue = requireNonNull(successValue);
        return this;
    }

    /**
     * Set the function that produces the event failure message from the exception produced by an expression. By
     * default, the event failure message is <code>exception.getClass().getName:exception.getMessage</code>.
     *
     * @param failureMessage a function that produces the event failure message from the exception
     * @return this event builder
     */
    public EventBuilder<O> withFailureOutput(final Function<Throwable, String> failureMessage) {
        this.failureMessage = requireNonNull(failureMessage);
        return this;
    }

    ExpEvent updateEvent(final O o, final ExpEvent event) {
        event.result = ExpEvent.RESULT.SUCCESS.name();
        event.value = successValue.apply(o);
        event.context = context;
        event.expression = exp;
        return event;
    }

    ExpEvent updateEvent(final Throwable exc, final ExpEvent event) {
        var cause = findUltimateCause(exc);
        event.result = ExpEvent.RESULT.FAILURE.name();
        event.context = context;
        event.expression = exp;
        event.exception = failureMessage.apply(cause);
        return event;
    }

    void updateAndCommit(final O o, final ExpEvent event) {
        updateEvent(o, event).commit();
    }

    void updateAndCommit(final Throwable exc, final ExpEvent event) {
        updateEvent(exc, event).commit();
    }
}
