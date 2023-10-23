package jio;

import fun.tuple.Pair;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * Represents an expression that is reduced to a pair. Their elements can be evaluated either in parallel or
 * sequentially. In both cases, if one fails, the whole expression fails.
 * <p>
 * You can create PairExp expressions using the 'seq' method to evaluate effects sequentially, or using the 'par' method
 * to evaluate effects in parallel. If one effect fails, the entire expression fails.
 *
 * @param <A> the type of the first computation
 * @param <B> the type of the second computation
 */
public abstract sealed class PairExp<A, B> extends Exp<Pair<A, B>> permits PairExpSeq, PairExpPar {

    final IO<A> _1;
    final IO<B> _2;

    PairExp(Function<ExpEvent, BiConsumer<Pair<A, B>, Throwable>> debugger, IO<A> _1, IO<B> _2) {
        super(debugger);
        this._1 = _1;
        this._2 = _2;
    }

    /**
     * create a tuple of two effects that will be evaluated sequentially. If the first one fails, the second one is not
     * evaluated and the whole expression fails.
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
     * create a tuple of two effects that will be evaluated in parallel if they run on different threads. The two effect
     * are always evaluated, no matter if the first one fails.
     *
     * @param first  first effect of the pair
     * @param second second effect of the pair
     * @param <A>    type of the first effect result
     * @param <B>    type of the second effect result
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
     * Returns the first element of the pair.
     *
     * @return the first element of the pair
     */
    public IO<A> first() {
        return _1;
    }

    /**
     * Returns the second element of the pair.
     *
     * @return the second element of the pair
     */
    public IO<B> second() {
        return _2;
    }


    @Override
    public abstract PairExp<A, B> retryEach(final Predicate<? super Throwable> predicate,
                                            final RetryPolicy policy
                                           );


    @Override
    public PairExp<A, B> retryEach(final RetryPolicy policy) {
        return retryEach(e -> true, policy);
    }


    @Override
    public abstract PairExp<A, B> debugEach(final EventBuilder<Pair<A, B>> messageBuilder
                                           );


    @Override
    public abstract PairExp<A, B> debugEach(final String context);



}
