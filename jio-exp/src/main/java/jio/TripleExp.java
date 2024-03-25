package jio;

import static java.util.Objects.requireNonNull;

import fun.tuple.Triple;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents an expression that is reduced to a triple. Their elements can be evaluated either in parallel or
 * sequentially. In both cases, if one fails, the whole expression fails immediately.
 * <p>
 * You can create TripleExp expressions using the 'seq' method to evaluate effects sequentially, or using the 'par'
 * method to evaluate effects in parallel. If one effect fails, the entire expression fails.
 *
 * @param <First>  the type of the first computation
 * @param <Second> the type of the second computation
 * @param <Third>  the type of the third computation
 */
public abstract sealed class TripleExp<First, Second, Third> extends Exp<Triple<First, Second, Third>> permits
                                                                                                       TripleExpPar,
                                                                                                       TripleExpSeq {

  final IO<First> _1;
  final IO<Second> _2;
  final IO<Third> _3;

  TripleExp(final IO<First> _1,
            final IO<Second> _2,
            final IO<Third> _3,
            final Function<EvalExpEvent, BiConsumer<Triple<First, Second, Third>, Throwable>> debugger
           ) {
    super(debugger);
    this._1 = _1;
    this._2 = _2;
    this._3 = _3;
  }

  /**
   * create a tuple of three effects that will be evaluated sequentially. If an effect fails, the next ones are not
   * evaluated and the whole expression fails.
   *
   * @param first  the first effect
   * @param second the second effect
   * @param third  the third effect
   * @param <A>    the type of the first effect result
   * @param <B>    the type of the second effect result
   * @param <C>    the type of the third effect result
   * @return a PairExp
   */
  public static <A, B, C> TripleExp<A, B, C> seq(final IO<A> first,
                                                 final IO<B> second,
                                                 final IO<C> third
                                                ) {
    return new TripleExpSeq<>(requireNonNull(first),
                              requireNonNull(second),
                              requireNonNull(third),
                              null
    );
  }

  /**
   * Create a tuple of three effects that will be evaluated in paralell. If one fails, the whole expression fails
   * immediately
   *
   * @param first  the first effect
   * @param second the second effect
   * @param third  the third effect
   * @param <A>    type of the first effect result
   * @param <B>    type of the second effect result
   * @param <C>    type of the third effect result
   * @return a pair expression evaluated in parallel
   */
  public static <A, B, C> TripleExp<A, B, C> par(final IO<A> first,
                                                 final IO<B> second,
                                                 final IO<C> third
                                                ) {
    return new TripleExpPar<>(requireNonNull(first),
                              requireNonNull(second),
                              requireNonNull(third),
                              null
    );
  }

  /**
   * returns the first effect of the triple
   *
   * @return first effect of the triple
   */
  public IO<First> first() {
    return _1;
  }

  /**
   * returns the second effect of the triple
   *
   * @return second effect of the triple
   */
  public IO<Second> second() {
    return _2;
  }

  /**
   * returns the third effect of the triple
   *
   * @return third effect of the triple
   */
  public IO<Third> third() {
    return _3;
  }

  @Override
  public abstract TripleExp<First, Second, Third> retryEach(final Predicate<? super Throwable> predicate,
                                                            final RetryPolicy policy
                                                           );

  @Override
  public TripleExp<First, Second, Third> retryEach(final RetryPolicy policy) {
    return retryEach(e -> true,
                     policy
                    );
  }

  @Override
  public abstract TripleExp<First, Second, Third> debugEach(final EventBuilder<Triple<First, Second, Third>> messageBuilder
                                                           );

  @Override
  public abstract TripleExp<First, Second, Third> debugEach(final String context);

}
