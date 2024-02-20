package jio;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

/**
 * Represents a value, which is an irreducible expression. Values of type {@code Val} encapsulate a value of type
 * {@code O}. These values are terminal and represent the end result of an effectful computation.
 *
 * <p>It's important to note that in the context of effectful computations, the {@code IO} type can
 * be either {@code Val} (irreducible) or {@code Exp} (composable expressions made up of different operations). While
 * {@code Val} represents a final value, {@code Exp} expressions are composable and can involve multiple sub-effects
 * that need to be executed in a specific order.
 *
 * @param <Output> the type of the value encapsulated by this {@code Val}.
 */
final class Val<Output> extends IO<Output> {

  private final Supplier<Result<Output>> effect;

  Val(final Supplier<Result<Output>> effect) {
    this.effect = requireNonNull(effect);
  }

  @Override
  public Result<Output> get() {
    return effect.get();
  }

}
