package jio.api;

import java.util.function.Function;

public abstract sealed class Try<O> permits Try.Success, Try.Failure {

    public static <O> Try<O> succeed(O result) {
        return new Success<>(result);
    }

    public static <O> Try<O> fail(Exception e) {
        return new Failure(e).cast();
    }

    public O get() throws Exception {
        return switch (this) {
            case Try.Failure failure -> throw failure.exception;
            case Try.Success<O> success -> success.result;
        };
    }

    public <U> Try<U> flatMap(Function<O, Try<U>> fn) {
        return switch (this) {
            case Try.Failure failure -> failure.cast();
            case Try.Success<O> success -> fn.apply(success.result);
        };
    }

    public <U> Try<U> map(Function<O, U> fn) {
        return switch (this) {
            case Try.Failure failure -> failure.cast();
            case Try.Success<O> success -> Try.succeed(fn.apply(success.result));
        };
    }

    static final class Success<O> extends Try<O> {
        final O result;

        Success(O result) {
            this.result = result;
        }
    }

    static final class Failure extends Try<Object> {

        //Failure has no parametrized type -> down-casting is safe
        @SuppressWarnings("unchecked")
        private <U> Try<U> cast() {
            return (Try<U>) this;
        }

        final Exception exception;

        Failure(Exception failure) {
            this.exception = failure;
        }
    }
}
