package jio;

import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * Represents a function that takes an input and produces an IO effect.
 *
 * @param <I> the type of the input
 * @param <O> the type of the effect
 */
public interface Lambda<I, O> extends Function<I, IO<O>> {

    /**
     * Transforms a Predicate into a Lambda, producing boolean effects.
     *
     * @param <O>       the type of the parameter of the predicate
     * @param predicate the predicate to be transformed
     * @return a Lambda that produces boolean effects
     */
    static <O> Lambda<O, Boolean> liftPredicate(final Predicate<O> predicate) {
        requireNonNull(predicate);
        return o -> {
            try {
                return IO.succeed(predicate.test(o));
            } catch (Exception e) {
                return IO.fail(e);
            }
        };
    }

    /**
     * Transforms a Function into a Lambda, producing effects of type O.
     *
     * @param <I> the type of the function's input parameter
     * @param <O> the type of the function's output
     * @param fn  the function to be transformed
     * @return a Lambda that produces effects of type O
     */
    static <I, O> Lambda<I, O> liftFunction(final Function<I, O> fn) {
        requireNonNull(fn);
        return o -> {
            try {
                return IO.succeed(fn.apply(o));
            } catch (Exception e) {
                return IO.fail(e);
            }
        };
    }

    /**
     * Maps this effect into another one using the given map function.
     *
     * @param <Q> the type of the new effect
     * @param map the map function to transform this effect
     * @return a new Lambda representing the mapped effect
     */
    default <Q> Lambda<I, Q> map(Function<IO<O>, IO<Q>> map) {
        return i -> map.apply(this.apply(i));
    }

}
