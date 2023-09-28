package jio;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Represents a value, which is an irreducible expression. Values of type {@code Val} encapsulate
 * a value of type {@code O}. These values are terminal and represent the end result of an effectful
 * computation.
 *
 * <p>It's important to note that in the context of effectful computations, the {@code IO} type can be either
 * {@code Val} (irreducible) or {@code Exp} (composable expressions made up of different operations).
 * While {@code Val} represents a final value, {@code Exp} expressions are composable and can involve
 * multiple sub-effects that need to be executed in a specific order.
 *
 * @param <O> the type of the value encapsulated by this {@code Val}.
 */
final class Val<O> extends IO<O> {
    private final Supplier<CompletableFuture<O>> effect;

    Val(final Supplier<CompletableFuture<O>> effect) {
        this.effect = requireNonNull(effect);
    }

    @Override
    public CompletableFuture<O> get() {
        try {
            return effect.get();
        } catch (Throwable throwable) {
            return CompletableFuture.failedFuture(throwable);
        }
    }

}
