package jio;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;


final class ListExpSeq<O> extends ListExp<O> {

    public ListExpSeq(final List<IO<O>> list,
                      final Function<ExpEvent, BiConsumer<List<O>, Throwable>> debugger
                     ) {
        super(list, debugger);
    }

    @Override
    public ListExp<O> append(final IO<O> val) {
        var xs = new ArrayList<>(list);
        xs.add(requireNonNull(val));
        return new ListExpSeq<>(xs, jfrPublisher);
    }

    @Override
    public ListExp<O> tail() {
        return new ListExpSeq<>(list.subList(1,
                                             list.size()
                                            ),
                                jfrPublisher
        );
    }


    @Override
    public ListExp<O> retryEach(final Predicate<? super Throwable> predicate,
                                final RetryPolicy policy
                               ) {
        requireNonNull(policy);
        requireNonNull(predicate);
        return new ListExpSeq<>(list.stream().map(it -> it.retry(predicate,
                                                                 policy
                                                                )
                                                 )
                                    .toList(),
                                jfrPublisher
        );
    }

    @Override
    CompletableFuture<List<O>> reduceExp() {
        var acc = CompletableFuture.<List<O>>completedFuture(new ArrayList<>());
        for (IO<O> val : list)
            acc = acc.thenCompose(l -> val.get()
                                          .thenApply(it -> {
                                              l.add(it);
                                              return l;
                                          })
                                 );

        return acc;

    }


    @Override
    public ListExp<O> debugEach(final EventBuilder<List<O>> eventBuilder) {
        return new ListExpSeq<>(DebuggerHelper.debugList(list,
                                                         eventBuilder.exp,
                                                         Objects.requireNonNull(eventBuilder).context
                                                        ),
                                getJFRPublisher(eventBuilder)
        );
    }


    @Override
    public ListExp<O> debugEach(final String context) {
        return debugEach(new EventBuilder<>(this.getClass().getSimpleName(), context));

    }
}
