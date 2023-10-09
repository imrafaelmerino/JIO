package jio;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;


final class ListExpPar<O> extends ListExp<O> {

    public ListExpPar(final List<IO<O>> list,
                      final Function<ExpEvent, BiConsumer<List<O>, Throwable>> debugger
                     ) {
        super(list, debugger);
    }

    @Override
    public ListExp<O> append(final IO<O> val) {
        var xs = new ArrayList<>(list);
        xs.add(requireNonNull(val));
        return new ListExpPar<>(xs, jfrPublisher);
    }

    @Override
    public ListExp<O> tail() {
        return new ListExpPar<>(list.subList(1, list.size()),
                                jfrPublisher
        );
    }


    @Override
    public ListExp<O> retryEach(final Predicate<Throwable> predicate,
                                final RetryPolicy policy
                               ) {
        requireNonNull(policy);
        requireNonNull(predicate);

        return new ListExpPar<>(list.stream().map(it -> it.retry(predicate,
                                                                 policy
                                                                )
                                                 )
                                    .toList(),
                                jfrPublisher
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    CompletableFuture<List<O>> reduceExp() {
        CompletableFuture<O>[] cfs = list.stream()
                                         .map(Supplier::get)
                                         .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(cfs)
                                .thenApply(n -> Arrays.stream(cfs)
                                                      .map(CompletableFuture::join)
                                                      .collect(Collectors.toList())
                                          );
    }

    @Override
    public ListExp<O> debugEach(final EventBuilder<List<O>> eventBuilder
                               ) {
        Objects.requireNonNull(eventBuilder);
        return new ListExpPar<>(LoggerHelper.debugList(list,
                                                       eventBuilder.exp,
                                                       eventBuilder.context
                                                      ),
                                getJFRPublisher(eventBuilder)
        );
    }


    @Override
    public ListExp<O> debugEach(String context) {
        return debugEach(
                new EventBuilder<>(this.getClass().getSimpleName(), context));

    }
}
