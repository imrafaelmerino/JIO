package jio;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Represents an expression made up of different test-effect branches and a default effect. Each branch consists of a
 * predicate (concretely a boolean effect) and an associated effect. The expression is reduced to the effect of the
 * first predicate that succeeds and is evaluated to true. If no predicate is evaluated to true, then the expression is
 * reduced to the default effect.
 * <p>
 * Predicates can be evaluated either in parallel with the static factory method {@code CondExp.par} or sequentially
 * with {@code CondExp.seq}.
 *
 * @param <O> the type of the computation returned by this expression.
 */
public abstract sealed class CondExp<O> extends Exp<O> permits CondExpPar, CondExpSeq {

    CondExp(Function<ExpEvent, BiConsumer<O, Throwable>> logger) {
        super(logger);
    }

    /**
     * It creates a Cond expression which predicates are computed in parallel. Once all the predicates succeed, the
     * expression is reduced to the effect of the fist one that is true, following the order they are passed in the
     * constructor.
     * <p>
     * Not like expressions created with the <code>seq</code> constructor, <strong>all the predicates must terminate
     * before the whole expression is reduced to the selected effect, no matter if one fails or is evaluated to
     * true</strong>. If one predicate terminates with an exception, the whole expression fails.
     *
     * @param predicate1 the first predicate
     * @param effect1    the effect associated to the first predicate
     * @param predicate2 the second predicate
     * @param effect2    the effect associated to the second predicate
     * @param otherwise  the default effect, computed if all the predicates are false
     * @param <O>        the type of the computation result
     * @return a Cond expression
     */
    public static <O> CondExp<O> par(final IO<Boolean> predicate1,
                                     final Supplier<IO<O>> effect1,
                                     final IO<Boolean> predicate2,
                                     final Supplier<IO<O>> effect2,
                                     final Supplier<IO<O>> otherwise
                                    ) {

        return new CondExpPar<>(List.of(requireNonNull(predicate1),
                                        requireNonNull(predicate2)
                                       ),
                                List.of(requireNonNull(effect1),
                                        requireNonNull(effect2)
                                       ),
                                requireNonNull(otherwise),
                                null
        );

    }


    /**
     * It creates a Cond expression which predicates are computed in parallel. Once all the predicates succeed, the
     * expression is reduced to the effect of the fist one that is true, following the order they are passed in the
     * constructor.
     * <p>
     * Not like expressions created with the <code>seq</code> constructor, <strong>all the predicates must terminate
     * before the whole expression is reduced to the selected effect, no matter if one fails or is evaluated to
     * true</strong>. If one predicate terminates with an exception, the whole expression fails.
     *
     * @param predicate1 the first predicate
     * @param effect1    the effect associated to the first predicate
     * @param predicate2 the second predicate
     * @param effect2    the effect associated to the second predicate
     * @param predicate3 the third predicate
     * @param effect3    the effect associated to the third predicate
     * @param otherwise  the default effect, computed if all the predicates are false
     * @param <O>        the type of the computation result
     * @return a Cond expression
     */
    public static <O> CondExp<O> par(final IO<Boolean> predicate1,
                                     final Supplier<IO<O>> effect1,
                                     final IO<Boolean> predicate2,
                                     final Supplier<IO<O>> effect2,
                                     final IO<Boolean> predicate3,
                                     final Supplier<IO<O>> effect3,
                                     final Supplier<IO<O>> otherwise
                                    ) {

        return new CondExpPar<>(List.of(requireNonNull(predicate1),
                                        requireNonNull(predicate2),
                                        requireNonNull(predicate3)
                                       ),
                                List.of(requireNonNull(effect1),
                                        requireNonNull(effect2),
                                        requireNonNull(effect3)
                                       ),
                                requireNonNull(otherwise),
                                null
        );

    }


