package jio;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;

import static java.util.Objects.requireNonNull;

/**
 * Represents an immutable expression which result is a list of values
 *
 * @param <O> the type of the values
 */
public abstract sealed class ListExp<O> extends Exp<List<O>> permits ListExpPar, ListExpSeq {

    List<IO<O>> list;

    ListExp(List<IO<O>> list,
            Function<ExpEvent,BiConsumer<List<O>, Throwable>> logger
           ) {
        super(logger);
        this.list = list;
    }

    /**
     * returns the size of the list
     *
     * @return the number of effects
     */
    public int size() {
        return list.size();
    }


    /**
     * Creates a ListExp from a list of effects that will be evaluated sequentially. If one fails, the whole expression
     * fails
     *
     * @param effects the list of effects
     * @param <O>     the type of the effects
     * @return a ListExp
     */
    @SafeVarargs
    public static <O> ListExp<O> seq(final IO<O>... effects) {
        var xs = new ArrayList<IO<O>>();
        for (var other : requireNonNull(effects)) xs.add(requireNonNull(other));
        return new ListExpSeq<>(xs, null);
    }


    /**
     * Creates a new list expression with the given effect appended to the end
     *
     * @param effect the effect
     * @return a new list expression with the given effect appended to the end
     */
    public abstract ListExp<O> append(final IO<O> effect);

    /**
     * Creates a ListExp that will be evaluated to an empty list.
     *
     * @param <O> the type of the effects
     * @return a ListExp
     */
    public static <O> ListExp<O> par() {
        return new ListExpPar<>(new ArrayList<>(), null);
    }


    /**
     * Creates a ListExp from a list of effects that will be evaluated in parallel. If one fails, the whole expression
     * fails
     *
     * @param effects the list of effects
     * @param <O>     the type of the list effects
     * @return a ListExp
     */
    @SafeVarargs
    public static <O> ListExp<O> par(final IO<O>... effects) {

        var list = new ArrayList<IO<O>>();
        for (IO<O> effect : requireNonNull(effects)) list.add(requireNonNull(effect));
        return new ListExpPar<>(list, null);
    }


    /**
     * Returns the first effect from the list that is evaluated, either if it succeeds or fails
     *
     * @return the first effect that is evaluated
     */
    @SuppressWarnings("unchecked")
    public IO<O> race() {
        return IO.fromEffect(() -> CompletableFuture.anyOf(list.stream()
                                                               .map(Supplier::get)
                                                               .toArray(CompletableFuture[]::new))
                                                    .thenApply(it -> ((O) it))
                            );
    }

    /**
     * returns true if the list is empty
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * returns the fist effect if it exists, throwing an IndexOutOfBoundsException otherwise
     *
     * @return the first effect
     */
    public IO<O> head() {
        return list.get(0);
    }

    /**
     * returns all the effects but the head
     *
     * @return all the effects but the head
     */
    public abstract ListExp<O> tail();

    /**
     * Creates a new ListExp expression where the given retry policy is applied recursively
     * to every subexpression when an exception is tested true against the specified predicate.
     *
     * @param predicate the predicate to test exceptions
     * @param policy    the retry policy
     * @return a new ListExp
     */
    @Override
    public abstract ListExp<O> retryEach(final Predicate<Throwable> predicate,
                                         final RetryPolicy policy
                                        );

    /**
     * Creates a new ListExp expression where the given retry policy is applied recursively
     * to every subexpression when an exception happens.
     *
     * @param policy the retry policy
     * @return a new ListExp
     */
    @Override
    public ListExp<O> retryEach(final RetryPolicy policy) {
        return retryEach(e -> true, policy);
    }

    /**
     * Creates a new ListExp that will write to the given logger information about every
     * computation evaluated to reduce this expression (like {@link #debugEach(String)} does).
     * A final log message created with the specified messageBuilder is written after reducing
     * the whole expression
     *
     * @param messageBuilder the builder to create the log message from the result of the expression
     * @return a new ListExp
     * @see #debugEach(String) debugEach
     */
    @Override
    public abstract ListExp<O> debugEach(final EventBuilder<List<O>> messageBuilder
                                        );

    /**
     * Creates a new ListExp that will print out on the console information about every
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
     *        ListExp.par(IO.succeed(1),
     *                    IO.succeed(2),
     *                    IO.succeed(3)
     *                   )
     *               .debugEach("context")
     *               .join()
     *
     *
     * }
     * </pre>
     * 2023-02-04T15:54:19.535311+01:00 pool-1-thread-1 DEBUGGER [context] 1020664458 success ListExpPar[0]
     * 2023-02-04T15:54:19.535383+01:00 main DEBUGGER [context] 1018793416 success ListExpPar[2]
     * 2023-02-04T15:54:19.535365+01:00 pool-2-thread-1 DEBUGGER [context] 1019055375 success ListExpPar[1]
     * 2023-02-04T15:54:19.537009+01:00 main DEBUGGER [context] 1022577167 success ListExpPar [a, b, c]
     *
     * @param context the context shared by all the subexpressions that will be printed out
     * @return a new ListExp
     */
    @Override
    public abstract ListExp<O> debugEach(final String context);

}
