package jio;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Represents a boolean expression that will be reduced to true <strong>if and only if at least one of the
 * subexpressions is evaluated to true and all of the executed subexpressions succeed</strong>.
 *
 * @see AnyExp#par(IO, IO[])
 * @see AnyExp#seq(IO, IO[])
 */
public abstract sealed class AnyExp extends Exp<Boolean> permits AnyExpPar, AnyExpSeq {

  final List<IO<Boolean>> exps;

  AnyExp(Function<EvalExpEvent, BiConsumer<Boolean, Throwable>> debugger,
         List<IO<Boolean>> exps
        ) {
    super(debugger);
    this.exps = exps;
  }

  /**
   * Returns a Collector for collecting IO boolean effects into an AnyExp. All the effects will be executed sequentially
   * to compute the final boolean.
   *
   * @return A Collector for collecting boolean effects into an AnyExp
   */
  public static Collector<IO<Boolean>, ?, AnyExp> parCollector() {
    return new Collector<IO<Boolean>, List<IO<Boolean>>, AnyExp>() {

      private final Set<Characteristics> characteristics = Collections.emptySet();

      @Override
      public Supplier<List<IO<Boolean>>> supplier() {
        return ArrayList::new;
      }

      @Override
      public BiConsumer<List<IO<Boolean>>, IO<Boolean>> accumulator() {
        return List::add;
      }

      @Override
      public BinaryOperator<List<IO<Boolean>>> combiner() {
        return (a, b) -> {
          a.addAll(b);
          return b;
        };
      }

      @Override
      public Function<List<IO<Boolean>>, AnyExp> finisher() {
        return AnyExp::par;
      }

      @Override
      public Set<Characteristics> characteristics() {
        return characteristics;
      }
    };
  }

  /**
   * Returns a Collector for collecting IO boolean effects into an AnyExp. All the effects will be executed in parallel
   * to compute the final boolean.
   *
   * @return A Collector for collecting boolean effects into an AnyExp
   */
  public static Collector<IO<Boolean>, ?, AnyExp> seqCollector() {
    return new Collector<IO<Boolean>, List<IO<Boolean>>, AnyExp>() {

      private final Set<Characteristics> characteristics = Collections.emptySet();

      @Override
      public Supplier<List<IO<Boolean>>> supplier() {
        return ArrayList::new;
      }

      @Override
      public BiConsumer<List<IO<Boolean>>, IO<Boolean>> accumulator() {
        return List::add;
      }

      @Override
      public BinaryOperator<List<IO<Boolean>>> combiner() {
        return (a, b) -> {
          a.addAll(b);
          return b;
        };
      }

      @Override
      public Function<List<IO<Boolean>>, AnyExp> finisher() {
        return AnyExp::seq;
      }

      @Override
      public Set<Characteristics> characteristics() {
        return characteristics;
      }
    };
  }

  /**
   * Creates an AnyExp expression where all the subexpressions are evaluated in parallel. If one expression fails, the
   * whole expression fails immediately with the same error. In case of success, all the subexpressions must end before
   * returning the result.
   *
   * @param bool   the first subexpression
   * @param others the others subexpressions
   * @return an AnyExp
   */
  @SafeVarargs
  public static AnyExp par(final IO<Boolean> bool,
                           final IO<Boolean>... others
                          ) {
    var exps = new ArrayList<IO<Boolean>>();
    exps.add(requireNonNull(bool));
    for (IO<Boolean> other : requireNonNull(others)) {
      exps.add(requireNonNull(other));
    }
    return new AnyExpPar(exps,
                         null);
  }

  /**
   * Creates an AnyExp expression where all the subexpression are evaluated sequentially. If one subexpression
   * terminates with an exception, the whole expression ends immediately. On the other hand, if one subexpression is
   * evaluated to true, the whole expression ends and is also evaluated to true.
   *
   * @param bool   the first subexpression
   * @param others the others subexpressions
   * @return an AllExp
   */
  @SafeVarargs
  public static AnyExp seq(final IO<Boolean> bool,
                           final IO<Boolean>... others
                          ) {
    var exps = new ArrayList<IO<Boolean>>();
    exps.add(requireNonNull(bool));
    for (IO<Boolean> other : requireNonNull(others)) {
      exps.add(requireNonNull(other));
    }
    return new AnyExpSeq(exps,
                         null);
  }

  /**
   * Creates an AnyExp expression where all the subexpressions are evaluated in parallel. If one expression fails, the
   * whole expression fails immediately with the same error. In case of success, all the subexpressions must end before
   * returning the result.
   *
   * @param ios the list of subexpressions
   * @return an AnyExp
   */
  public static AnyExp par(final List<IO<Boolean>> ios) {
    return new AnyExpPar(ios,
                         null);
  }

  /**
   * Creates an AnyExp expression where all the subexpression are evaluated sequentially. If one subexpression
   * terminates with an exception, the whole expression ends immediately. On the other hand, if one subexpression is
   * evaluated to true, the whole expression ends and is also evaluated to true.
   *
   * @param ios the list of subexpressions
   * @return an AllExp
   */
  public static AnyExp seq(final List<IO<Boolean>> ios) {
    return new AnyExpSeq(ios,
                         null);
  }

  @Override
  public abstract AnyExp retryEach(final Predicate<? super Throwable> predicate,
                                   final RetryPolicy policy
                                  );

  @Override
  public abstract AnyExp debugEach(final EventBuilder<Boolean> messageBuilder);

  @Override
  public abstract AnyExp debugEach(final String context);

  @Override
  public AnyExp retryEach(final RetryPolicy policy) {
    return retryEach(_ -> true,
                     policy);
  }

}