    /**
     * It creates a Cond expression which predicates are computed in parallel. Once all the predicates succeed, the
     * expression is reduced to the effect of the fist one that is true, following the order they are passed in the
     * constructor.
     * <p>
     * Not like expressions created with the <code>seq</code> constructor, <strong>all the predicates must terminate
     * before the whole expression is reduced to the selected effect, no matter if one fails or is evaluated to
     * true</strong>. If one predicate terminates with an exception, the whole expression fails.
     *
     * @param predicate1 the first predicate
     * @param effect1    the effect associated to the first predicate
     * @param predicate2 the second predicate
     * @param effect2    the effect associated to the second predicate
     * @param predicate3 the third predicate
     * @param effect3    the effect associated to the third predicate
     * @param predicate4 the forth predicate
     * @param effect4    the effect associated to the forth predicate
     * @param otherwise  the default effect, computed if all the predicates are false
     * @param <O>        the type of the computation result
     * @return a Cond expression
     */
    public static <O> CondExp<O> par(final IO<Boolean> predicate1,
                                     final Supplier<IO<O>> effect1,
                                     final IO<Boolean> predicate2,
                                     final Supplier<IO<O>> effect2,
                                     final IO<Boolean> predicate3,
                                     final Supplier<IO<O>> effect3,
                                     final IO<Boolean> predicate4,
                                     final Supplier<IO<O>> effect4,
                                     final Supplier<IO<O>> otherwise
                                    ) {

        return new CondExpPar<>(List.of(requireNonNull(predicate1),
                                        requireNonNull(predicate2),
                                        requireNonNull(predicate3),
                                        requireNonNull(predicate4)
                                       ),
                                List.of(requireNonNull(effect1),
                                        requireNonNull(effect2),
                                        requireNonNull(effect3),
                                        requireNonNull(effect4)
                                       ),
                                requireNonNull(otherwise),
                                null
        );
    }

    /**
     * It creates a Cond expression which predicates are computed in parallel. Once all the predicates succeed, the
     * expression is reduced to the effect of the fist one that is true, following the order they are passed in the
     * constructor.
     * <p>
     * Not like expressions created with the <code>seq</code> constructor, <strong>all the predicates must terminate
     * before the whole expression is reduced to the selected effect, no matter if one fails or is evaluated to
     * true</strong>. If one predicate terminates with an exception, the whole expression fails.
     *
     * @param predicate1 the first predicate
     * @param effect1    the effect associated to the first predicate
     * @param predicate2 the second predicate
     * @param effect2    the effect associated to the second predicate
     * @param predicate3 the third predicate
     * @param effect3    the effect associated to the third predicate
     * @param predicate4 the forth predicate
     * @param effect4    the effect associated to the forth predicate
     * @param predicate5 the fifth predicate
     * @param effect5    the effect associated to the fifth predicate
     * @param otherwise  the default effect, computed if all the predicates are false
     * @param <O>        the type of the computation result
     * @return a Cond expression
     */
    public static <O> CondExp<O> par(final IO<Boolean> predicate1,
                                     final Supplier<IO<O>> effect1,
                                     final IO<Boolean> predicate2,
                                     final Supplier<IO<O>> effect2,
                                     final IO<Boolean> predicate3,
                                     final Supplier<IO<O>> effect3,
                                     final IO<Boolean> predicate4,
                                     final Supplier<IO<O>> effect4,
                                     final IO<Boolean> predicate5,
                                     final Supplier<IO<O>> effect5,
                                     final Supplier<IO<O>> otherwise
                                    ) {
        return new CondExpPar<>(List.of(requireNonNull(predicate1),
                                        requireNonNull(predicate2),
                                        requireNonNull(predicate3),
                                        requireNonNull(predicate4),
                                        requireNonNull(predicate5)
                                       ),
                                List.of(requireNonNull(effect1),
                                        requireNonNull(effect2),
                                        requireNonNull(effect3),
                                        requireNonNull(effect4),
                                        requireNonNull(effect5)
                                       ),
                                requireNonNull(otherwise),
                                null
        );


    }

