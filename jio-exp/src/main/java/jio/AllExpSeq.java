package jio;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

final class AllExpSeq extends AllExp {

    public AllExpSeq(List<IO<Boolean>> exps,
                     Function<ExpEvent, BiConsumer<Boolean, Throwable>> logger) {
        super(logger, exps);
    }

    @Override
    public AllExp retryEach(final Predicate<Throwable> predicate,
                            final RetryPolicy policy
    ) {
        requireNonNull(predicate);
        requireNonNull(policy);

        return new AllExpSeq(exps.stream()
                .map(it -> it.retry(predicate,
                                policy
                        )
                )
                .toList(),
                jfrPublisher
        );
    }


    @Override
    CompletableFuture<Boolean> reduceExp() {
        return get(exps);
    }

    private CompletableFuture<Boolean> get(List<IO<Boolean>> exps) {

        return exps.size() == 1 ?
                exps.get(0).get() :
                exps.get(0)
                        .get()
                        .thenCompose(bool -> bool ?
                                get(exps.subList(1,
                                                exps.size()
                                        )
                                ) :
                                CompletableFuture.completedFuture(false)
                        );
    }

    @Override
    public AllExp debugEach(final EventBuilder<Boolean> builder) {
        Objects.requireNonNull(builder);
        return new AllExpSeq(
                LoggerHelper.debugConditions(
                        exps,
                        this.getClass().getSimpleName(),
                        builder.context),
                getJFRPublisher(builder)
        );
    }


    @Override
    public AllExp debugEach(final String context) {
        return debugEach(EventBuilder.<Boolean>ofExp(this.getClass().getSimpleName())
                .setContext(context));

    }
}
