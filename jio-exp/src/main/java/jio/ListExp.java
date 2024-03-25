package jio;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.StructuredTaskScope;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import jio.Result.Failure;

/**
 * Represents an expression that is reduced to a list of values. You can create ListExp expressions using the 'seq'
 * method to evaluate effects sequentially or using the 'par' method to evaluate effects in parallel. If one effect
 * fails, the entire expression fails.
 *
 * @param <Elem> the type of the elements of the list
 */
public abstract sealed class ListExp<Elem> extends Exp<List<Elem>> permits ListExpPar, ListExpSeq {

  final List<IO<Elem>> list;

  ListExp(List<IO<Elem>> list,
          Function<EvalExpEvent, BiConsumer<List<Elem>, Throwable>> debugger) {
    super(debugger);
    this.list = list;
  }

  /**
   * Returns a Collector for collecting IO effects into a ListExp. All effects will be executed sequentially to compute
   * the List, and the order of the results in the list is maintained.
   *
   * @param <O> The type of elements in the ListExp and IO objects.
   * @return A Collector for collecting effects into a ListExp
   */
  public static <O> Collector<IO<O>, ?, ListExp<O>> seqCollector() {
    return new Collector<IO<O>, List<IO<O>>, ListExp<O>>() {

      private final Set<Characteristics> characteristics = Collections.emptySet();

      @Override
      public Supplier<List<IO<O>>> supplier() {
        return ArrayList::new;
      }

      @Override
      public BiConsumer<List<IO<O>>, IO<O>> accumulator() {
        return List::add;
      }

      @Override
      public BinaryOperator<List<IO<O>>> combiner() {
        return (a, b) -> {
          a.addAll(b);
          return b;
        };
      }

      @Override
      public Function<List<IO<O>>, ListExp<O>> finisher() {
        return ListExp::seq;
      }

      @Override
      public Set<Characteristics> characteristics() {
        return characteristics;
      }
    };
  }

  /**
   * Returns a Collector for collecting IO effects into a ListExp. All the effects will be executed in parallel to
   * compute the List, and the order of the results in the list is maintained.
   *
   * @param <O> The type of elements in the ListExp and IO objects.
   * @return A Collector for collecting effects into a ListExp
   */
  public static <O> Collector<IO<O>, ?, ListExp<O>> parCollector() {
    return new Collector<IO<O>, List<IO<O>>, ListExp<O>>() {

      private final Set<Characteristics> characteristics = Collections.emptySet();

      @Override
      public Supplier<List<IO<O>>> supplier() {
        return ArrayList::new;
      }

      @Override
      public BiConsumer<List<IO<O>>, IO<O>> accumulator() {
        return List::add;
      }

      @Override
      public BinaryOperator<List<IO<O>>> combiner() {
        return (a, b) -> {
          a.addAll(b);
          return b;
        };
      }

      @Override
      public Function<List<IO<O>>, ListExp<O>> finisher() {
        return ListExp::par;
      }

      @Override
      public Set<Characteristics> characteristics() {
        return characteristics;
      }
    };
  }

  /**
   * Creates a ListExp from a list of effects that will be evaluated sequentially. If one fails, the whole expression
   * fails.
   *
   * @param effects the list of effects
   * @param <O>     the type of the effects
   * @return a ListExp
   */
  @SafeVarargs
  public static <O> ListExp<O> seq(final IO<O>... effects) {
    var xs = new ArrayList<IO<O>>();
    for (var other : requireNonNull(effects)) {
      xs.add(requireNonNull(other));
    }
    return new ListExpSeq<>(xs,
                            null);
  }

  public static <O> ListExp<O> seq(final List<IO<O>> list) {
    return new ListExpSeq<>(list,
                            null);
  }

  /**
   * Creates a ListExp from a list of effects that will be evaluated in parallel. If one fails, the whole expression
   * fails immediately.
   *
   * @param effects the list of effects
   * @param <O>     the type of the list effects
   * @return a ListExp
   */
  @SafeVarargs
  public static <O> ListExp<O> par(final IO<O>... effects) {

    var list = new ArrayList<IO<O>>();
    for (IO<O> effect : requireNonNull(effects)) {
      list.add(requireNonNull(effect));
    }
    return new ListExpPar<>(list,
                            null);
  }

  /**
   * Creates a ListExp from a list of effects that will be evaluated in parallel. If one fails, the whole expression
   * fails immediately.
   *
   * @param list the list of effects
   * @param <O>  the type of the list effects
   * @return a ListExp
   */
  public static <O> ListExp<O> par(final List<IO<O>> list) {
    return new ListExpPar<>(list,
                            null);
  }

  /**
   * Returns the size of the list.
   *
   * @return the number of effects
   */
  public int size() {
    return list.size();
  }

  /**
   * Creates a new list expression with the given effect appended to the end.
   *
   * @param effect the effect to append
   * @return a new list expression with the given effect appended
   */
  public abstract ListExp<Elem> append(final IO<Elem> effect);

  /**
   * Returns the first effect from the list that is evaluated successfully (no matter if some of them fail)
   *
   * @return the first effect that is evaluated
   */
  public IO<Elem> race() {
    return new Val<>(() -> {
      try (var scope = new StructuredTaskScope.ShutdownOnSuccess<Result<Elem>>()) {
        for (var task : list) {
          scope.fork(task);
        }
        try {
          return scope.join()
                      .result();
        } catch (Exception e) {
          return new Failure<>(e);
        }
      }
    });
  }

  /**
   * Checks if the list is empty.
   *
   * @return true if the list is empty, false otherwise
   */
  public boolean isEmpty() {
    return list.isEmpty();
  }

  /**
   * Returns the first effect from the list. Throws an IndexOutOfBoundsException if the list is empty.
   *
   * @return the first effect
   */
  public IO<Elem> head() {
    return list.getFirst();
  }

  /**
   * Returns all the effects in the list except the first one.
   *
   * @return a new list expression containing all effects except the first one
   */
  public abstract ListExp<Elem> tail();

  @Override
  public abstract ListExp<Elem> retryEach(final Predicate<? super Throwable> predicate,
                                          final RetryPolicy policy);

  @Override
  public ListExp<Elem> retryEach(final RetryPolicy policy) {
    return retryEach(_ -> true,
                     policy);
  }

  @Override
  public abstract ListExp<Elem> debugEach(final EventBuilder<List<Elem>> messageBuilder);

  @Override
  public abstract ListExp<Elem> debugEach(final String context);

}
