package jio;

import fun.tuple.Triple;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * Represents an expression that is reduced to a triple. Their elements can be evaluated either in parallel or
 * sequentially. In both cases, if one fails, the whole expression fails.
 * <p>
 * You can create TripleExp expressions using the 'seq' method to evaluate effects sequentially, or using the 'par'
 * method to evaluate effects in parallel. If one effect fails, the entire expression fails.
 *
 * @param <A> the type of the first computation
 * @param <B> the type of the second computation
 */
public abstract sealed class TripleExp<A, B, C> extends Exp<Triple<A, B, C>> permits TripleExpPar, TripleExpSeq {

    final IO<A> _1;
    final IO<B> _2;
    final IO<C> _3;


    TripleExp(final IO<A> _1,
              final IO<B> _2,
              final IO<C> _3,
              final Function<ExpEvent, BiConsumer<Triple<A, B, C>, Throwable>> debugger
             ) {
        super(debugger);
        this._1 = _1;
        this._2 = _2;
        this._3 = _3;
    }

    /**
     * create a tuple of three effects that will be evaluated sequentially. If an effect fails, the next ones are not
     * evaluated and the whole expression fails.
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
     * create a tuple of three effects that will be evaluated in parallel if they run on different threads. The three
     * effect are always evaluated, no matter if one fails.
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

    @Override
    public abstract TripleExp<A, B, C> retryEach(final Predicate<? super Throwable> predicate,
                                                 final RetryPolicy policy
                                                );

    @Override
    public TripleExp<A, B, C> retryEach(final RetryPolicy policy) {
        return retryEach(e -> true,
                         policy
                        );
    }


    @Override
    public abstract TripleExp<A, B, C> debugEach(final EventBuilder<Triple<A, B, C>> messageBuilder
                                                );

    @Override
    public abstract TripleExp<A, B, C> debugEach(final String context);

}
