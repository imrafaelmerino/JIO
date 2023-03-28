package jio;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * Represents a boolean expression that will be reduced to true <strong>if and only if
 * all the subexpression succeed and at least one is evaluated to true</strong>.
 *
 * @see AnyExp#par(IO, IO[])
 * @see AnyExp#seq(IO, IO[])
 */
public abstract sealed class AnyExp extends Exp<Boolean> permits AnyExpPar, AnyExpSeq {

    final List<IO<Boolean>> exps;

    AnyExp(Function<ExpEvent,BiConsumer<Boolean, Throwable>> logger, List<IO<Boolean>> exps) {
        super(logger);
        this.exps = exps;
    }

    /**
     * Creates an AnyExp expression where all the subexpressions are evaluated in parallel,
     * <strong>as long as they are computed by a different thread</strong>. In the following example,
     * isDivisibleByTwo and isDivisibleByThree will be computed by the same thread (the caller thread),
     * despite the fact that the par constructor is used:
     * <pre>
     * {@code
     *     Lambda<Integer,Boolean> isDivisibleByTwoOrThree =
     *              n -> {
     *
     *                  IO<Boolean> isDivisibleByTwo = IO.compute(()-> n % 2 == 0);
     *                  IO<Boolean> isDivisibleByThree = IO.compute(()-> n % 3 == 0);
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
     * <p>
     * On the other hand, isDivisibleByTwo and isDivisibleByThree will be computed in parallel by
     * different threads in the following example (if the executor pool is bigger than one and two
     * threads are free):
     *
     * <pre>
     * {@code
     *     Lambda<Integer,Boolean> isDivisibleByTwoAndThree =
     *              n -> {
     *
     *                  IO<Boolean> isDivisibleByTwo = IO.computeOn(()-> n % 2 == 0, executor);
     *
     *                  IO<Boolean> isDivisibleByThree = IO.computeOn(()-> n % 3 == 0, executor);
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
     * Not like expressions created with the {@link #seq(IO, IO[]) seq} constructor, <strong>all the
     * subexpressions must terminate before the whole expression  is reduced, no matter if one fails
     * or one is evaluated to true</strong>.
     * If a subexpression terminates with an exception, the whole expression fails.
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
     * Creates an AnyExp expression where all the subexpression are <strong>always</strong> evaluated sequentially.
     * If one subexpression terminates with an exception, the whole expression ends immediately.
     * <p>
     * On the other hand, if one subexpression is evaluated to true, the whole expression ends
     * and is also evaluated to true.
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


    /**
     * Creates a new AnyExp expression where the given retry policy is applied recursively
     * to every subexpression when an exception is tested true against the specified predicate.
     *
     * @param predicate the predicate to test exceptions
     * @param policy    the retry policy
     * @return a new AnyExp
     */
    @Override
    public abstract AnyExp retryEach(final Predicate<Throwable> predicate,
                                     final RetryPolicy policy
                                    );


    /**
     * Creates a new AnyExp that will write to the given logger information about every
     * computation evaluated to reduce this expression (like {@link #debugEach(String)} does).
     * A final log message created with the specified messageBuilder is written after reducing
     * the whole expression
     *
     * @param messageBuilder the builder to create the log message from the result of the expression
     * @return a new AnyExp
     * @see #debugEach(String) debugEach
     */
    @Override
    public abstract AnyExp debugEach(
                                   final EventBuilder<Boolean> messageBuilder
                                    );

    /**
     * Creates a new AnyExp that will print out on the console information about every
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
     * AnyExp.par(IO.FALSE,
     *            IO.TRUE
     *           )
     *       .debugEach("context")
     *       .join()
     *
     *
     * 2023-02-04T18:02:57.700266+01:00 main DEBUGGER [context] 7961625 success AnyExpPar[0] false
     * 2023-02-04T18:02:57.708664+01:00 main DEBUGGER [context] 72875 success AnyExpPar[1] true
     * 2023-02-04T18:02:57.709509+01:00 main DEBUGGER [context] 12478625 success AnyExpPar true
     * }
     * </pre>
     *
     * @param context the context shared by all the subexpressions that will be printed out
     * @return a new AnyExp
     */
    @Override
    public abstract AnyExp debugEach(final String context);

    /**
     * Creates a new AnyExp expression where the given retry policy is applied recursively
     * to every subexpression when an exception happens.
     *
     * @param policy the retry policy
     * @return a new AnyExp
     */
    @Override
    public AnyExp retryEach(final RetryPolicy policy) {
        return retryEach(e -> true, policy);
    }

}
