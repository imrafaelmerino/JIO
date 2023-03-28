package jio;

import fun.tuple.Pair;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * Immutable expression that represents a tuple of two computations. Their elements can be evaluated either in
 * parallel or sequentially. In both cases, if one fails, the whole expression fails.
 *
 * @param <A> the type of the first computation
 * @param <B> the type of the second computation
 */
public abstract sealed class PairExp<A, B> extends Exp<Pair<A, B>> permits PairExpSeq, PairExpPar {

    final IO<A> _1;
    final IO<B> _2;

    PairExp(Function<ExpEvent,BiConsumer<Pair<A,B>, Throwable>> logger, IO<A> _1, IO<B> _2) {
        super(logger);
        this._1 = _1;
        this._2 = _2;
    }

    /**
     * Returns the first element of the pair.
     * @return the first element of the pair
     */
    public IO<A> first() {
        return _1;
    }

    /**
     * Returns the second element of the pair.
     * @return the second element of the pair
     */
    public IO<B> second() {
        return _2;
    }

    /**
     * create a tuple of two effect that will be evaluated sequentially. If the first one fails,
     * the second one is not evaluated and the whole expression fails.
     *
     * @param first  the first effect
     * @param second the second effect
     * @param <A>    the type of the first effect result
     * @param <B>    the type of the second effect result
     * @return a PairExp evaluated sequentially
     */
    public static <A, B> PairExp<A, B> seq(final IO<A> first,
                                           final IO<B> second
                                          ) {
        return new PairExpSeq<>(requireNonNull(first),
                                requireNonNull(second),
                                null
        );
    }

    /**
     * create a tuple of two effect that will be evaluated in parallel if they run on different threads.
     * The two effect are always evaluated, no matter if the first one fails.
     *
     * @param first first effect of the pair
     * @param second second effect of the pair
     * @param <A> type of the first effect result
     * @param <B> type of the second effect result
     * @return a pair expression evaluated in parallel
     */
    public static <A, B> PairExp<A, B> par(final IO<A> first,
                                           final IO<B> second
                                          ) {
        return new PairExpPar<>(requireNonNull(first),
                                requireNonNull(second),
                                null
        );
    }

    /**
     * Creates a new PairExp expression where the given retry policy is applied recursively
     * to every subexpression when an exception is tested true against the specified predicate.
     *
     * @param predicate the predicate to test exceptions
     * @param policy    the retry policy
     * @return a new PairExp
     */
    @Override
    public abstract PairExp<A, B> retryEach(final Predicate<Throwable> predicate,
                                            final RetryPolicy policy
                                           );

    /**
     * Creates a new PairExp expression where the given retry policy is applied recursively
     * to every subexpression when an exception happens.
     *
     * @param policy the retry policy
     * @return a new PairExp
     */
    @Override
    public PairExp<A, B> retryEach(final RetryPolicy policy) {
        return retryEach(e -> true, policy);
    }

    /**
     * Creates a new PairExp that will write to the given logger information about every
     * computation evaluated to reduce this expression (like {@link #debugEach(String)} does).
     * A final log message created with the specified messageBuilder is written after reducing
     * the whole expression
     *
     * @param messageBuilder the builder to create the log message from the result of the expression
     * @return a new PairExp
     * @see #debugEach(String) debugEach
     */
    @Override
    public abstract PairExp<A, B> debugEach(final EventBuilder<Pair<A, B>> messageBuilder
                                           );

    /**
     * Creates a new PairExp that will print out on the console information about every
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
     *        PairExp.par(IO.succeed(1),
     *                    IO.succeed(2)
     *                   )
     *               .debugEach("context")
     *               .join()
     *
    2023-02-04T15:50:46.763583+01:00 pool-1-thread-1 DEBUGGER [context] 1019785125 success PairExpPar[1]
    2023-02-04T15:50:46.76361+01:00 pool-2-thread-1 DEBUGGER [context] 1017817417 success PairExpPar[2]
    2023-02-04T15:50:46.763878+01:00 pool-2-thread-1 DEBUGGER [context] 1020096750 success PairExpPar (a, b)
     * }
     * </pre>
     *
     * @param context the context shared by all the subexpressions that will be printed out
     * @return a new PairExp
     */
    @Override
    public abstract PairExp<A, B> debugEach(final String context);

}
