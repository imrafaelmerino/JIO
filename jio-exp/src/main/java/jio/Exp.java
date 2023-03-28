package jio;



import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

sealed abstract class Exp<O> extends IO<O>
        permits AllExp, AnyExp, CondExp, IfElseExp, JsArrayExp, JsObjExp, ListExp, PairExp, SwitchExp, TripleExp {


    final Function<ExpEvent,BiConsumer<O, Throwable>> jfrPublisher;

    Exp(Function<ExpEvent,BiConsumer<O, Throwable>> jfrPublisher) {
        this.jfrPublisher = jfrPublisher;
    }

    Function<ExpEvent,BiConsumer<O, Throwable>> getJFRPublisher(final EventBuilder<O> builder) {
        return event -> (val, exc) -> {
            if (exc == null)
                builder.updateAndCommit(val, event);
            else
                builder.updateAndCommit(exc, event);
        };
    }

    @Override
    public CompletableFuture<O> get() {
        if(jfrPublisher ==null) return reduceExp();
        ExpEvent event = new ExpEvent();
        event.begin();
        return reduceExp().whenComplete(jfrPublisher.apply(event));
    }

    abstract CompletableFuture<O> reduceExp();


    abstract Exp<O> retryEach(final Predicate<Throwable> predicate,
                              final RetryPolicy policy
                             );


    abstract Exp<O> retryEach(final RetryPolicy policy);


    abstract Exp<O> debugEach(final EventBuilder<O> messageBuilder);


    abstract Exp<O> debugEach(final String context);


}
