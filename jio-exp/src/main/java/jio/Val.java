package jio;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Represents a value. A value is an irreducible expression.
 *
 * @param <O> the type of the value
 */
final class Val<O> extends IO<O> {
    @Override
    public CompletableFuture<O> get() {
        try {
            return effect.get();
        } catch (Throwable throwable) {
            return CompletableFuture.failedFuture(throwable);
        }
    }

    private final Supplier<CompletableFuture<O>> effect;

    Val(final Supplier<CompletableFuture<O>> effect) {
        this.effect = requireNonNull(effect);
    }

}
