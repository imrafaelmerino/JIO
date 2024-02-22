package jio;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

final class ListExpSeq<Elem> extends ListExp<Elem> {

  public ListExpSeq(final List<IO<Elem>> list,
                    final Function<EvalExpEvent, BiConsumer<List<Elem>, Throwable>> debugger
                   ) {
    super(list,
          debugger);
  }

  @Override
  public ListExp<Elem> append(final IO<Elem> val) {
    var xs = new ArrayList<>(list);
    xs.add(requireNonNull(val));
    return new ListExpSeq<>(xs,
                            jfrPublisher);
  }

  @Override
  public ListExp<Elem> tail() {
    return new ListExpSeq<>(list.subList(1,
                                         list.size()
                                        ),
                            jfrPublisher
    );
  }

  @Override
  public ListExp<Elem> retryEach(final Predicate<? super Throwable> predicate,
                                 final RetryPolicy policy
                                ) {
    requireNonNull(policy);
    requireNonNull(predicate);
    return new ListExpSeq<>(list.stream()
                                .map(it -> it.retry(predicate,
                                                    policy
                                                   )
                                    )
                                .toList(),
                            jfrPublisher
    );
  }

  @Override
  CompletableFuture<List<Elem>> reduceExp() {
    var acc = CompletableFuture.<List<Elem>>completedFuture(new ArrayList<>());
    for (IO<Elem> val : list) {
      acc = acc.thenCompose(l -> val.get()
                                    .thenApply(it -> {
                                      l.add(it);
                                      return l;
                                    })
                           );
    }

    return acc;

  }

  @Override
  public ListExp<Elem> debugEach(final EventBuilder<List<Elem>> eventBuilder) {
    return new ListExpSeq<>(DebuggerHelper.debugList(list,
                                                     eventBuilder.exp,
                                                     Objects.requireNonNull(eventBuilder).context
                                                    ),
                            getJFRPublisher(eventBuilder)
    );
  }

  @Override
  public ListExp<Elem> debugEach(final String context) {
    return debugEach(EventBuilder.of(this.getClass()
                                         .getSimpleName(),
                                     context));

  }
}
