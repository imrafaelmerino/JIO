package jio;

import java.util.concurrent.Callable;

public sealed interface Result<Output> extends Callable<Output> permits Result.Success, Result.Failure {


  Result<Void> NULL = new Success<>(null);
  Result<Boolean> TRUE = new Success<>(true);

  Result<Boolean> FALSE = new Success<>(false);

  @Override
  default Output call() throws Exception {
    return switch (this) {
      case Success<Output>(Output output) -> output;
      case Failure<Output>(Exception exception) -> throw exception;
    };
  }


  record Success<Output>(Output value) implements Result<Output> {

  }

  record Failure<Output>(Exception exception) implements Result<Output> {

  }

}
