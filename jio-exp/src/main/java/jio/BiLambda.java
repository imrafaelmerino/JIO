package jio;


import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import static java.util.Objects.requireNonNull;

/**
 * Function that takes two inputs and produces an IO effect.
 *
 * @param <A> the type of the first input
 * @param <B> the type of the second input
 * @param <O> the type of the effect
 */
public interface BiLambda<A, B, O> extends BiFunction<A, B, IO<O>> {


    /**
     * Transforms a BiPredicate into a BiLambda
     * @param predicate the predicate
     * @return a BiLambda that produces boolean effects
     * @param <A> the type of the first parameter of the predicate
     * @param <B> the type of the second parameter of the predicate
     */
    static <A, B> BiLambda<A, B, Boolean> lift(final BiPredicate<A, B> predicate) {
        requireNonNull(predicate);
        return (a, b) -> {
            try {
                return IO.fromValue(predicate.test(a, b));
            } catch (Exception e) {
                return IO.fromFailure(e);
            }
        };
    }

    /**
     * Transforms a BiFunction into a BiLambda
     * @param fn the function
     * @return a BiLambda that produces effects of type O
     * @param <A> the type of the first parameter of the function
     * @param <B> the type of the second parameter of the function
     * @param <O> the type of the function output
     */
    static <A, B, O> BiLambda<A, B, O> lift(final BiFunction<A, B, O> fn) {
        requireNonNull(fn);
        return (a, b) -> {
            try {
                return IO.fromValue(fn.apply(a, b));
            } catch (Exception e) {
                return IO.fromFailure(e);
            }
        };
    }
}
