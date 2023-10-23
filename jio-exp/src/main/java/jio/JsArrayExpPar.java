package jio;

import jsonvalues.JsArray;
import jsonvalues.JsValue;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

final class JsArrayExpPar extends JsArrayExp {


    public JsArrayExpPar(List<IO<? extends JsValue>> list,
                         Function<ExpEvent, BiConsumer<JsArray, Throwable>> debugger
                        ) {
        super(list, debugger);
    }

    /**
     * it triggers the execution of all the completable futures, combining the results into a JsArray
     *
     * @return a CompletableFuture of a json array
     */
    @Override
    CompletableFuture<JsArray> reduceExp() {
        var result = CompletableFuture.completedFuture(JsArray.empty());

        for (final IO<? extends JsValue> future : list)
            result = result.thenCombine(future.get(),
                                        JsArray::append
                                       );
        return result;
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
        return this.debugEach(EventBuilder.of(this.getClass().getSimpleName(),
                                                 context));

    }
}
