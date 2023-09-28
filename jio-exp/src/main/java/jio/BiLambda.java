package jio;


import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import static java.util.Objects.requireNonNull;

/**
 * Represents a function that takes two inputs of types 'A' and 'B' and produces an 'IO' effect with a result of type 'O'.
 *
 * @param <A> the type of the first input
 * @param <B> the type of the second input
 * @param <O> the type of the effect's result
 */
public interface BiLambda<A, B, O> extends BiFunction<A, B, IO<O>> {


    /**
     * Transforms a 'BiPredicate' into a 'BiLambda' for producing boolean 'IO' effects.
     *
     * @param predicate the predicate to transform
     * @param <A> the type of the first parameter of the predicate
     * @param <B> the type of the second parameter of the predicate
     * @return a 'BiLambda' that produces boolean 'IO' effects
     */
    static <A, B> BiLambda<A, B, Boolean> lift(final BiPredicate<A, B> predicate) {
        requireNonNull(predicate);
        return (a, b) -> {
            try {
                return IO.succeed(predicate.test(a, b));
            } catch (Exception e) {
                return IO.fail(e);
            }
        };
    }

    /**
     * Transforms a 'BiFunction' into a 'BiLambda' for producing 'IO' effects with a custom result type 'O'.
     *
     * @param fn the function to transform
     * @param <A> the type of the first parameter of the function
     * @param <B> the type of the second parameter of the function
     * @param <O> the type of the result produced by the function
     * @return a 'BiLambda' that produces 'IO' effects with a result of type 'O'
     */
    static <A, B, O> BiLambda<A, B, O> lift(final BiFunction<A, B, O> fn) {
        requireNonNull(fn);
        return (a, b) -> {
            try {
                return IO.succeed(fn.apply(a, b));
            } catch (Exception e) {
                return IO.fail(e);
            }
        };
    }
}
