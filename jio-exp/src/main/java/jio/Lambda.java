package jio;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * Represents a function that takes an input and produces an IO effect.
 *
 * @param <Input> the type of the input
 * @param <Output> the type of the effect
 */
public interface Lambda<Input, Output> extends Function<Input, IO<Output>> {


  /**
   * Composes this Lambda with another Lambda, producing a new Lambda. The resulting Lambda, when applied to an input,
   * will execute this Lambda followed by the other Lambda, creating a sequence of effects.
   *
   * @param <FinalOutput>   the type of the result produced by the other Lambda
   * @param other the other Lambda to be executed after this Lambda
   * @return a new Lambda that represents the composed effects
   */
  default <FinalOutput> Lambda<Input, FinalOutput> then(final Lambda<Output, FinalOutput> other) {
    Objects.requireNonNull(other);
    return i -> this.apply(i)
                    .then(other);
  }

  /**
   * Transforms a Predicate into a Lambda, producing boolean effects.
   *
   * @param <Input>       the type of the parameter of the predicate
   * @param predicate the predicate to be transformed
   * @return a Lambda that produces boolean effects
   */
  static <Input> Lambda<Input, Boolean> liftPredicate(final Predicate<Input> predicate) {
    requireNonNull(predicate);
    return input -> {
      try {
        return IO.succeed(predicate.test(input));
      } catch (Exception e) {
        return IO.fail(e);
      }
    };
  }

  /**
   * Transforms a Function into a Lambda, producing effects of type O.
   *
   * @param <Input> the type of the function's input parameter
   * @param <Output> the type of the function's output
   * @param fn  the function to be transformed
   * @return a Lambda that produces effects of type O
   */
  static <Input, Output> Lambda<Input, Output> liftFunction(final Function<Input, Output> fn) {
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
  default <Q> Lambda<Input, Q> map(Function<IO<Output>, IO<Q>> map) {
    return input -> map.apply(this.apply(input));
  }

}
