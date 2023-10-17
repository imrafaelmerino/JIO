package jio;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Abstract base class representing an expression that can be composed of different operands, which can be either
 * expressions or IO effects. Operands are used to build complex computations and workflows.
 *
 * @param <O> the type of the result returned by this expression when it succeeds
 */
sealed abstract class Exp<O> extends IO<O>
        permits AllExp, AnyExp, CondExp, IfElseExp, JsArrayExp, JsObjExp, ListExp, PairExp, SwitchExp, TripleExp {


    final Function<ExpEvent, BiConsumer<O, Throwable>> jfrPublisher;

    Exp(Function<ExpEvent, BiConsumer<O, Throwable>> jfrPublisher) {
        this.jfrPublisher = jfrPublisher;
    }

    Function<ExpEvent, BiConsumer<O, Throwable>> getJFRPublisher(final EventBuilder<O> builder) {
        return event -> (val, exc) -> {
            if (exc == null)
                builder.updateAndCommit(val, event);
            else
                builder.updateAndCommit(exc, event);
        };
    }

    @Override
    public CompletableFuture<O> get() {
        if (jfrPublisher == null) return reduceExp();
        ExpEvent event = new ExpEvent();
        event.begin();
        return reduceExp().whenComplete(jfrPublisher.apply(event));
    }

    abstract CompletableFuture<O> reduceExp();


    /**
     * Defines a strategy for retrying each operand of this expression when a specified condition is met, based on the
     * given retry policy.
     *
     * @param predicate the condition to evaluate whether to retry an operand
     * @param policy    the retry policy specifying the behavior for each retry attempt
     * @return a new expression with retry behavior applied to each operand
     */
    abstract Exp<O> retryEach(final Predicate<? super Throwable> predicate,
                              final RetryPolicy policy
                             );


    /**
     * Defines a strategy for retrying each operand of this expression based on the given retry policy.
     *
     * @param policy the retry policy specifying the behavior for each retry attempt
     * @return a new expression with retry behavior applied to each operand
     */
    abstract Exp<O> retryEach(final RetryPolicy policy);


    /**
     * Attaches a debug mechanism to each operand of this expression, allowing you to monitor and log the execution of
     * each operand individually.
     *
     * @param messageBuilder the builder for creating debug events for each operand
     * @return a new expression with debug behavior applied to each operand
     */
    abstract Exp<O> debugEach(final EventBuilder<O> messageBuilder);


    /**
     * Attaches a debug mechanism to each operand of this expression, allowing you to monitor and log the execution of
     * each operand individually.
     *
     * @param context a descriptive context for the debug events of each operand
     * @return a new expression with debug behavior applied to each operand
     */
    abstract Exp<O> debugEach(final String context);


}