    /**
     * It creates a Cond expression which predicates are computed in parallel. Once all the predicates succeed, the
     * expression is reduced to the effect of the fist one that is true, following the order they are passed in the
     * constructor.
     * <p>
     * Not like expressions created with the <code>seq</code> constructor, <strong>all the predicates must terminate
     * before the whole expression is reduced to the selected effect, no matter if one fails or is evaluated to
     * true</strong>. If one predicate terminates with an exception, the whole expression fails.
     *
     * @param predicate1 the first predicate
     * @param effect1    the effect associated to the first predicate
     * @param predicate2 the second predicate
     * @param effect2    the effect associated to the second predicate
     * @param predicate3 the third predicate
     * @param effect3    the effect associated to the third predicate
     * @param predicate4 the forth predicate
     * @param effect4    the effect associated to the forth predicate
     * @param predicate5 the fifth predicate
     * @param effect5    the effect associated to the fifth predicate
     * @param predicate6 the sixth predicate
     * @param effect6    the effect associated to the sixth predicate
     * @param otherwise  the default effect, returned if all predicates are evaluated to false
     * @param <O>        the type of the computation result
     * @return a Cond expression
     */
    public static <O> CondExp<O> par(final IO<Boolean> predicate1,
                                     final Supplier<IO<O>> effect1,
                                     final IO<Boolean> predicate2,
                                     final Supplier<IO<O>> effect2,
                                     final IO<Boolean> predicate3,
                                     final Supplier<IO<O>> effect3,
                                     final IO<Boolean> predicate4,
                                     final Supplier<IO<O>> effect4,
                                     final IO<Boolean> predicate5,
                                     final Supplier<IO<O>> effect5,
                                     final IO<Boolean> predicate6,
                                     final Supplier<IO<O>> effect6,
                                     final Supplier<IO<O>> otherwise
                                    ) {
        return new CondExpPar<>(List.of(requireNonNull(predicate1),
                                        requireNonNull(predicate2),
                                        requireNonNull(predicate3),
                                        requireNonNull(predicate4),
                                        requireNonNull(predicate5),
                                        requireNonNull(predicate6)
                                       ),
                                List.of(requireNonNull(effect1),
                                        requireNonNull(effect2),
                                        requireNonNull(effect3),
                                        requireNonNull(effect4),
                                        requireNonNull(effect5),
                                        requireNonNull(effect6)
                                       ),
                                requireNonNull(otherwise),
                                null
        );


    }

    /**
     * It creates a Cond expression which predicates are computed sequentially, one after the other. If a predicates
     * succeed and is evaluated to true, the expression is reduced to its effect. Predicates are evaluated in the order
     * they are passed in the constructor. If all the predicates succeed and all are evaluated to false, the expression
     * is reduced to the specified default effect. If a predicate terminates with an exception, the expression fails.
     *
     * @param predicate1 the first predicate
     * @param effect1    the effect associated to the first predicate
     * @param predicate2 the second predicate
     * @param effect2    the effect associated to the second predicate
     * @param otherwise  the default effect, computed if all the predicates are false
     * @param <O>        the type of the computation result
     * @return a Cond expression
     */
    public static <O> CondExp<O> seq(final IO<Boolean> predicate1,
                                     final Supplier<IO<O>> effect1,
                                     final IO<Boolean> predicate2,
                                     final Supplier<IO<O>> effect2,
                                     final Supplier<IO<O>> otherwise
                                    ) {

        return new CondExpSeq<>(List.of(requireNonNull(predicate1),
                                        requireNonNull(predicate2)
                                       ),
                                List.of(requireNonNull(effect1),
                                        requireNonNull(effect2)
                                       ),
                                requireNonNull(otherwise),
                                null
        );

    }


