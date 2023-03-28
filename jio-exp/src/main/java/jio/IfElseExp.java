package jio;

import java.util.concurrent.CompletableFuture;
import java.util.function.*;

import static java.util.Objects.requireNonNull;

/**
 * Represents an immutable expression made up of a predicate and two effects, the consequence and the alternative.
 * If the predicate succeed and is evaluated to true, the expression is reduced to the consequence, and if it's
 * evaluated to false, the expression is reduced to the alternative. The predicate can be either
 * a boolean or an effect.
 *
 * @param <O> the type of the value that the expression will be reduced to
 */
public final class IfElseExp<O> extends Exp<O> {

    private final IO<Boolean> predicate;
    private Supplier<IO<O>> consequence = IO::NULL;
    private Supplier<IO<O>> alternative = IO::NULL;

    /**
     * Creates an IfElseExp being the given boolean effect the predicate.
     *
     * @param predicate the predicate
     * @param <O>       the type that the expression will be evaluated to
     * @return an IfElseExp
     */
    public static <O> IfElseExp<O> predicate(final IO<Boolean> predicate) {
        return new IfElseExp<>(requireNonNull(predicate), null);
    }


    /**
     * Set the given lazy effect as the consequence of the IfElseExp. The
     * consequence is computed if the predicate is evaluated to true
     *
     * @param consequence the consequence effect
     * @return this IfElseExp with the specified consequence
     */
    public IfElseExp<O> consequence(final Supplier<IO<O>> consequence) {
        IfElseExp<O> exp = new IfElseExp<>(predicate, jfrPublisher);
        exp.alternative = alternative;
        exp.consequence = requireNonNull(consequence);
        return exp;
    }


    /**
     * Set the given lazy effect as the alternative of the IfElseExp. The
     * alternative is computed if the predicate is evaluated to false
     *
     * @param alternative the alternative effect
     * @return this IfElseExp with the specified alternative
     */
    public IfElseExp<O> alternative(final Supplier<IO<O>> alternative) {
        IfElseExp<O> exp = new IfElseExp<>(predicate, jfrPublisher);
        exp.consequence = consequence;
        exp.alternative = requireNonNull(alternative);
        return exp;
    }

    private IfElseExp(final IO<Boolean> predicate,
                      final Function<ExpEvent, BiConsumer<O, Throwable>> logger
                     ) {
        super(logger);
        this.predicate = predicate;
    }

    /**
     * Creates a new IfElseExp expression where the given retry policy is applied recursively
     * to every subexpression when an exception is tested true against the specified predicate.
     *
     * @param predicate the predicate to test exceptions
     * @param policy    the retry policy
     * @return a new IfElseExp
     */
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

    /**
     * Creates a new IfElseExp that will write to the given logger information about every
     * computation evaluated to reduce this expression (like {@link #debugEach(String)} does).
     * A final log message created with the specified logBuilder is written after reducing
     * the whole expression
     *
     * @param logBuilder the builder to create the log message from the result of the expression
     * @return a new IfElseExp
     * @see #debugEach(String) debugEach
     */
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

    /**
     * Creates a new IfElseExp expression where the given retry policy is applied recursively
     * to every subexpression when an exception happens.
     *
     * @param policy the retry policy
     * @return a new IfElseExp
     */
    @Override
    public IfElseExp<O> retryEach(final RetryPolicy policy) {
        return retryEach(e -> true, policy);
    }

    /**
     * Creates a new IfElseExp that will print out on the console information about every
     * computation evaluated to reduce this expression. The given context will be associated
     * to every subexpression and printed out to correlate all the evaluations (contextual
     * logging).
     * <p>
     * The line format is the following:
     * <p>
     * datetime thread logger [context] elapsed_time success|exception expression|subexpression result?
     * <p>
     * Find bellow an example:
     *
     * <pre>
     * {@code
     *
     *          IfElseExp.predicate(IO.FALSE)
     *                   .consequence(() -> IO.succeed("b"))
     *                   .alternative(() -> IO.succeed("a"))
     *                   .debugEach("context")
     *                   .join()
     *
     *
     * }
     * </pre>
     * <p>
     * 2023-02-04T17:52:42.437946+01:00 main DEBUGGER [context] 7748042 success IfElseExp-predicate false
     * 2023-02-04T17:52:43.451851+01:00 pool-2-thread-1 DEBUGGER [context] 1005255875 success IfElseExp-alternative
     * 2023-02-04T17:52:43.45223+01:00 pool-2-thread-1 DEBUGGER [context] 1017262500 success IfElseExp b
     *
     * @param context the context shared by all the subexpressions that will be printed out
     * @return a new IfElseExp
     */
    @Override
    public IfElseExp<O> debugEach(final String context) {
        return debugEach(EventBuilder.<O>ofExp(this.getClass().getSimpleName()).setContext(context));
    }


}
