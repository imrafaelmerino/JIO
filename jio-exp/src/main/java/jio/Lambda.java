package jio;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents a function that takes an input and produces an IO effect.
 *
 * @param <Input>  the type of the input
 * @param <Output> the type of the effect
 */
public interface Lambda<Input, Output> extends Function<Input, IO<Output>> {

  /**
   * Composes this Lambda with another Lambda, producing a new Lambda. The resulting Lambda, when applied to an input,
   * will execute this Lambda followed by the other Lambda, creating a sequence of effects.
   *
   * @param <FinalOutput> the type of the result produced by the other Lambda
   * @param other         the other Lambda to be executed after this Lambda
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
   * @param <Input>   the type of the parameter of the predicate
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
   * @param <Input>  the type of the function's input parameter
   * @param <Output> the type of the function's output
   * @param fn       the function to be transformed
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
   * Composes this Lambda with a mapping function that transforms the output of the inner IO operation.
   *
   * <p>This method allows you to apply a function to transform or manipulate the result of the IO operation.</p>
   *
   * @param map            A function that takes an {@code IO<Output>} and returns an {@code IO<MappedOutput>}.
   * @param <MappedOutput> The type of the result after applying the mapping function.
   * @return A new Lambda that represents the composition of this Lambda and the provided mapping function.
   * @throws NullPointerException If the mapping function {@code map} is {@code null}.
   * @since 1.0
   */
  default <MappedOutput> Lambda<Input, MappedOutput> map(Function<IO<Output>, IO<MappedOutput>> map) {
    return input -> map.apply(this.apply(input));
  }

  /**
   * Composes this Lambda with a mapping function that transforms the successful output of the inner IO operation.
   *
   * <p>This method allows you to apply a function to transform or manipulate the successful result of the IO
   * operation.</p>
   *
   * @param mapSuccess     A function that takes an {@code Output} and returns a {@code MappedOutput}.
   * @param <MappedOutput> The type of the result after applying the mapping function.
   * @return A new Lambda that represents the composition of this Lambda and the provided success mapping function.
   * @since 1.0
   */
  default <MappedOutput> Lambda<Input, MappedOutput> mapSuccess(Function<Output, MappedOutput> mapSuccess) {
    return input -> this.apply(input)
                        .map(mapSuccess);
  }

  /**
   * Composes this Lambda with a mapping function that transforms the failure of the inner IO operation.
   *
   * <p>This method allows you to apply a function to transform or handle the failure of the IO operation.</p>
   *
   * @param mapFailure A function that takes a {@code Throwable} and returns a transformed {@code Throwable}.
   * @return A new Lambda that represents the composition of this Lambda and the provided failure mapping function.
   * @since 1.0
   */
  default Lambda<Input, Output> mapFailure(Function<Exception, Exception> mapFailure) {
    return input -> this.apply(input)
                        .mapFailure(mapFailure);
  }

}
