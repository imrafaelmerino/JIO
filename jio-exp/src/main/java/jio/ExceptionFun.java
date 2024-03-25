package jio;

import java.io.EOFException;
import java.net.ConnectException;
import java.net.SocketException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * The {@code ExceptionFun} class provides utility methods for working with exceptions in Java, offering functionalities
 * to find the ultimate cause in an exception chain and to filter exceptions based on specific criteria.
 */
public final class ExceptionFun {

  private ExceptionFun() {
  }

  /**
   * Returns the ultimate cause in the exception chain or the exception if the cause is null.
   *
   * @param exception The initial exception to start the search from.
   * @return The ultimate cause in the exception chain.
   */
  public static Throwable findUltimateCause(Throwable exception) {
    var ultimateCause = Objects.requireNonNull(exception);

    while (ultimateCause.getCause() != null) {
      ultimateCause = ultimateCause.getCause();
    }

    return ultimateCause;
  }

  /**
   * Returns a function that finds the cause in the exception chain that satisfies the given predicate.
   *
   * @param predicate The predicate to test each cause in the chain.
   * @return A function that finds the cause such that the predicate is satisfied.
   * @throws NullPointerException If the provided predicate is {@code null}.
   */
  public static Function<Throwable, Optional<Throwable>> findCauseRecursively(Predicate<Throwable> predicate) {
    Objects.requireNonNull(predicate);

    return e -> {
      var cause = Objects.requireNonNull(e);

      while (cause != null && !predicate.test(cause)) {
        cause = cause.getCause();
      }
      return Optional.ofNullable(cause);
    };
  }

  /**
   * Predicate to check if the given exception indicates a connection refused error. This predicate can be used to
   * filter or handle exceptions related to connection errors.
   *
   * <p>The predicate checks if the ultimate cause of the given exception is an instance of {@link ConnectException}
   * and if the message of the {@link ConnectException} is "Connection refused".</p>
   *
   * @see ConnectException
   */
  public static final Predicate<Throwable> IS_CONNECTION_REFUSE =
      e -> findConnectionExcRecursively("connection refused"::equalsIgnoreCase).apply(e)
                                                                               .isPresent();

  /**
   * Returns a function that finds the cause in the exception chain that satisfies the given predicate, specifically for
   * {@link ConnectException} instances.
   *
   * <p>The function traverses the exception chain recursively starting from the provided exception,
   * identifying instances of {@link ConnectException} and filtering them based on the provided message predicate. If a
   * matching {@link ConnectException} is found, it is returned as an optional.</p>
   *
   * @param messagePredicate The predicate to test each message in the chain.
   * @return A function that finds the {@link ConnectException} cause satisfying the predicate.
   * @throws NullPointerException If the provided predicate is {@code null}.
   * @see ConnectException
   */
  public static Function<Throwable, Optional<ConnectException>> findConnectionExcRecursively(Predicate<String> messagePredicate) {
    return e -> findConnectionExcRecursively.apply(e)
                                            .filter(it -> messagePredicate.test(it.getMessage()));
  }

  /**
   * Returns a function that finds the cause in the exception chain that satisfies the given predicate, specifically for
   * {@link SocketException} instances.
   *
   * <p>The function traverses the exception chain recursively starting from the provided exception,
   * identifying instances of {@link SocketException} and filtering them based on the provided message predicate. If a
   * matching {@link SocketException} is found, it is returned as an optional.</p>
   *
   * @param messagePredicate The predicate to test each message in the chain.
   * @return A function that finds the {@link SocketException} cause satisfying the predicate.
   * @throws NullPointerException If the provided predicate is {@code null}.
   * @see SocketException
   */
  public static Function<Throwable, Optional<SocketException>> findSocketExcRecursively(Predicate<String> messagePredicate) {
    return e -> findSocketExcRecursively.apply(e)
                                        .filter(it -> messagePredicate.test(it.getMessage()));
  }

  /**
   * Returns a function that finds the cause in the exception chain that is an instance of {@link ConnectException}.
   *
   * @see SocketException
   */
  public static Function<Throwable, Optional<ConnectException>> findConnectionExcRecursively =
      e -> findCauseRecursively(exc -> exc instanceof ConnectException).apply(e)
                                                                       .map(it -> ((ConnectException) it));

  /**
   * Returns a function that finds the cause in the exception chain that is an instance of {@link SocketException}.
   *
   * @see SocketException
   */
  public static Function<Throwable, Optional<SocketException>> findSocketExcRecursively =
      e -> findCauseRecursively(exc -> exc instanceof SocketException).apply(e)
                                                                      .map(it -> ((SocketException) it));

  /**
   * Returns a function that finds the cause in the exception chain that is an instance of {@link EOFException}.
   *
   * @see EOFException
   */
  public static Function<Throwable, Optional<EOFException>> findEndOfStreamExcRecursively =
      e -> findCauseRecursively(exc -> exc instanceof EOFException).apply(e)
                                                                   .map(it -> ((EOFException) it));

  /**
   * A predefined function to find instances of {@link SocketException} in the exception chain with a message indicating
   * a "Connection reset" error.
   *
   * @see SocketException
   */
  public static final Predicate<Throwable> IS_CONNECTION_RESET =
      e -> findSocketExcRecursively("connection reset"::equalsIgnoreCase).apply(e)
                                                                         .isPresent();
  /**
   * A predefined function to find instances of {@link EOFException} in the exception chain.
   *
   * @see EOFException
   */
  public static final Predicate<Throwable> IS_END_OF_STREAM =
      e -> findEndOfStreamExcRecursively.apply(e)
                                        .isPresent();

}
