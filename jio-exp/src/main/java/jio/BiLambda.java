package jio;

import static java.util.Objects.requireNonNull;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/**
 * Represents a function that takes two inputs of types 'A' and 'B' and produces an 'IO' effect with a result of type
 * 'O'.
 *
 * @param <FirstInput>  the type of the first input
 * @param <SecondInput> the type of the second input
 * @param <Output>      the type of the effect's result
 */
public interface BiLambda<FirstInput, SecondInput, Output> extends BiFunction<FirstInput, SecondInput, IO<Output>> {

  /**
   * Transforms a 'BiPredicate' into a 'BiLambda' for producing boolean 'IO' effects.
   *
   * @param predicate the predicate to transform
   * @param <A>       the type of the first parameter of the predicate
   * @param <B>       the type of the second parameter of the predicate
   * @return a 'BiLambda' that produces boolean 'IO' effects
   */
  static <A, B> BiLambda<A, B, Boolean> liftPredicate(final BiPredicate<A, B> predicate) {
    requireNonNull(predicate);
    return (a,
            b) -> {
      try {
        return IO.succeed(predicate.test(a,
                                         b));
      } catch (Exception e) {
        return IO.fail(e);
      }
    };
  }

  /**
   * Transforms a 'BiFunction' into a 'BiLambda' for producing 'IO' effects with a custom result type 'O'.
   *
   * @param fn  the function to transform
   * @param <A> the type of the first parameter of the function
   * @param <B> the type of the second parameter of the function
   * @param <O> the type of the result produced by the function
   * @return a 'BiLambda' that produces 'IO' effects with a result of type 'O'
   */
  static <A, B, O> BiLambda<A, B, O> liftFunction(final BiFunction<A, B, O> fn) {
    requireNonNull(fn);
    return (a,
            b) -> {
      try {
        return IO.succeed(fn.apply(a,
                                   b));
      } catch (Exception e) {
        return IO.fail(e);
      }
    };
  }

  /**
   * Partially applies the first parameter (A) and returns a 'Lambda' with the second parameter (B) as the input.
   *
   * @param firstInput The first parameter to partially apply.
   * @return a 'Lambda' with the second parameter (B) as the input.
   */
  default Lambda<SecondInput, Output> partialWithFirst(final FirstInput firstInput) {
    return i -> apply(firstInput,
                      i);
  }

  /**
   * Partially applies the second parameter (B) and returns a 'Lambda' with the first parameter (A) as the input.
   *
   * @param secondInput The second parameter to partially apply.
   * @return a 'Lambda' with the first parameter (A) as the input.
   */
  default Lambda<FirstInput, Output> partialWithSecond(final SecondInput secondInput) {
    return i -> apply(i,
                      secondInput);
  }
}
