package jio;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;


final class AnyExpPar extends AnyExp {


    public AnyExpPar(final List<IO<Boolean>> exps,
                     final Function<ExpEvent, BiConsumer<Boolean, Throwable>> logger
                    ) {
        super(logger, exps);
    }

    @Override
    public AnyExp retryEach(final Predicate<Throwable> predicate,
                            final RetryPolicy policy
                           ) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(policy);
        return new AnyExpPar(exps.stream()
                                 .map(it -> it.retry(predicate,
                                                     policy
                                                    ))
                                 .toList(),
                             jfrPublisher
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    CompletableFuture<Boolean> reduceExp() {
        CompletableFuture<Boolean>[] cfs = exps.stream()
                                               .map(Supplier::get)
                                               .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(cfs)
                                .thenApply(l -> Arrays.stream(cfs)
                                                      .anyMatch(CompletableFuture::join)
                                          );
    }

    @Override
    public AnyExp debugEach(
            final EventBuilder<Boolean> messageBuilder
                           ) {
        Objects.requireNonNull(messageBuilder);
        return new AnyExpPar(LoggerHelper.debugConditions(
                exps,
                this.getClass().getSimpleName(),
                messageBuilder.context
                                                         ),
                             getJFRPublisher(
                                     messageBuilder
                                            )
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
