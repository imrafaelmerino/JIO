package jio;

import java.util.function.Predicate;

public sealed interface Result<Output> permits Result.Success, Result.Failure {

  Result<Void> NULL = new Success<>(null);

  Result<Boolean> TRUE = new Success<>(true);

  Result<Boolean> FALSE = new Success<>(false);

  boolean isFailure();

  boolean isFailure(Predicate<? super Exception> predicate);

  boolean isSuccess(Predicate<Output> predicate);

  boolean isSuccess();


  default Output tryGet() throws Exception {
    return switch (this) {
      case Success<Output>(Output output) -> output;
      case Failure<Output>(Exception exception) -> throw exception;
    };
  }

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
