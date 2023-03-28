package jio;

import fun.tuple.Triple;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * Immutable expression that represents a tuple of three values
 *
 * @param <A> the type the first element will be reduced
 * @param <B> the type the second element will be reduced
 * @param <C> the type the third element will be reduced
 */
public abstract sealed class TripleExp<A, B, C> extends Exp<Triple<A, B, C>> permits TripleExpPar, TripleExpSeq {

    /**
     * returns the first effect of the triple
     *
     * @return first effect of the triple
     */
    public IO<A> first() {
        return _1;
    }

    /**
     * returns the second effect of the triple
     *
     * @return second effect of the triple
     */
    public IO<B> second() {
        return _2;
    }

    /**
     * returns the third effect of the triple
     *
     * @return third effect of the triple
     */
    public IO<C> third() {
        return _3;
    }


    final IO<A> _1;
    final IO<B> _2;
    final IO<C> _3;

    TripleExp(final IO<A> _1,
              final IO<B> _2,
              final IO<C> _3,
              final Function<ExpEvent,BiConsumer<Triple<A, B, C>, Throwable>> logger
             ) {
        super(logger);
        this._1 = _1;
        this._2 = _2;
        this._3 = _3;
    }

    /**
     * create a tuple of three effects that will be evaluated sequentially. If an effect fails,
     * the next ones are not evaluated and the whole expression fails.
     *
     * @param first  the first effect
     * @param second the second effect
     * @param third  the third effect
     * @param <A>    the type of the first effect result
     * @param <B>    the type of the second effect result
     * @param <C>    the type of the third effect result
     * @return a PairExp
     */
    public static <A, B, C> TripleExp<A, B, C> seq(final IO<A> first,
                                                   final IO<B> second,
                                                   final IO<C> third
                                                  ) {
        return new TripleExpSeq<>(requireNonNull(first),
                                  requireNonNull(second),
                                  requireNonNull(third),
                                  null
        );
    }

    /**
     * create a tuple of three effects that will be evaluated in parallel if they run on different threads.
     * The three effect are always evaluated, no matter if one fails.
     *
     * @param first  the first effect
     * @param second the second effect
     * @param third  the third effect
     * @param <A>    type of the first effect result
     * @param <B>    type of the second effect result
     * @param <C>    type of the third effect result
     * @return a pair expression evaluated in parallel
     */
    public static <A, B, C> TripleExp<A, B, C> par(final IO<A> first,
                                                   final IO<B> second,
                                                   final IO<C> third
                                                  ) {
        return new TripleExpPar<>(requireNonNull(first),
                                  requireNonNull(second),
                                  requireNonNull(third),
                                  null
        );
    }

    /**
     * Creates a new TripleExp expression where the given retry policy is applied recursively
     * to every subexpression when an exception is tested true against the specified predicate.
     *
     * @param predicate the predicate to test exceptions
     * @param policy    the retry policy
     * @return a new TripleExp
     */
    @Override
    public abstract TripleExp<A, B, C> retryEach(final Predicate<Throwable> predicate,
                                                 final RetryPolicy policy
                                                );

    /**
     * Creates a new TripleExp expression where the given retry policy is applied recursively
     * to every subexpression when an exception happens.
     *
     * @param policy the retry policy
     * @return a new TripleExp
     */
    @Override
    public TripleExp<A, B, C> retryEach(final RetryPolicy policy) {
        return retryEach(e -> true,
                         policy
                        );
    }

    /**
     * Creates a new TripleExp that will write to the given logger information about every
     * computation evaluated to reduce this expression (like {@link #debugEach(String)} does).
     * A final log message created with the specified messageBuilder is written after reducing
     * the whole expression
     *
     * @param messageBuilder the builder to create the log message from the result of the expression
     * @return a new TripleExp
     * @see #debugEach(String) debugEach
     */
    @Override
    public abstract TripleExp<A, B, C> debugEach(final EventBuilder<Triple<A, B, C>> messageBuilder
                                                );

    /**
     * Creates a new TripleExp that will print out on the console information about every
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
     *        TripleExp.seq(IO.succeed(1),
     *                      IO.succeed(2),
     *                      IO.succeed(3)
     *                     )
     *                 .debugEach("context")
     *                 .join()
     *
     *
     * 2023-02-04T14:50:31.598874+01:00 main DEBUGGER [context] 7333000 success TripleExpSeq[1]
     * 2023-02-04T14:50:31.606825+01:00 main DEBUGGER [context] 59458 success TripleExpSeq[2]
     * 2023-02-04T14:50:31.607016+01:00 main DEBUGGER [context] 39208 success TripleExpSeq[3]
     * 2023-02-04T14:50:31.607674+01:00 main DEBUGGER [context] 11635959 success TripleExpSeq (1, 2, 3)
     * }
     * </pre>
     *
     * @param context the context shared by all the subexpressions that will be printed out
     * @return a new TripleExp
     */
    @Override
    public abstract TripleExp<A, B, C> debugEach(final String context);
}
