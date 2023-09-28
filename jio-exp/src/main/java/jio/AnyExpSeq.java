package jio;


import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

final class AnyExpSeq extends AnyExp {

    public AnyExpSeq(final List<IO<Boolean>> exps,
                     final Function<ExpEvent, BiConsumer<Boolean, Throwable>> logger
                    ) {
        super(logger, exps);
    }


    @Override
    public AnyExp retryEach(final Predicate<Throwable> predicate,
                            final RetryPolicy policy
                           ) {
        requireNonNull(predicate);
        requireNonNull(policy);
        return new AnyExpSeq(exps.stream()
                                 .map(it -> it.retry(predicate,
                                                     policy
                                                    ))
                                 .toList(),
                             jfrPublisher
        );
    }

    @Override
    CompletableFuture<Boolean> reduceExp() {
        return get(exps);
    }

    private CompletableFuture<Boolean> get(final List<IO<Boolean>> exps) {

        return exps.size() == 1 ?
                exps.get(0).get() :
                exps.get(0)
                    .get()
                    .thenCompose(bool -> bool ?
                            CompletableFuture.completedFuture(true) :
                            get(exps.subList(1, exps.size())));
    }

    @Override
    public AnyExp debugEach(final EventBuilder<Boolean> messageBuilder) {
        Objects.requireNonNull(messageBuilder);
        return new AnyExpSeq(LoggerHelper.debugConditions(exps,
                                                          this.getClass().getSimpleName(),
                                                          messageBuilder.context
                                                         ),
                             getJFRPublisher(messageBuilder)
        );
    }


    @Override
    public AnyExp debugEach(final String context) {
        return debugEach(
                EventBuilder.<Boolean>ofExp(this.getClass().getSimpleName())
                            .setContext(context)
                        );

    }
}
