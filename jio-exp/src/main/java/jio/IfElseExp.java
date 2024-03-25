package jio;

import static java.util.Objects.requireNonNull;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import jio.Result.Failure;
import jio.Result.Success;

/**
 * Represents an expression that combines a predicate effect with two alternative effect suppliers, one for the
 * consequence and another for the alternative branch. If the predicate evaluates to true, the expression is reduced to
 * the consequence effect; otherwise, it is reduced to the alternative effect.
 *
 * @param <Output> the type of the result that the expression will produce
 */
public final class IfElseExp<Output> extends Exp<Output> {

  private final IO<Boolean> predicate;
  private Supplier<IO<Output>> consequence = IO::NULL;
  private Supplier<IO<Output>> alternative = IO::NULL;

  private IfElseExp(final IO<Boolean> predicate,
                    final Function<EvalExpEvent, BiConsumer<Output, Throwable>> debugger
                   ) {
    super(debugger);
    this.predicate = predicate;
  }

  /**
   * Creates an IfElseExp with the given boolean effect as the predicate.
   *
   * @param predicate the predicate effect
   * @param <O>       the type that the expression will produce
   * @return an IfElseExp instance
   */
  public static <O> IfElseExp<O> predicate(final IO<Boolean> predicate) {
    return new IfElseExp<>(requireNonNull(predicate),
                           null);
  }

  /**
   * Sets the consequence effect to be computed if the predicate evaluates to true.
   *
   * @param consequence the consequence effect
   * @return this IfElseExp instance with the specified consequence effect
   */
  public IfElseExp<Output> consequence(final Supplier<IO<Output>> consequence) {
    IfElseExp<Output> exp = new IfElseExp<>(predicate,
                                            jfrPublisher);
    exp.alternative = alternative;
    exp.consequence = requireNonNull(consequence);
    return exp;
  }

  /**
   * Sets the alternative effect to be computed if the predicate evaluates to false.
   *
   * @param alternative the alternative effect
   * @return this IfElseExp instance with the specified alternative effect
   */
  public IfElseExp<Output> alternative(final Supplier<IO<Output>> alternative) {
    IfElseExp<Output> exp = new IfElseExp<>(predicate,
                                            jfrPublisher);
    exp.consequence = consequence;
    exp.alternative = requireNonNull(alternative);
    return exp;
  }

  @Override
  public IfElseExp<Output> retryEach(final Predicate<? super Throwable> predicate,
                                     final RetryPolicy policy
                                    ) {
    return new IfElseExp<>(this.predicate.retry(requireNonNull(predicate),
                                                requireNonNull(policy)
                                               ),
                           jfrPublisher
    )
        .consequence(() -> consequence.get()
                                      .retry(predicate,
                                             policy)
                    )
        .alternative(() -> alternative.get()
                                      .retry(predicate,
                                             policy)
                    );
  }

  @Override
  Result<Output> reduceExp() {

    try {
      Result<Boolean> predicate = this.predicate.call();
      return switch (predicate) {
        case Success(Boolean output) -> output ? this.consequence.get()
                                                                 .call() : this.alternative.get()
                                                                                           .call();
        case Failure(Exception e) -> new Failure<>(e);
      };
    } catch (Exception e) {
      return new Failure<>(e);
    }

  }

  @Override
  public IfElseExp<Output> debugEach(final EventBuilder<Output> eventBuilder) {
    return new IfElseExp<>(DebuggerHelper.debugIO(predicate,
                                                  String.format("%s-predicate",
                                                                eventBuilder.exp
                                                               ),

                                                  eventBuilder.context
                                                 ),
                           getJFRPublisher(eventBuilder)
    )
        .consequence(() -> DebuggerHelper.debugIO(consequence.get(),
                                                  String.format("%s-consequence",
                                                                eventBuilder.exp
                                                               ),
                                                  eventBuilder.context
                                                 )

                    )
        .alternative(() -> DebuggerHelper.debugIO(alternative.get(),
                                                  String.format("%s-alternative",
                                                                eventBuilder.exp
                                                               ),
                                                  eventBuilder.context
                                                 )

                    );
  }

  @Override
  public IfElseExp<Output> retryEach(final RetryPolicy policy) {
    return retryEach(_ -> true,
                     policy);
  }

  @Override
  public IfElseExp<Output> debugEach(final String context) {
    return debugEach(EventBuilder.of(this.getClass()
                                         .getSimpleName(),
                                     context)
                    );
  }

}
