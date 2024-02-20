package jio;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Class with handy functions for development with jio
 */
final class Fun {

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

}