    /**
     * It creates a Cond expression which predicates are computed sequentially, one after the other. If a predicates
     * succeed and is evaluated to true, the expression is reduced to its effect. Predicates are evaluated in the order
     * they are passed in the constructor. If all the predicates succeed and all are evaluated to false, the expression
     * is reduced to the specified default effect. If a predicate terminates with an exception, the expression fails.
     *
     * @param predicate1 the first predicate
     * @param effect1    the effect associated to the first predicate
     * @param predicate2 the second predicate
     * @param effect2    the effect associated to the second predicate
     * @param predicate3 the third predicate
     * @param effect3    the effect associated to the third predicate
     * @param otherwise  the default effect, returned if all predicates are evaluated to false
     * @param <O>        the type of the computation result
     * @return a Cond expression
     */
    public static <O> CondExp<O> seq(final IO<Boolean> predicate1,
                                     final Supplier<IO<O>> effect1,
                                     final IO<Boolean> predicate2,
                                     final Supplier<IO<O>> effect2,
                                     final IO<Boolean> predicate3,
                                     final Supplier<IO<O>> effect3,
                                     final Supplier<IO<O>> otherwise
                                    ) {

        return new CondExpSeq<>(List.of(requireNonNull(predicate1),
                                        requireNonNull(predicate2),
                                        requireNonNull(predicate3)
                                       ),
                                List.of(requireNonNull(effect1),
                                        requireNonNull(effect2),
                                        requireNonNull(effect3)
                                       ),
                                requireNonNull(otherwise),
                                null
        );

    }


    /**
     * It creates a Cond expression which predicates are computed sequentially, one after the other. If a predicates
     * succeed and is evaluated to true, the expression is reduced to its effect. Predicates are evaluated in the order
     * they are passed in the constructor. If all the predicates succeed and all are evaluated to false, the expression
     * is reduced to the specified default effect. If a predicate terminates with an exception, the expression fails.
     *
     * @param predicate1 the first predicate
     * @param effect1    the effect associated to the first predicate
     * @param predicate2 the second predicate
     * @param effect2    the effect associated to the second predicate
     * @param predicate3 the third predicate
     * @param effect3    the effect associated to the third predicate
     * @param predicate4 the forth predicate
     * @param effect4    the effect associated to the forth predicate
     * @param otherwise  the default effect, computed if all the predicates are false
     * @param <O>        the type of the computation result
     * @return a Cond expression
     */
    public static <O> CondExp<O> seq(final IO<Boolean> predicate1,
                                     final Supplier<IO<O>> effect1,
                                     final IO<Boolean> predicate2,
                                     final Supplier<IO<O>> effect2,
                                     final IO<Boolean> predicate3,
                                     final Supplier<IO<O>> effect3,
                                     final IO<Boolean> predicate4,
                                     final Supplier<IO<O>> effect4,
                                     final Supplier<IO<O>> otherwise
                                    ) {

        return new CondExpSeq<>(List.of(requireNonNull(predicate1),
                                        requireNonNull(predicate2),
                                        requireNonNull(predicate3),
                                        requireNonNull(predicate4)
                                       ),
                                List.of(requireNonNull(effect1),
                                        requireNonNull(effect2),
                                        requireNonNull(effect3),
                                        requireNonNull(effect4)
                                       ),
                                requireNonNull(otherwise),
                                null
        );

    }


