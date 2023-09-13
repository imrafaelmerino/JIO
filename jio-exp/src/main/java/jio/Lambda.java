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
     * transforms a Predicate into a Lambda
     * @param predicate the predicate
     * @return a Lambda that produces boolean effects
     * @param <O> the type of the parameter of the predicate
     */
    static <O> Lambda<O, Boolean> lift(final Predicate<O> predicate) {
        requireNonNull(predicate);
        return o -> {
            try {
                return IO.value(predicate.test(o));
            } catch (Exception e) {
                return IO.failure(e);
            }
        };
    }
    /**
     * transforms a Function into a Lambda
     * @param fn the function
     * @return a Lambda that produces effects of type O
     * @param <I> the type of the function parameter
     * @param <O> the type of the function output
     */
    static <I, O> Lambda<I, O> lift(final Function<I, O> fn) {
        requireNonNull(fn);
        return o -> {
            try {
                return IO.value(fn.apply(o));
            } catch (Exception e) {
                return IO.failure(e);
            }
        };
    }

    /**
     * map this effect into another one using the given map function
     * @param map the map function
     * @return a new effect
     * @param <Q> the type of new effect
     */
    default <Q> Lambda<I,Q> map(Function<IO<O>,IO<Q>> map){
        return i-> map.apply(this.apply(i));
    }

}
