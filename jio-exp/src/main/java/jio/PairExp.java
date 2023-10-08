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

    PairExp(Function<ExpEvent, BiConsumer<Pair<A, B>, Throwable>> logger, IO<A> _1, IO<B> _2) {
        super(logger);
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
    public abstract PairExp<A, B> retryEach(final Predicate<Throwable> predicate,
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

    /**
     * Discards the result of the first `IO` operation and returns the second `IO` operation.
     * Use this method when you are not interested in waiting for the results of the first operation.
     *
     * <p><b>Note:</b> If the actions associated with `_1` and `_2` are both computed by the caller thread,
     * the behaviour is different from the expected since we have to wait for _1 to be executed.
     *
     * @return An effect representing the second `IO` operation.
     */
    @SuppressWarnings("ReturnValueIgnored")
    public IO<B> discardFirst(){
         return IO.NULL().then(nill -> {
             _1.get();
             return _2;
         });

    }

    /**
     * Discards the result of the second `IO` operation and returns the first `IO` operation.
     * Use this method when you are not interested in waiting for the results of the second operation.
     *
     * <p><b>Note:</b> If the actions associated with `_1` and `_2` are both computed by the caller thread,
     * the behaviour is different from the expected since we have to wait for `_2` to be executed.
     *
     * @return An effect representing the second `IO` operation.
     */
    @SuppressWarnings("ReturnValueIgnored")
    public IO<A> discardSecond(){
        return IO.NULL().then(nill -> {
            _2.get();
            return _1;
        });
    }

}
