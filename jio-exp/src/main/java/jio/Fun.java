package jio;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Class with handy functions for development with jio
 */
public final class Fun {

  private Fun() {
  }

  static <A, B> Function<Supplier<A>, Supplier<B>> mapSupplier(Function<A, B> map) {
    return supplier -> () -> map.apply(supplier.get());
  }

  static void publishException(String exp,
                               String context,
                               Throwable exc) {
    EvalExpEvent event = new EvalExpEvent();
    event.exception = String.format("%s:%s",
                                    exc.getClass()
                                       .getName(),
                                    exc.getMessage());
    event.result = EvalExpEvent.RESULT.FAILURE.name();
    event.expression = exp;
    event.context = context;
    event.commit();
  }

  /**
   * Finds the ultimate cause in the exception chain.
   * <p>
   * The ultimate cause is the last non-null cause in the exception chain.
   * </p>
   *
   * @param exception The initial exception to start the search from.
   * @return The ultimate cause in the exception chain.
   * @throws NullPointerException If the provided exception is {@code null}.
   */
  public static Throwable findUltimateCause(Throwable exception) {
    var ultimateCause = Objects.requireNonNull(exception);

    // Iterate through the exception chain until the ultimate cause is found
    while (ultimateCause.getCause() != null) {
      ultimateCause = ultimateCause.getCause();
    }

    return ultimateCause;
  }


  /**
   * Finds the first cause in the exception chain that matches the specified predicate.
   * <p>
   * This function iterates through the exception chain until a cause is found that matches the specified predicate. The
   * search does not check the root exception.
   * </p>
   *
   * @param predicate The predicate to test each cause in the exception chain.
   * @return A function that takes a Throwable and returns the first cause matching the predicate, or {@code null} if no
   * matching cause is found.
   * @throws NullPointerException If the provided predicate is {@code null}.
   */
  public static Function<Throwable, Throwable> findCause(Predicate<Throwable> predicate) {
    Objects.requireNonNull(predicate);
    return e -> {
      var cause = Objects.requireNonNull(e);
      while ((cause = cause.getCause()) != null) {
        if (predicate.test(cause)) {
          return cause;
        }
      }
      return null;
    };
  }

}
