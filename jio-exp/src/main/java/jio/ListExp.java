package jio;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Represents an expression that is reduced to a list of values. You can create ListExp expressions using the 'seq'
 * method to evaluate effects sequentially or using the 'par' method to evaluate effects in parallel. If one effect
 * fails, the entire expression fails.
 *
 * @param <O> the type of the values
 */
public abstract sealed class ListExp<O> extends Exp<List<O>> permits ListExpPar, ListExpSeq {

    List<IO<O>> list;

    ListExp(List<IO<O>> list,
            Function<ExpEvent, BiConsumer<List<O>, Throwable>> logger
           ) {
        super(logger);
        this.list = list;
    }

    /**
     * Creates a ListExp from a list of effects that will be evaluated sequentially. If one fails, the whole expression
     * fails.
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
     * Creates a ListExp from a list of effects that will be evaluated in parallel. If one fails, the whole expression
     * fails.
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
     * Returns the size of the list.
     *
     * @return the number of effects
     */
    public int size() {
        return list.size();
    }

    /**
     * Creates a new list expression with the given effect appended to the end.
     *
     * @param effect the effect to append
     * @return a new list expression with the given effect appended
     */
    public abstract ListExp<O> append(final IO<O> effect);

    /**
     * Returns the first effect from the list that is evaluated, either if it succeeds or fails.
     *
     * @return the first effect that is evaluated
     */
    @SuppressWarnings("unchecked")
    public IO<O> race() {
        return IO.effect(() -> CompletableFuture.anyOf(list.stream()
                                                           .map(Supplier::get)
                                                           .toArray(CompletableFuture[]::new))
                                                .thenApply(it -> ((O) it))
                        );
    }

    /**
     * Checks if the list is empty.
     *
     * @return true if the list is empty, false otherwise
     */
    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * Returns the first effect from the list. Throws an IndexOutOfBoundsException if the list is empty.
     *
     * @return the first effect
     */
    public IO<O> head() {
        return list.get(0);
    }

    /**
     * Returns all the effects in the list except the first one.
     *
     * @return a new list expression containing all effects except the first one
     */
    public abstract ListExp<O> tail();


    @Override
    public abstract ListExp<O> retryEach(final Predicate<Throwable> predicate,
                                         final RetryPolicy policy
                                        );


    @Override
    public ListExp<O> retryEach(final RetryPolicy policy) {
        return retryEach(e -> true, policy);
    }


    @Override
    public abstract ListExp<O> debugEach(final EventBuilder<List<O>> messageBuilder
                                        );

    @Override
    public abstract ListExp<O> debugEach(final String context);

}
