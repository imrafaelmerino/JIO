package jio;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * Represents a boolean expression that will be reduced to true <strong>if and only if all the
 * subexpressions succeed and are evaluated to true</strong>.
 *
 * @see AllExp#par(IO, IO[])
 * @see AllExp#seq(IO, IO[])
 */
public sealed abstract class AllExp extends Exp<Boolean> permits AllExpPar, AllExpSeq {

    /**
     * list of subexpressions the AllExp is made up of
     */
    protected final List<IO<Boolean>> exps;

    AllExp(Function<ExpEvent,BiConsumer<Boolean, Throwable>> logger,
           List<IO<Boolean>> exps
          ) {
        super(logger);
        this.exps = exps;
    }

    /**
     * Creates an AllExp expression where all the subexpressions are evaluated in parallel,
     * <strong>as long as they are computed by a different thread</strong>. In the following example,
     * isDivisibleByTwo and isDivisibleByThree will be computed by the same thread (the caller thread),
     * despite the fact that the par constructor is used:
     * <pre>
     * {@code
     *     Lambda<Integer,Boolean> isDivisibleByTwoAndThree =
     *              n -> {
     *
     *                  IO<Boolean> isDivisibleByTwo = IO.fromSupplier(() -> n % 2 == 0);
     *                  IO<Boolean> isDivisibleByThree = IO.fromSupplier(() -> n % 3 == 0);
     *                  return AllExp.par(isDivisibleByTwo,
     *                                    isDivisibleByThree
     *                                    );
     *              };
     *
     *
     *     boolean result = isDivisibleByTwoAndThree.apply(6).join()
     *
     * }
     * </pre>
     * <p>
     * On the other hand, isDivisibleByTwo and isDivisibleByThree will be computed in parallel by
     * different threads in the following example (if the executor pool is bigger than one and at least
     * two threads are free) :
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
     *                  return AllExp.par(isDivisibleByTwo,
     *                                    isDivisibleByThree
     *                                    );
     *              };
     *
     *
     *     boolean result = isDivisibleByTwoAndThree.apply(6).join()
     *
     * }
     * </pre>
     *
     * <p>
     * Not like expressions created with the {@link #seq(IO, IO[]) seq} constructor, <strong>all the
     * subexpressions must terminate before the whole expression is reduced, no matter if one fails
     * or is evaluated to false</strong>.
     * If one subexpression terminates with an exception, the whole expression fails.
     *
     * @param bool   the first subexpression
     * @param others the others subexpressions
     * @return an AllExp
     */
    @SafeVarargs
    public static AllExp par(final IO<Boolean> bool,
                             final IO<Boolean>... others
                            ) {
        var exps = new ArrayList<IO<Boolean>>();
        exps.add(requireNonNull(bool));
        for (var other : requireNonNull(others)) exps.add(requireNonNull(other));
        return new AllExpPar(exps, null);
    }

    /**
     * Creates an AllExp expression where all the subexpression are <strong>always</strong> evaluated
     * sequentially. If one subexpression terminates with an exception or is evaluated to false,
     * the whole expression ends immediately and the rest of subexpressions (if any) are not evaluated.
     *
     * @param bool   the first subexpression
     * @param others the others subexpressions
     * @return an AllExp
     */
    @SafeVarargs
    public static AllExp seq(final IO<Boolean> bool,
                             final IO<Boolean>... others
                            ) {
        var exps = new ArrayList<IO<Boolean>>();
        exps.add(requireNonNull(bool));
        for (var other : requireNonNull(others)) exps.add(requireNonNull(other));
        return new AllExpSeq(exps, null);
    }

    /**
     * Creates a new AllExp expression where the given retry policy is applied recursively
     * to each subexpression.
     *
     * @param predicate the predicate to test exceptions. If false, no retry is attempted
     * @param policy    the retry policy
     * @return a new AllExp
     */
    @Override
    public abstract AllExp retryEach(final Predicate<Throwable> predicate,
                                     final RetryPolicy policy
                                    );


    /**
     * Creates a new AllExp that will write to the given logger information about every
     * computation evaluated to reduce this expression (like {@link #debugEach(String)} does).
     * A final log message created with the specified log builder is written after reducing
     * the whole expression
     *
     * @param builder the builder to create the log message from the result of the expression
     * @return a new AllExp
     * @see #debugEach(String) debugEach
     */
    @Override
    public abstract Exp<Boolean> debugEach(final EventBuilder<Boolean> builder);

    /**
     * Creates a new AllExp that will print out on the console information about every
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
     * AllExp.seq(IO.TRUE,
     *            IO.TRUE
     *           )
     *       .debugEach("context")
     *       .join()
     *
     *
     * }
     * </pre>
     *
     *2023-02-04T18:04:21.459985+01:00 main DEBUGGER [context] 7712125 success AllExpPar[0] true
     *2023-02-04T18:04:21.468211+01:00 main DEBUGGER [context] 73834 success AllExpPar[1] true
     *2023-02-04T18:04:21.469131+01:00 main DEBUGGER [context] 12266834 success AllExpPar true
     *
     * @param context the context shared by all the subexpressions that will be printed out
     * @return a new AllExp
     */
    @Override
    public abstract AllExp debugEach(final String context);

    /**
     * Creates a new AllExp expression where the given retry policy is applied recursively
     * to every subexpression.
     *
     * @param policy the retry policy
     * @return a new AllExp
     */
    @Override
    public AllExp retryEach(RetryPolicy policy) {
        return retryEach(e -> true, policy);
    }


}
