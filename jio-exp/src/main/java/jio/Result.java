package jio;

import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents the result of a computation, which can be either a success or a failure. This interface is sealed and
 * permits implementations of {@link Result.Success} and {@link Result.Failure}.
 *
 * @param <Output> the type of the result.
 */
public sealed interface Result<Output> permits Result.Success, Result.Failure {

  /**
   * A Result representing a null output.
   */
  Result<Void> NULL = new Success<>(null);

  /**
   * A Result representing a successful boolean output with a value of true.
   */
  Result<Boolean> TRUE = new Success<>(true);

  /**
   * A Result representing a successful boolean output with a value of false.
   */
  Result<Boolean> FALSE = new Success<>(false);

  /**
   * Checks whether this result represents a failure.
   *
   * @return true if this result represents a failure, false otherwise.
   */
  boolean isFailure();

  /**
   * Checks whether this result represents a failure that matches the given predicate.
   *
   * @param predicate the predicate to check against the failure exception.
   * @return true if this result represents a failure and the exception matches the predicate, false otherwise.
   */
  boolean isFailure(Predicate<? super Exception> predicate);

  /**
   * Checks whether this result represents a success that matches the given predicate.
   *
   * @param predicate the predicate to check against the success output.
   * @return true if this result represents a success and the output matches the predicate, false otherwise.
   */
  boolean isSuccess(Predicate<Output> predicate);

  /**
   * Checks whether this result represents a success.
   *
   * @return true if this result represents a success, false otherwise.
   */
  boolean isSuccess();

  /**
   * Maps the output of this result to another output using the provided function.
   *
   * @param fn       the mapping function.
   * @param <Mapped> the type of the new output.
   * @return a new result with the output mapped using the provided function.
   */
  default <Mapped> Result<Mapped> map(Function<Output, Mapped> fn) {
    return switch (this) {
      case Success<Output> success -> new Success<>(fn.apply(success.output));
      case Failure<Output> failure -> new Failure<>(failure.exception);
    };
  }

  /**
   * Retrieves the output of this result. If this result represents a failure, throws a CompletionException wrapping the
   * failure exception.
   *
   * @return the output of this result.
   * @throws CompletionException if this result represents a failure.
   */
  default Output getOutput() throws CompletionException {
    return switch (this) {
      case Success<Output>(Output output) -> output;
      case Failure<Output>(Exception exception) -> throw new CompletionException(exception);
    };
  }

  /**
   * Retrieves the output of this result. If this result represents a failure, throws the failure exception.
   *
   * @return the output of this result.
   * @throws Exception if this result represents a failure.
   */
  default Output getOutputOrThrow() throws Exception {
    return switch (this) {
      case Success<Output>(Output output) -> output;
      case Failure<Output>(Exception exception) -> throw exception;
    };
  }

  /**
   * Represents a successful result with an output of type Output.
   *
   * @param <Output> the type of the output.
   */
   record Success<Output>(Output output) implements Result<Output> {

    @Override
    public boolean isFailure() {
      return false;
    }

    @Override
    public boolean isFailure(final Predicate<? super Exception> predicate) {
      return false;
    }

    @Override
    public boolean isSuccess(final Predicate<Output> predicate) {
      return predicate.test(output);
    }

    @Override
    public boolean isSuccess() {
      return true;
    }
  }

  /**
   * Represents a failed result with an exception.
   *
   * @param <Output> the type of the output.
   */
  record Failure<Output>(Exception exception) implements Result<Output> {

    @Override
    public boolean isFailure() {
      return true;
    }

    @Override
    public boolean isFailure(final Predicate<? super Exception> predicate) {
      return predicate.test(exception);
    }

    @Override
    public boolean isSuccess(final Predicate<Output> predicate) {
      return false;
    }

    @Override
    public boolean isSuccess() {
      return false;
    }
  }

}
