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

final class ListExpPar<Elem> extends ListExp<Elem> {

  public ListExpPar(final List<IO<Elem>> list,
                    final Function<EvalExpEvent, BiConsumer<List<Elem>, Throwable>> debugger
  ) {
    super(list,
          debugger);
  }

  @Override
  public ListExp<Elem> append(final IO<Elem> val) {
    var xs = new ArrayList<>(list);
    xs.add(requireNonNull(val));
    return new ListExpPar<>(xs,
                            jfrPublisher);
  }

  @Override
  public ListExp<Elem> tail() {
    return new ListExpPar<>(list.subList(1,
                                         list.size()),
                            jfrPublisher
    );
  }

  @Override
  public ListExp<Elem> retryEach(final Predicate<? super Throwable> predicate,
                                 final RetryPolicy policy
  ) {
    requireNonNull(policy);
    requireNonNull(predicate);

    return new ListExpPar<>(list.stream()
                                .map(it -> it.retry(predicate,
                                                    policy
                                )
                                )
                                .toList(),
                            jfrPublisher
    );
  }

  @Override
  @SuppressWarnings("unchecked")
  CompletableFuture<List<Elem>> reduceExp() {
    CompletableFuture<Elem>[] cfs = list.stream()
                                        .map(Supplier::get)
                                        .toArray(CompletableFuture[]::new);
    return CompletableFuture.allOf(cfs)
                            .thenApply(n -> Arrays.stream(cfs)
                                                  .map(CompletableFuture::join)
                                                  .collect(Collectors.toList())
                            );
  }

  @Override
  public ListExp<Elem> debugEach(final EventBuilder<List<Elem>> eventBuilder
  ) {
    Objects.requireNonNull(eventBuilder);
    return new ListExpPar<>(DebuggerHelper.debugList(list,
                                                     eventBuilder.exp,
                                                     eventBuilder.context
    ),
                            getJFRPublisher(eventBuilder)
    );
  }

  @Override
  public ListExp<Elem> debugEach(String context) {
    return debugEach(EventBuilder.of(this.getClass()
                                         .getSimpleName(),
                                     context));

  }
}
