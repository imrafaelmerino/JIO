package jio;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Represents an expression that combines a predicate effect with two alternative effect suppliers, one for the
 * consequence and another for the alternative branch. If the predicate evaluates to true, the expression is reduced to
 * the consequence effect; otherwise, it is reduced to the alternative effect.
 *
 * @param <O> the type of the result that the expression will produce
 */
public final class IfElseExp<O> extends Exp<O> {

    private final IO<Boolean> predicate;
    private Supplier<IO<O>> consequence = IO::NULL;
    private Supplier<IO<O>> alternative = IO::NULL;

    private IfElseExp(final IO<Boolean> predicate,
                      final Function<ExpEvent, BiConsumer<O, Throwable>> logger
                     ) {
        super(logger);
        this.predicate = predicate;
    }

    /**
     * Creates an IfElseExp with the given boolean effect as the predicate.
     *
     * @param predicate the predicate effect
     * @param <O>       the type that the expression will produce
     * @return an IfElseExp instance
     */
    public static <O> IfElseExp<O> predicate(final IO<Boolean> predicate) {
        return new IfElseExp<>(requireNonNull(predicate), null);
    }

    /**
     * Sets the consequence effect to be computed if the predicate evaluates to true.
     *
     * @param consequence the consequence effect
     * @return this IfElseExp instance with the specified consequence effect
     */
    public IfElseExp<O> consequence(final Supplier<IO<O>> consequence) {
        IfElseExp<O> exp = new IfElseExp<>(predicate, jfrPublisher);
        exp.alternative = alternative;
        exp.consequence = requireNonNull(consequence);
        return exp;
    }

    /**
     * Sets the alternative effect to be computed if the predicate evaluates to false.
     *
     * @param alternative the alternative effect
     * @return this IfElseExp instance with the specified alternative effect
     */
    public IfElseExp<O> alternative(final Supplier<IO<O>> alternative) {
        IfElseExp<O> exp = new IfElseExp<>(predicate, jfrPublisher);
        exp.consequence = consequence;
        exp.alternative = requireNonNull(alternative);
        return exp;
    }


    @Override
    public IfElseExp<O> retryEach(final Predicate<Throwable> predicate,
                                  final RetryPolicy policy
                                 ) {
        return new IfElseExp<O>(this.predicate.retry(requireNonNull(predicate),
                                                     requireNonNull(policy)
                                                    ),
                                jfrPublisher
        )
                .consequence(() -> consequence.get()
                                              .retry(predicate, policy)
                            )
                .alternative(() -> alternative.get()
                                              .retry(predicate, policy)
                            );
    }


    @Override
    CompletableFuture<O> reduceExp() {

        return predicate.get()
                        .thenCompose(bool -> bool ?
                                             consequence.get()
                                                        .get() :
                                             alternative.get()
                                                        .get()
                                    );
    }


    @Override
    public IfElseExp<O> debugEach(
            final EventBuilder<O> logBuilder
                                 ) {
        return new IfElseExp<O>(LoggerHelper.debugIO(predicate,
                                                     String.format("%s-predicate",
                                                                   this.getClass().getSimpleName()
                                                                  ),

                                                     logBuilder.context
                                                    ),
                                getJFRPublisher(logBuilder)
        )
                .consequence(() -> LoggerHelper.debugIO(consequence.get(),
                                                        String.format("%s-consequence",
                                                                      this.getClass().getSimpleName()
                                                                     ),
                                                        logBuilder.context
                                                       )

                            )
                .alternative(() -> LoggerHelper.debugIO(alternative.get(),
                                                        String.format("%s-alternative",
                                                                      this.getClass().getSimpleName()
                                                                     ),
                                                        logBuilder.context
                                                       )

                            );
    }


    @Override
    public IfElseExp<O> retryEach(final RetryPolicy policy) {
        return retryEach(e -> true, policy);
    }

    @Override
    public IfElseExp<O> debugEach(final String context) {
        return debugEach(EventBuilder.<O>ofExp(this.getClass().getSimpleName()).setContext(context));
    }


}
