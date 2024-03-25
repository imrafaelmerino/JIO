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
 * Represents a boolean expression that will be reduced to true <strong>if and only if all the subexpressions succeed
 * and are evaluated to true</strong>.
 *
 * @see AllExp#par(IO, IO[])
 * @see AllExp#seq(IO, IO[])
 */
public abstract sealed class AllExp extends Exp<Boolean> permits AllExpPar, AllExpSeq {

  /**
   * the list of subexpressions the AllExp is made up of
   */
  protected final List<IO<Boolean>> exps;

  AllExp(Function<EvalExpEvent, BiConsumer<Boolean, Throwable>> debugger,
         List<IO<Boolean>> exps
        ) {
    super(debugger);
    this.exps = exps;
  }

  /**
   * Returns a Collector for collecting IO boolean effects into an AllExp. All the effects will be executed sequentially
   * to compute the final boolean.
   *
   * @return A Collector for collecting boolean effects into an AllExp
   */
  public static Collector<IO<Boolean>, ?, AllExp> seqCollector() {
    return new Collector<IO<Boolean>, List<IO<Boolean>>, AllExp>() {

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
      public Function<List<IO<Boolean>>, AllExp> finisher() {
        return AllExp::seq;
      }

      @Override
      public Set<Characteristics> characteristics() {
        return characteristics;
      }
    };
  }

  /**
   * Returns a Collector for collecting IO boolean effects into an AllExp. All the effects will be executed in parallel
   * to compute the final boolean.
   *
   * @return A Collector for collecting boolean effects into an AllExp
   */
  public static Collector<IO<Boolean>, ?, AllExp> parCollector() {
    return new Collector<IO<Boolean>, List<IO<Boolean>>, AllExp>() {

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
      public Function<List<IO<Boolean>>, AllExp> finisher() {
        return AllExp::par;
      }

      @Override
      public Set<Characteristics> characteristics() {
        return characteristics;
      }
    };
  }

  /**
   * Creates an AllExp expression where all the subexpressions are evaluated in parallel. If one subexpression fails,
   * the whole expression fails immediately with the same error. In case of success, all the subexpressions must end
   * before returning the result.
   *
   * @param bool   the first subexpression
   * @param others the others subexpressions
   * @return an AllExp
   */
  @SafeVarargs
  public static AllExp par(final IO<Boolean> bool,
                           final IO<Boolean>... others
                          ) {
    var exps = new ArrayList<IO<Boolean>>();
    exps.add(requireNonNull(bool));
    for (var other : requireNonNull(others)) {
      exps.add(requireNonNull(other));
    }
    return new AllExpPar(exps,
                         null
    );
  }

  /**
   * Creates an AllExp expression where all the subexpressions are evaluated in parallel. If one subexpression fails,
   * the whole expression fails immediately with the same error. In case of success, all the subexpressions must end
   * before returning the result.
   *
   * @param ios the boolean subexpressions
   * @return an AllExp
   */
  public static AllExp par(final List<IO<Boolean>> ios) {
    return new AllExpPar(ios,
                         null
    );
  }

  /**
   * Creates an AllExp expression where all the subexpression are evaluated sequentially. If one subexpression
   * terminates with an exception or is evaluated to false, the whole expression ends immediately, and the rest of
   * subexpressions (if any) are not evaluated.
   *
   * @param ios the list of subexpression
   * @return an AllExp
   */
  public static AllExp seq(final List<IO<Boolean>> ios) {
    return new AllExpSeq(ios,
                         null
    );
  }

  /**
   * Creates an AllExp expression where all the subexpression are evaluated sequentially. If one subexpression
   * terminates with an exception or is evaluated to false, the whole expression ends immediately, and the rest of
   * subexpressions (if any) are not evaluated.
   *
   * @param bool   the first subexpression
   * @param others the others subexpressions
   * @return an AllExp
   */
  @SafeVarargs
  public static AllExp seq(final IO<Boolean> bool,
                           final IO<Boolean>... others
                          ) {
    var exps = new ArrayList<IO<Boolean>>();
    exps.add(requireNonNull(bool));
    for (var other : requireNonNull(others)) {
      exps.add(requireNonNull(other));
    }
    return new AllExpSeq(exps,
                         null);
  }

  /**
   * Creates a new AllExp expression where the given retry policy is applied recursively to each subexpression.
   *
   * @param predicate the predicate to test exceptions. If false, no retry is attempted
   * @param policy    the retry policy
   * @return a new AllExp
   */
  @Override
  public abstract AllExp retryEach(final Predicate<? super Throwable> predicate,
                                   final RetryPolicy policy
                                  );

  @Override
  public abstract AllExp debugEach(final EventBuilder<Boolean> builder);

  @Override
  public abstract AllExp debugEach(final String context);

  /**
   * Creates a new AllExp expression where the given retry policy is applied recursively to every subexpression.
   *
   * @param policy the retry policy
   * @return a new AllExp
   */
  @Override
  public AllExp retryEach(RetryPolicy policy) {
    return retryEach(_ -> true,
                     policy
                    );
  }
}
