package jio;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * Represents a boolean expression that will be reduced to true <strong>if and only if at least one of the
 * subexpressions is evaluated to true and all of the executed subexpressions succeed</strong>.
 *
 * @see AnyExp#par(IO, IO[])
 * @see AnyExp#seq(IO, IO[])
 */
public abstract sealed class AnyExp extends Exp<Boolean> permits AnyExpPar, AnyExpSeq {

    final List<IO<Boolean>> exps;

    AnyExp(Function<ExpEvent, BiConsumer<Boolean, Throwable>> debugger,
           List<IO<Boolean>> exps
          ) {
        super(debugger);
        this.exps = exps;
    }

    /**
     * Creates an AnyExp expression where all the subexpressions are evaluated in parallel,
     * <strong>as long as they are computed by a different thread</strong>. In the following example,
     * `isDivisibleByTwo` and `isDivisibleByThree` will be computed by the same thread (the caller thread), despite the
     * fact that the `par` constructor is used:
     *
     * <pre>
     * {@code
     *     Lambda<Integer,Boolean> isDivisibleByTwoOrThree =
     *              n -> {
     *
     *                  IO<Boolean> isDivisibleByTwo = IO.fromSupplier(()-> n % 2 == 0);
     *                  IO<Boolean> isDivisibleByThree = IO.fromSupplier(()-> n % 3 == 0);
     *                  return AnyExp.par(isDivisibleByTwo,
     *                                    isDivisibleByThree
     *                                    );
     *              };
     *
     *
     *     boolean result = isDivisibleByTwoAndThree.apply(8).join()
     *
     * }
     * </pre>
     *
     * <p>
     * On the other hand, `isDivisibleByTwo` and `isDivisibleByThree` will be computed in parallel by different threads
     * in the following example (if the executor pool is bigger than one and two threads are free):
     *
     * <pre>
     * {@code
     *     Lambda<Integer,Boolean> isDivisibleByTwoAndThree =
     *              n -> {
     *
     *                  IO<Boolean> isDivisibleByTwo = IO.fromSupplier(()-> n % 2 == 0, executor);
     *
     *                  IO<Boolean> isDivisibleByThree = IO.fromSupplier(()-> n % 3 == 0, executor);
     *
     *                  return AnyExp.par(isDivisibleByTwo,
     *                                    isDivisibleByThree
     *                                    );
     *              };
     *
     *
     *     boolean result = isDivisibleByTwoAndThree.apply(8).join()
     *
     * }
     * </pre>
     *
     * <p>
     * Not like expressions created with the {@link #seq(IO, IO[]) seq} constructor, <strong>all the subexpressions must
     * terminate before the whole expression is reduced, no matter if one fails or one is evaluated to true</strong>. If
     * a subexpression terminates with an exception, the whole expression fails.
     *
     * @param bool   the first subexpression
     * @param others the others subexpressions
     * @return an AnyExp
     */
    @SafeVarargs
    public static AnyExp par(final IO<Boolean> bool,
                             final IO<Boolean>... others
                            ) {
        var exps = new ArrayList<IO<Boolean>>();
        exps.add(requireNonNull(bool));
        for (IO<Boolean> other : requireNonNull(others)) exps.add(requireNonNull(other));
        return new AnyExpPar(exps, null);
    }

    /**
     * Creates an AnyExp expression where all the subexpression are <strong>always</strong> evaluated sequentially. If
     * one subexpression terminates with an exception, the whole expression ends immediately.
     * <p>
     * On the other hand, if one subexpression is evaluated to true, the whole expression ends and is also evaluated to
     * true.
     *
     * @param bool   the first subexpression
     * @param others the others subexpressions
     * @return an AllExp
     */
    @SafeVarargs
    public static AnyExp seq(final IO<Boolean> bool,
                             final IO<Boolean>... others
                            ) {
        var exps = new ArrayList<IO<Boolean>>();
        exps.add(requireNonNull(bool));
        for (IO<Boolean> other : requireNonNull(others)) exps.add(requireNonNull(other));
        return new AnyExpSeq(exps, null);
    }


    @Override
    public abstract AnyExp retryEach(final Predicate<Throwable> predicate,
                                     final RetryPolicy policy
                                    );


    @Override
    public abstract AnyExp debugEach(final EventBuilder<Boolean> messageBuilder);

    @Override
    public abstract AnyExp debugEach(final String context);


    @Override
    public AnyExp retryEach(final RetryPolicy policy) {
        return retryEach(e -> true, policy);
    }

}