    /**
     * It creates a Cond expression which predicates are computed sequentially, one after the other. If a predicates
     * succeed and is evaluated to true, the expression is reduced to its effect. Predicates are evaluated in the order
     * they are passed in the constructor. If all the predicates succeed and all are evaluated to false, the expression
     * is reduced to the specified default effect. If a predicate terminates with an exception, the expression fails.
     *
     * @param predicate1 the first predicate
     * @param effect1    the effect associated to the first predicate
     * @param predicate2 the second predicate
     * @param effect2    the effect associated to the second predicate
     * @param predicate3 the third predicate
     * @param effect3    the effect associated to the third predicate
     * @param predicate4 the forth predicate
     * @param effect4    the effect associated to the forth predicate
     * @param predicate5 the fifth predicate
     * @param effect5    the effect associated to the fifth predicate
     * @param otherwise  the default effect, computed if all the predicates are false
     * @param <O>        the type of the computation result
     * @return a Cond expression
     */
    public static <O> CondExp<O> seq(final IO<Boolean> predicate1,
                                     final Supplier<IO<O>> effect1,
                                     final IO<Boolean> predicate2,
                                     final Supplier<IO<O>> effect2,
                                     final IO<Boolean> predicate3,
                                     final Supplier<IO<O>> effect3,
                                     final IO<Boolean> predicate4,
                                     final Supplier<IO<O>> effect4,
                                     final IO<Boolean> predicate5,
                                     final Supplier<IO<O>> effect5,
                                     final Supplier<IO<O>> otherwise
                                    ) {


        return new CondExpSeq<>(List.of(requireNonNull(predicate1),
                                        requireNonNull(predicate2),
                                        requireNonNull(predicate3),
                                        requireNonNull(predicate4),
                                        requireNonNull(predicate5)
                                       ),
                                List.of(requireNonNull(effect1),
                                        requireNonNull(effect2),
                                        requireNonNull(effect3),
                                        requireNonNull(effect4),
                                        requireNonNull(effect5)
                                       ),
                                requireNonNull(otherwise),
                                null
        );


    }


    /**
     * It creates a Cond expression which predicates are computed sequentially, one after the other. If a predicates
     * succeeds and is evaluated to true, the expression is reduced to its associated effect. Predicates are evaluated
     * in the order they are passed in the constructor. If all the predicates succeeds and all are evaluated to false,
     * the expression is reduced to the specified default effect. If a predicate terminates with an exception, the
     * expression fails.
     *
     * @param predicate1 the first predicate
     * @param effect1    the effect associated to the first predicate
     * @param predicate2 the second predicate
     * @param effect2    the effect associated to the second predicate
     * @param predicate3 the third predicate
     * @param effect3    the effect associated to the third predicate
     * @param predicate4 the forth predicate
     * @param effect4    the effect associated to the forth predicate
     * @param predicate5 the fifth predicate
     * @param effect5    the effect associated to the fifth predicate
     * @param predicate6 the sixth predicate
     * @param effect6    the effect associated to the sixth predicate
     * @param otherwise  the default effect, computed if all the predicates are false
     * @param <O>        the type of the computation result
     * @return a Cond expression
     */
    public static <O> CondExp<O> seq(final IO<Boolean> predicate1,
                                     final Supplier<IO<O>> effect1,
                                     final IO<Boolean> predicate2,
                                     final Supplier<IO<O>> effect2,
                                     final IO<Boolean> predicate3,
                                     final Supplier<IO<O>> effect3,
                                     final IO<Boolean> predicate4,
                                     final Supplier<IO<O>> effect4,
                                     final IO<Boolean> predicate5,
                                     final Supplier<IO<O>> effect5,
                                     final IO<Boolean> predicate6,
                                     final Supplier<IO<O>> effect6,
                                     final Supplier<IO<O>> otherwise
                                    ) {

        return new CondExpSeq<>(List.of(requireNonNull(predicate1),
                                        requireNonNull(predicate2),
                                        requireNonNull(predicate3),
                                        requireNonNull(predicate4),
                                        requireNonNull(predicate5),
                                        requireNonNull(predicate6)
                                       ),
                                List.of(requireNonNull(effect1),
                                        requireNonNull(effect2),
                                        requireNonNull(effect3),
                                        requireNonNull(effect4),
                                        requireNonNull(effect5),
                                        requireNonNull(effect6)
                                       ),
                                requireNonNull(otherwise),
                                null
        );
    }


    @Override
    public abstract CondExp<O> retryEach(final Predicate<Throwable> predicate,
                                         final RetryPolicy policy
                                        );


    @Override
    public abstract CondExp<O> debugEach(
            final EventBuilder<O> messageBuilder
                                        );


    @Override
    public abstract CondExp<O> debugEach(final String context);


    @Override
    public CondExp<O> retryEach(final RetryPolicy policy) {
        return retryEach(e -> true, policy);
    }

}
