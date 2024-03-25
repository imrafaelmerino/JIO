package jio;

import static java.util.Objects.requireNonNull;

import fun.tuple.Pair;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents an expression that is reduced to a pair. Their elements can be evaluated either in parallel or
 * sequentially. In both cases, if one fails, the whole expression fails immediately.
 * <p>
 * You can create PairExp expressions using the 'seq' method to evaluate effects sequentially, or using the 'par' method
 * to evaluate effects in parallel. If one effect fails, the entire expression fails.
 *
 * @param <First>  the type of the first computation
 * @param <Second> the type of the second computation
 */
public abstract sealed class PairExp<First, Second> extends Exp<Pair<First, Second>> permits PairExpSeq, PairExpPar {

  final IO<First> _1;
  final IO<Second> _2;

  PairExp(Function<EvalExpEvent, BiConsumer<Pair<First, Second>, Throwable>> debugger,
          IO<First> _1,
          IO<Second> _2) {
    super(debugger);
    this._1 = _1;
    this._2 = _2;
  }

  /**
   * create a tuple of two effects that will be evaluated sequentially. If the first one fails, the second one is not
   * evaluated and the whole expression fails.
   *
   * @param first  the first effect
   * @param second the second effect
   * @param <A>    the type of the first effect result
   * @param <B>    the type of the second effect result
   * @return a PairExp evaluated sequentially
   */
  public static <A, B> PairExp<A, B> seq(final IO<A> first,
                                         final IO<B> second
                                        ) {
    return new PairExpSeq<>(requireNonNull(first),
                            requireNonNull(second),
                            null
    );
  }

  /**
   * create a tuple of two effects that will be evaluated in paralell. If one fails, the whole expression fails
   * immediately
   *
   * @param first  first effect of the pair
   * @param second second effect of the pair
   * @param <A>    type of the first effect result
   * @param <B>    type of the second effect result
   * @return a pair expression evaluated in parallel
   */
  public static <A, B> PairExp<A, B> par(final IO<A> first,
                                         final IO<B> second
                                        ) {
    return new PairExpPar<>(requireNonNull(first),
                            requireNonNull(second),
                            null
    );
  }

  /**
   * Returns the first element of the pair.
   *
   * @return the first element of the pair
   */
  public IO<First> first() {
    return _1;
  }

  /**
   * Returns the second element of the pair.
   *
   * @return the second element of the pair
   */
  public IO<Second> second() {
    return _2;
  }

  @Override
  public abstract PairExp<First, Second> retryEach(final Predicate<? super Throwable> predicate,
                                                   final RetryPolicy policy
                                                  );

  @Override
  public PairExp<First, Second> retryEach(final RetryPolicy policy) {
    return retryEach(_ -> true,
                     policy);
  }

  @Override
  public abstract PairExp<First, Second> debugEach(final EventBuilder<Pair<First, Second>> messageBuilder
                                                  );

  @Override
  public abstract PairExp<First, Second> debugEach(final String context);

}
