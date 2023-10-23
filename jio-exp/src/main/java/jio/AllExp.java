package jio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.*;
import java.util.stream.Collector;

import static java.util.Objects.requireNonNull;

/**
 * Represents a boolean expression that will be reduced to true <strong>if and only if all the subexpressions succeed
 * and are evaluated to true</strong>.
 *
 * @see AllExp#par(IO, IO[])
 * @see AllExp#seq(IO, IO[])
 */
public sealed abstract class AllExp extends Exp<Boolean> permits AllExpPar, AllExpSeq {


    /**
     * list of subexpressions the AllExp is made up of
     */
    protected final List<IO<Boolean>> exps;

    AllExp(Function<ExpEvent, BiConsumer<Boolean, Throwable>> debugger,
           List<IO<Boolean>> exps
          ) {
        super(debugger);
        this.exps = exps;
    }

    /**
     * Returns a Collector for collecting IO boolean effects into an AllExp. All the effects will be executed
     * sequentially to compute the final boolean.
     *
     * @return A Collector for collecting boolean effects into an AllExp
     */
    public static Collector<IO<Boolean>, ?, AllExp> seqCollector() {
        return new Collector<IO<Boolean>, List<IO<Boolean>>, AllExp>() {

            private final Set<Characteristics> characteristics = Collections.emptySet();

            @Override
            public Supplier<List<IO<Boolean>>> supplier() {
                return ArrayList::new;
            }

            @Override
            public BiConsumer<List<IO<Boolean>>, IO<Boolean>> accumulator() {
                return List::add;
            }

            @Override
            public BinaryOperator<List<IO<Boolean>>> combiner() {
                return (a, b) -> {
                    a.addAll(b);
                    return b;
                };
            }

            @Override
            public Function<List<IO<Boolean>>, AllExp> finisher() {
                return AllExp::seq;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return characteristics;
            }
        };
    }

    /**
     * Returns a Collector for collecting IO boolean effects into an AllExp. All the effects will be executed in
     * parallel to compute the final boolean.
     *
     * @return A Collector for collecting boolean effects into an AllExp
     */
    public static Collector<IO<Boolean>, ?, AllExp> parCollector() {
        return new Collector<IO<Boolean>, List<IO<Boolean>>, AllExp>() {

            private final Set<Characteristics> characteristics = Collections.emptySet();

            @Override
            public Supplier<List<IO<Boolean>>> supplier() {
                return ArrayList::new;
            }

            @Override
            public BiConsumer<List<IO<Boolean>>, IO<Boolean>> accumulator() {
                return List::add;
            }

            @Override
            public BinaryOperator<List<IO<Boolean>>> combiner() {
                return (a, b) -> {
                    a.addAll(b);
                    return b;
                };
            }

            @Override
            public Function<List<IO<Boolean>>, AllExp> finisher() {
                return AllExp::par;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return characteristics;
            }
        };
    }

    /**
     * Creates an AllExp expression where all the subexpressions are evaluated in parallel,
     * <strong>as long as they are computed by a different thread</strong>. In the following example,
     * `isDivisibleByTwo` and `isDivisibleByThree` will be computed by the same thread (the caller thread), despite the
     * fact that the `par` constructor is used:
     *
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
     *
     * <p>
     * On the other hand, isDivisibleByTwo and isDivisibleByThree will be computed in parallel by different threads in
     * the following example (if the executor pool is bigger than one and at least two threads are free):
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
     * Not like expressions created with the {@link #seq(IO, IO[]) seq} constructor, <strong>all the subexpressions must
     * terminate before the whole expression is reduced, no matter if one fails or is evaluated to false</strong>. If
     * one subexpression terminates with an exception, the whole expression fails.
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

    public static AllExp par(final List<IO<Boolean>> ios) {
        return new AllExpPar(ios, null);
    }

    public static AllExp seq(final List<IO<Boolean>> ios) {
        return new AllExpSeq(ios, null);
    }

    /**
     * Creates an AllExp expression where all the subexpression are <strong>always</strong> evaluated sequentially. If
     * one subexpression terminates with an exception or is evaluated to false, the whole expression ends immediately,
     * and the rest of subexpressions (if any) are not evaluated.
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
     * Creates a new AllExp expression where the given retry policy is applied recursively to each subexpression.
     *
     * @param predicate the predicate to test exceptions. If false, no retry is attempted
     * @param policy    the retry policy
     * @return a new AllExp
     */
    @Override
    public abstract AllExp retryEach(final Predicate<? super Throwable> predicate,
                                     final RetryPolicy policy
                                    );


    @Override
    public abstract Exp<Boolean> debugEach(final EventBuilder<Boolean> builder);


    @Override
    public abstract AllExp debugEach(final String context);

    /**
     * Creates a new AllExp expression where the given retry policy is applied recursively to every subexpression.
     *
     * @param policy the retry policy
     * @return a new AllExp
     */
    @Override
    public AllExp retryEach(RetryPolicy policy) {
        return retryEach(e -> true, policy);
    }


}
