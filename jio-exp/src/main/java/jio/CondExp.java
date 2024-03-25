package jio;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents an expression made up of different test-effect branches and a default effect. Each branch consists of a
 * predicate (concretely a boolean effect) and an associated effect. The expression is reduced to the effect of the
 * first predicate that succeeds and is evaluated to true. If no predicate is evaluated to true, then the expression is
 * reduced to the default effect.
 * <p>
 * Predicates can be evaluated either in parallel with the static factory method {@code CondExp.par} or sequentially
 * with {@code CondExp.seq}.
 *
 * @param <Output> the type of the computation returned by this expression.
 */
public abstract sealed class CondExp<Output> extends Exp<Output> permits CondExpPar, CondExpSeq {

  CondExp(Function<EvalExpEvent, BiConsumer<Output, Throwable>> debugger) {
    super(debugger);
  }

  /**
   * It creates a `Cond` expression which predicates are computed in parallel. If one predicate fails, the whole
   * expression fails immediately with the same error. In case of success, all the predicates must end before returning
   * the result, and the expression is reduced to the effect of the fist predicate that is true, following the order
   * they are passed in the constructor. If none of the conditions evaluate to true, the expression reduces to the
   * otherwise parameter.
   *
   *
   * @param predicate1 the first predicate
   * @param effect1    the effect associated to the first predicate
   * @param predicate2 the second predicate
   * @param effect2    the effect associated to the second predicate
   * @param otherwise  the default effect, computed if all the predicates are false
   * @param <O>        the type of the computation result
   * @return a Cond expression
   */
  public static <O> CondExp<O> par(final IO<Boolean> predicate1,
                                   final Supplier<IO<O>> effect1,
                                   final IO<Boolean> predicate2,
                                   final Supplier<IO<O>> effect2,
                                   final Supplier<IO<O>> otherwise
                                  ) {

    return new CondExpPar<>(List.of(requireNonNull(predicate1),
                                    requireNonNull(predicate2)
                                   ),
                            List.of(requireNonNull(effect1),
                                    requireNonNull(effect2)
                                   ),
                            requireNonNull(otherwise),
                            null
    );

  }

  /**
   * It creates a `Cond` expression which predicates are computed in parallel. If one predicate fails, the whole
   * expression fails immediately with the same error. In case of success, all the predicates must end before returning
   * the result, and the expression is reduced to the effect of the fist predicate that is true, following the order
   * they are passed in the constructor. If none of the conditions evaluate to true, the expression reduces to the
   * {@link IO#NULL()} output.
   *
   * @param predicate1 the first predicate
   * @param effect1    the effect associated to the first predicate
   * @param predicate2 the second predicate
   * @param effect2    the effect associated to the second predicate
   * @param <O>        the type of the computation result
   * @return a Cond expression
   */
  public static <O> CondExp<O> par(final IO<Boolean> predicate1,
                                   final Supplier<IO<O>> effect1,
                                   final IO<Boolean> predicate2,
                                   final Supplier<IO<O>> effect2
                                  ) {

    return new CondExpPar<>(List.of(requireNonNull(predicate1),
                                    requireNonNull(predicate2)
                                   ),
                            List.of(requireNonNull(effect1),
                                    requireNonNull(effect2)
                                   ),
                            IO::NULL,
                            null
    );

  }

  /**
   * It creates a `Cond` expression which predicates are computed in parallel. If one predicate fails, the whole
   * expression fails immediately with the same error. In case of success, all the predicates must end before returning
   * the result, and the expression is reduced to the effect of the fist predicate that is true, following the order
   * they are passed in the constructor. If none of the conditions evaluate to true, the expression reduces to the
   * {@link IO#NULL()} output.
   *
   * @param predicate1 the first predicate
   * @param effect1    the effect associated to the first predicate
   * @param predicate2 the second predicate
   * @param effect2    the effect associated to the second predicate
   * @param predicate3 the third predicate
   * @param effect3    the effect associated to the third predicate
   * @param otherwise  the default effect, computed if all the predicates are false
   * @param <O>        the type of the computation result
   * @return a Cond expression
   */
  public static <O> CondExp<O> par(final IO<Boolean> predicate1,
                                   final Supplier<IO<O>> effect1,
                                   final IO<Boolean> predicate2,
                                   final Supplier<IO<O>> effect2,
                                   final IO<Boolean> predicate3,
                                   final Supplier<IO<O>> effect3,
                                   final Supplier<IO<O>> otherwise
                                  ) {

    return new CondExpPar<>(List.of(requireNonNull(predicate1),
                                    requireNonNull(predicate2),
                                    requireNonNull(predicate3)
                                   ),
                            List.of(requireNonNull(effect1),
                                    requireNonNull(effect2),
                                    requireNonNull(effect3)
                                   ),
                            requireNonNull(otherwise),
                            null
    );

  }

  /**
   * It creates a `Cond` expression which predicates are computed in parallel. If one predicate fails, the whole
   * expression fails immediately with the same error. In case of success, all the predicates must end before returning
   * the result, and the expression is reduced to the effect of the fist predicate that is true, following the order
   * they are passed in the constructor. If none of the conditions evaluate to true, the expression reduces to the
   * {@link IO#NULL()} output.
   *
   * @param predicate1 the first predicate
   * @param effect1    the effect associated to the first predicate
   * @param predicate2 the second predicate
   * @param effect2    the effect associated to the second predicate
   * @param predicate3 the third predicate
   * @param effect3    the effect associated to the third predicate
   * @param <O>        the type of the computation result
   * @return a Cond expression
   */
  public static <O> CondExp<O> par(final IO<Boolean> predicate1,
                                   final Supplier<IO<O>> effect1,
                                   final IO<Boolean> predicate2,
                                   final Supplier<IO<O>> effect2,
                                   final IO<Boolean> predicate3,
                                   final Supplier<IO<O>> effect3
                                  ) {

    return new CondExpPar<>(List.of(requireNonNull(predicate1),
                                    requireNonNull(predicate2),
                                    requireNonNull(predicate3)
                                   ),
                            List.of(requireNonNull(effect1),
                                    requireNonNull(effect2),
                                    requireNonNull(effect3)
                                   ),
                            IO::NULL,
                            null
    );

  }

  /**
   * It creates a `Cond` expression which predicates are computed in parallel. If one predicate fails, the whole
   * expression fails immediately with the same error. In case of success, all the predicates must end before returning
   * the result, and the expression is reduced to the effect of the fist predicate that is true, following the order
   * they are passed in the constructor. If none of the conditions evaluate to true, the expression reduces to the
   * otherwise parameter.
   *
   *
   * @param predicate1 the first predicate
   * @param effect1    the effect associated to the first predicate
   * @param predicate2 the second predicate
   * @param effect2    the effect associated to the second predicate
   * @param predicate3 the third predicate
   * @param effect3    the effect associated to the third predicate
   * @param predicate4 the forth predicate
   * @param effect4    the effect associated to the forth predicate
   * @param otherwise  the default effect, computed if all the predicates are false
   * @param <O>        the type of the computation result
   * @return a Cond expression
   */
  public static <O> CondExp<O> par(final IO<Boolean> predicate1,
                                   final Supplier<IO<O>> effect1,
                                   final IO<Boolean> predicate2,
                                   final Supplier<IO<O>> effect2,
                                   final IO<Boolean> predicate3,
                                   final Supplier<IO<O>> effect3,
                                   final IO<Boolean> predicate4,
                                   final Supplier<IO<O>> effect4,
                                   final Supplier<IO<O>> otherwise
                                  ) {

    return new CondExpPar<>(List.of(requireNonNull(predicate1),
                                    requireNonNull(predicate2),
                                    requireNonNull(predicate3),
                                    requireNonNull(predicate4)
                                   ),
                            List.of(requireNonNull(effect1),
                                    requireNonNull(effect2),
                                    requireNonNull(effect3),
                                    requireNonNull(effect4)
                                   ),
                            requireNonNull(otherwise),
                            null
    );
  }

  /**
   * It creates a `Cond` expression which predicates are computed in parallel. If one predicate fails, the whole
   * expression fails immediately with the same error. In case of success, all the predicates must end before returning
   * the result, and the expression is reduced to the effect of the fist predicate that is true, following the order
   * they are passed in the constructor. If none of the conditions evaluate to true, the expression reduces to the
   * {@link IO#NULL()} output.
   *
   * @param predicate1 the first predicate
   * @param effect1    the effect associated to the first predicate
   * @param predicate2 the second predicate
   * @param effect2    the effect associated to the second predicate
   * @param predicate3 the third predicate
   * @param effect3    the effect associated to the third predicate
   * @param predicate4 the forth predicate
   * @param effect4    the effect associated to the forth predicate
   * @param <O>        the type of the computation result
   * @return a Cond expression
   */
  public static <O> CondExp<O> par(final IO<Boolean> predicate1,
                                   final Supplier<IO<O>> effect1,
                                   final IO<Boolean> predicate2,
                                   final Supplier<IO<O>> effect2,
                                   final IO<Boolean> predicate3,
                                   final Supplier<IO<O>> effect3,
                                   final IO<Boolean> predicate4,
                                   final Supplier<IO<O>> effect4
                                  ) {

    return new CondExpPar<>(List.of(requireNonNull(predicate1),
                                    requireNonNull(predicate2),
                                    requireNonNull(predicate3),
                                    requireNonNull(predicate4)
                                   ),
                            List.of(requireNonNull(effect1),
                                    requireNonNull(effect2),
                                    requireNonNull(effect3),
                                    requireNonNull(effect4)
                                   ),
                            IO::NULL,
                            null
    );
  }

  /**
   * It creates a `Cond` expression which predicates are computed in parallel. If one predicate fails, the whole
   * expression fails immediately with the same error. In case of success, all the predicates must end before returning
   * the result, and the expression is reduced to the effect of the fist predicate that is true, following the order
   * they are passed in the constructor. If none of the conditions evaluate to true, the expression reduces to the
   * otherwise parameter.
   *
   *
   * @param predicate1 the first predicate
   * @param effect1    the effect associated to the first predicate
   * @param predicate2 the second predicate
   * @param effect2    the effect associated to the second predicate
   * @param predicate3 the third predicate
   * @param effect3    the effect associated to the third predicate
   * @param predicate4 the forth predicate
   * @param effect4    the effect associated to the forth predicate
   * @param predicate5 the fifth predicate
   * @param effect5    the effect associated to the fifth predicate
   * @param otherwise  the default effect, computed if all the predicates are false
   * @param <O>        the type of the computation result
   * @return a Cond expression
   */
  public static <O> CondExp<O> par(final IO<Boolean> predicate1,
                                   final Supplier<IO<O>> effect1,
                                   final IO<Boolean> predicate2,
                                   final Supplier<IO<O>> effect2,
                                   final IO<Boolean> predicate3,
                                   final Supplier<IO<O>> effect3,
                                   final IO<Boolean> predicate4,
                                   final Supplier<IO<O>> effect4,
                                   final IO<Boolean> predicate5,
                                   final Supplier<IO<O>> effect5,
                                   final Supplier<IO<O>> otherwise
                                  ) {
    return new CondExpPar<>(List.of(requireNonNull(predicate1),
                                    requireNonNull(predicate2),
                                    requireNonNull(predicate3),
                                    requireNonNull(predicate4),
                                    requireNonNull(predicate5)
                                   ),
                            List.of(requireNonNull(effect1),
                                    requireNonNull(effect2),
                                    requireNonNull(effect3),
                                    requireNonNull(effect4),
                                    requireNonNull(effect5)
                                   ),
                            requireNonNull(otherwise),
                            null
    );

  }

  /**
   * It creates a `Cond` expression which predicates are computed in parallel. If one predicate fails, the whole
   * expression fails immediately with the same error. In case of success, all the predicates must end before returning
   * the result, and the expression is reduced to the effect of the fist predicate that is true, following the order
   * they are passed in the constructor. If none of the conditions evaluate to true, the expression reduces to the
   * {@link IO#NULL()} output.
   *
   * @param predicate1 the first predicate
   * @param effect1    the effect associated to the first predicate
   * @param predicate2 the second predicate
   * @param effect2    the effect associated to the second predicate
   * @param predicate3 the third predicate
   * @param effect3    the effect associated to the third predicate
   * @param predicate4 the forth predicate
   * @param effect4    the effect associated to the forth predicate
   * @param predicate5 the fifth predicate
   * @param effect5    the effect associated to the fifth predicate
   * @param <O>        the type of the computation result
   * @return a Cond expression
   */
  public static <O> CondExp<O> par(final IO<Boolean> predicate1,
                                   final Supplier<IO<O>> effect1,
                                   final IO<Boolean> predicate2,
                                   final Supplier<IO<O>> effect2,
                                   final IO<Boolean> predicate3,
                                   final Supplier<IO<O>> effect3,
                                   final IO<Boolean> predicate4,
                                   final Supplier<IO<O>> effect4,
                                   final IO<Boolean> predicate5,
                                   final Supplier<IO<O>> effect5
                                  ) {
    return new CondExpPar<>(List.of(requireNonNull(predicate1),
                                    requireNonNull(predicate2),
                                    requireNonNull(predicate3),
                                    requireNonNull(predicate4),
                                    requireNonNull(predicate5)
                                   ),
                            List.of(requireNonNull(effect1),
                                    requireNonNull(effect2),
                                    requireNonNull(effect3),
                                    requireNonNull(effect4),
                                    requireNonNull(effect5)
                                   ),
                            IO::NULL,
                            null
    );

  }

  /**
   * It creates a `Cond` expression which predicates are computed in parallel. If one predicate fails, the whole
   * expression fails immediately with the same error. In case of success, all the predicates must end before returning
   * the result, and the expression is reduced to the effect of the fist predicate that is true, following the order
   * they are passed in the constructor. If none of the conditions evaluate to true, the expression reduces to the
   * otherwise parameter.
   *
   * @param predicate1 the first predicate
   * @param effect1    the effect associated to the first predicate
   * @param predicate2 the second predicate
   * @param effect2    the effect associated to the second predicate
   * @param predicate3 the third predicate
   * @param effect3    the effect associated to the third predicate
   * @param predicate4 the forth predicate
   * @param effect4    the effect associated to the forth predicate
   * @param predicate5 the fifth predicate
   * @param effect5    the effect associated to the fifth predicate
   * @param predicate6 the sixth predicate
   * @param effect6    the effect associated to the sixth predicate
   * @param otherwise  the default effect, returned if all predicates are evaluated to false
   * @param <O>        the type of the computation result
   * @return a Cond expression
   */
  public static <O> CondExp<O> par(final IO<Boolean> predicate1,
                                   final Supplier<IO<O>> effect1,
                                   final IO<Boolean> predicate2,
                                   final Supplier<IO<O>> effect2,
                                   final IO<Boolean> predicate3,
                                   final Supplier<IO<O>> effect3,
                                   final IO<Boolean> predicate4,
                                   final Supplier<IO<O>> effect4,
                                   final IO<Boolean> predicate5,
                                   final Supplier<IO<O>> effect5,
                                   final IO<Boolean> predicate6,
                                   final Supplier<IO<O>> effect6,
                                   final Supplier<IO<O>> otherwise
                                  ) {
    return new CondExpPar<>(List.of(requireNonNull(predicate1),
                                    requireNonNull(predicate2),
                                    requireNonNull(predicate3),
                                    requireNonNull(predicate4),
                                    requireNonNull(predicate5),
                                    requireNonNull(predicate6)
                                   ),
                            List.of(requireNonNull(effect1),
                                    requireNonNull(effect2),
                                    requireNonNull(effect3),
                                    requireNonNull(effect4),
                                    requireNonNull(effect5),
                                    requireNonNull(effect6)
                                   ),
                            requireNonNull(otherwise),
                            null
    );

  }

  /**
   * It creates a `Cond` expression which predicates are computed in parallel. If one predicate fails, the whole
   * expression fails immediately with the same error. In case of success, all the predicates must end before returning
   * the result, and the expression is reduced to the effect of the fist predicate that is true, following the order
   * they are passed in the constructor. If none of the conditions evaluate to true, the expression reduces to the
   * {@link IO#NULL()} output.
   *
   * @param predicate1 the first predicate
   * @param effect1    the effect associated to the first predicate
   * @param predicate2 the second predicate
   * @param effect2    the effect associated to the second predicate
   * @param predicate3 the third predicate
   * @param effect3    the effect associated to the third predicate
   * @param predicate4 the forth predicate
   * @param effect4    the effect associated to the forth predicate
   * @param predicate5 the fifth predicate
   * @param effect5    the effect associated to the fifth predicate
   * @param predicate6 the sixth predicate
   * @param effect6    the effect associated to the sixth predicate
   * @param <O>        the type of the computation result
   * @return a Cond expression
   */
  public static <O> CondExp<O> par(final IO<Boolean> predicate1,
                                   final Supplier<IO<O>> effect1,
                                   final IO<Boolean> predicate2,
                                   final Supplier<IO<O>> effect2,
                                   final IO<Boolean> predicate3,
                                   final Supplier<IO<O>> effect3,
                                   final IO<Boolean> predicate4,
                                   final Supplier<IO<O>> effect4,
                                   final IO<Boolean> predicate5,
                                   final Supplier<IO<O>> effect5,
                                   final IO<Boolean> predicate6,
                                   final Supplier<IO<O>> effect6
                                  ) {
    return new CondExpPar<>(List.of(requireNonNull(predicate1),
                                    requireNonNull(predicate2),
                                    requireNonNull(predicate3),
                                    requireNonNull(predicate4),
                                    requireNonNull(predicate5),
                                    requireNonNull(predicate6)
                                   ),
                            List.of(requireNonNull(effect1),
                                    requireNonNull(effect2),
                                    requireNonNull(effect3),
                                    requireNonNull(effect4),
                                    requireNonNull(effect5),
                                    requireNonNull(effect6)
                                   ),
                            IO::NULL,
                            null
    );

  }

  /**
   * It creates a `Cond` expression which predicates are computed sequentially, one after the other. If a predicates
   * succeed and is evaluated to true, the expression is reduced to its effect. Predicates are evaluated in the order
   * they are passed in the constructor. If all the predicates succeed and all are evaluated to false, the expression is
   * reduced to the specified default effect. If a predicate terminates with an exception, the expression fails.
   *
   * @param predicate1 the first predicate
   * @param effect1    the effect associated to the first predicate
   * @param predicate2 the second predicate
   * @param effect2    the effect associated to the second predicate
   * @param otherwise  the default effect, computed if all the predicates are false
   * @param <O>        the type of the computation result
   * @return a Cond expression
   */
  public static <O> CondExp<O> seq(final IO<Boolean> predicate1,
                                   final Supplier<IO<O>> effect1,
                                   final IO<Boolean> predicate2,
                                   final Supplier<IO<O>> effect2,
                                   final Supplier<IO<O>> otherwise
                                  ) {

    return new CondExpSeq<>(List.of(requireNonNull(predicate1),
                                    requireNonNull(predicate2)
                                   ),
                            List.of(requireNonNull(effect1),
                                    requireNonNull(effect2)
                                   ),
                            requireNonNull(otherwise),
                            null
    );

  }

  /**
   * It creates a Cond expression which predicates are computed sequentially, one after the other. If a predicates
   * succeed and is evaluated to true, the expression is reduced to its effect. Predicates are evaluated in the order
   * they are passed in the constructor. If all the predicates succeed and all are evaluated to false, the expression is
   * reduced to the specified default effect. If a predicate terminates with an exception, the expression fails.
   * <p>
   * If none of the conditions evaluate to true, the expression reduces to the {@link IO#NULL()} output.
   *
   * @param predicate1 the first predicate
   * @param effect1    the effect associated to the first predicate
   * @param predicate2 the second predicate
   * @param effect2    the effect associated to the second predicate
   * @param <O>        the type of the computation result
   * @return a Cond expression
   */
  public static <O> CondExp<O> seq(final IO<Boolean> predicate1,
                                   final Supplier<IO<O>> effect1,
                                   final IO<Boolean> predicate2,
                                   final Supplier<IO<O>> effect2
                                  ) {

    return new CondExpSeq<>(List.of(requireNonNull(predicate1),
                                    requireNonNull(predicate2)
                                   ),
                            List.of(requireNonNull(effect1),
                                    requireNonNull(effect2)
                                   ),
                            IO::NULL,
                            null
    );

  }

  /**
   * It creates a Cond expression which predicates are computed sequentially, one after the other. If a predicates
   * succeed and is evaluated to true, the expression is reduced to its effect. Predicates are evaluated in the order
   * they are passed in the constructor. If all the predicates succeed and all are evaluated to false, the expression is
   * reduced to the specified default effect. If a predicate terminates with an exception, the expression fails.
   *
   * @param predicate1 the first predicate
   * @param effect1    the effect associated to the first predicate
   * @param predicate2 the second predicate
   * @param effect2    the effect associated to the second predicate
   * @param predicate3 the third predicate
   * @param effect3    the effect associated to the third predicate
   * @param otherwise  the default effect, returned if all predicates are evaluated to false
   * @param <O>        the type of the computation result
   * @return a Cond expression
   */
  public static <O> CondExp<O> seq(final IO<Boolean> predicate1,
                                   final Supplier<IO<O>> effect1,
                                   final IO<Boolean> predicate2,
                                   final Supplier<IO<O>> effect2,
                                   final IO<Boolean> predicate3,
                                   final Supplier<IO<O>> effect3,
                                   final Supplier<IO<O>> otherwise
                                  ) {

    return new CondExpSeq<>(List.of(requireNonNull(predicate1),
                                    requireNonNull(predicate2),
                                    requireNonNull(predicate3)
                                   ),
                            List.of(requireNonNull(effect1),
                                    requireNonNull(effect2),
                                    requireNonNull(effect3)
                                   ),
                            requireNonNull(otherwise),
                            null
    );

  }

  /**
   * It creates a Cond expression which predicates are computed sequentially, one after the other. If a predicates
   * succeed and is evaluated to true, the expression is reduced to its effect. Predicates are evaluated in the order
   * they are passed in the constructor. If all the predicates succeed and all are evaluated to false, the expression is
   * reduced to the specified default effect. If a predicate terminates with an exception, the expression fails.
   * <p>
   * If none of the conditions evaluate to true, the expression reduces to the {@link IO#NULL()} output.
   *
   * @param predicate1 the first predicate
   * @param effect1    the effect associated to the first predicate
   * @param predicate2 the second predicate
   * @param effect2    the effect associated to the second predicate
   * @param predicate3 the third predicate
   * @param effect3    the effect associated to the third predicate
   * @param <O>        the type of the computation result
   * @return a Cond expression
   */
  public static <O> CondExp<O> seq(final IO<Boolean> predicate1,
                                   final Supplier<IO<O>> effect1,
                                   final IO<Boolean> predicate2,
                                   final Supplier<IO<O>> effect2,
                                   final IO<Boolean> predicate3,
                                   final Supplier<IO<O>> effect3
                                  ) {

    return new CondExpSeq<>(List.of(requireNonNull(predicate1),
                                    requireNonNull(predicate2),
                                    requireNonNull(predicate3)
                                   ),
                            List.of(requireNonNull(effect1),
                                    requireNonNull(effect2),
                                    requireNonNull(effect3)
                                   ),
                            IO::NULL,
                            null
    );

  }

  /**
   * It creates a Cond expression which predicates are computed sequentially, one after the other. If a predicates
   * succeed and is evaluated to true, the expression is reduced to its effect. Predicates are evaluated in the order
   * they are passed in the constructor. If all the predicates succeed and all are evaluated to false, the expression is
   * reduced to the specified default effect. If a predicate terminates with an exception, the expression fails.
   *
   * @param predicate1 the first predicate
   * @param effect1    the effect associated to the first predicate
   * @param predicate2 the second predicate
   * @param effect2    the effect associated to the second predicate
   * @param predicate3 the third predicate
   * @param effect3    the effect associated to the third predicate
   * @param predicate4 the forth predicate
   * @param effect4    the effect associated to the forth predicate
   * @param otherwise  the default effect, computed if all the predicates are false
   * @param <O>        the type of the computation result
   * @return a Cond expression
   */
  public static <O> CondExp<O> seq(final IO<Boolean> predicate1,
                                   final Supplier<IO<O>> effect1,
                                   final IO<Boolean> predicate2,
                                   final Supplier<IO<O>> effect2,
                                   final IO<Boolean> predicate3,
                                   final Supplier<IO<O>> effect3,
                                   final IO<Boolean> predicate4,
                                   final Supplier<IO<O>> effect4,
                                   final Supplier<IO<O>> otherwise
                                  ) {

    return new CondExpSeq<>(List.of(requireNonNull(predicate1),
                                    requireNonNull(predicate2),
                                    requireNonNull(predicate3),
                                    requireNonNull(predicate4)
                                   ),
                            List.of(requireNonNull(effect1),
                                    requireNonNull(effect2),
                                    requireNonNull(effect3),
                                    requireNonNull(effect4)
                                   ),
                            requireNonNull(otherwise),
                            null
    );

  }

  /**
   * It creates a Cond expression which predicates are computed sequentially, one after the other. If a predicates
   * succeed and is evaluated to true, the expression is reduced to its effect. Predicates are evaluated in the order
   * they are passed in the constructor. If all the predicates succeed and all are evaluated to false, the expression is
   * reduced to the specified default effect. If a predicate terminates with an exception, the expression fails.
   * <p>
   * If none of the conditions evaluate to true, the expression reduces to the {@link IO#NULL()} output.
   *
   * @param predicate1 the first predicate
   * @param effect1    the effect associated to the first predicate
   * @param predicate2 the second predicate
   * @param effect2    the effect associated to the second predicate
   * @param predicate3 the third predicate
   * @param effect3    the effect associated to the third predicate
   * @param predicate4 the forth predicate
   * @param effect4    the effect associated to the forth predicate
   * @param <O>        the type of the computation result
   * @return a Cond expression
   */
  public static <O> CondExp<O> seq(final IO<Boolean> predicate1,
                                   final Supplier<IO<O>> effect1,
                                   final IO<Boolean> predicate2,
                                   final Supplier<IO<O>> effect2,
                                   final IO<Boolean> predicate3,
                                   final Supplier<IO<O>> effect3,
                                   final IO<Boolean> predicate4,
                                   final Supplier<IO<O>> effect4
                                  ) {

    return new CondExpSeq<>(List.of(requireNonNull(predicate1),
                                    requireNonNull(predicate2),
                                    requireNonNull(predicate3),
                                    requireNonNull(predicate4)
                                   ),
                            List.of(requireNonNull(effect1),
                                    requireNonNull(effect2),
                                    requireNonNull(effect3),
                                    requireNonNull(effect4)
                                   ),
                            IO::NULL,
                            null
    );

  }

  /**
   * It creates a Cond expression which predicates are computed sequentially, one after the other. If a predicates
   * succeed and is evaluated to true, the expression is reduced to its effect. Predicates are evaluated in the order
   * they are passed in the constructor. If all the predicates succeed and all are evaluated to false, the expression is
   * reduced to the specified default effect. If a predicate terminates with an exception, the expression fails.
   *
   * @param predicate1 the first predicate
   * @param effect1    the effect associated to the first predicate
   * @param predicate2 the second predicate
   * @param effect2    the effect associated to the second predicate
   * @param predicate3 the third predicate
   * @param effect3    the effect associated to the third predicate
   * @param predicate4 the forth predicate
   * @param effect4    the effect associated to the forth predicate
   * @param predicate5 the fifth predicate
   * @param effect5    the effect associated to the fifth predicate
   * @param otherwise  the default effect, computed if all the predicates are false
   * @param <O>        the type of the computation result
   * @return a Cond expression
   */
  public static <O> CondExp<O> seq(final IO<Boolean> predicate1,
                                   final Supplier<IO<O>> effect1,
                                   final IO<Boolean> predicate2,
                                   final Supplier<IO<O>> effect2,
                                   final IO<Boolean> predicate3,
                                   final Supplier<IO<O>> effect3,
                                   final IO<Boolean> predicate4,
                                   final Supplier<IO<O>> effect4,
                                   final IO<Boolean> predicate5,
                                   final Supplier<IO<O>> effect5,
                                   final Supplier<IO<O>> otherwise
                                  ) {

    return new CondExpSeq<>(List.of(requireNonNull(predicate1),
                                    requireNonNull(predicate2),
                                    requireNonNull(predicate3),
                                    requireNonNull(predicate4),
                                    requireNonNull(predicate5)
                                   ),
                            List.of(requireNonNull(effect1),
                                    requireNonNull(effect2),
                                    requireNonNull(effect3),
                                    requireNonNull(effect4),
                                    requireNonNull(effect5)
                                   ),
                            requireNonNull(otherwise),
                            null
    );

  }

  /**
   * It creates a Cond expression which predicates are computed sequentially, one after the other. If a predicates
   * succeed and is evaluated to true, the expression is reduced to its effect. Predicates are evaluated in the order
   * they are passed in the constructor. If all the predicates succeed and all are evaluated to false, the expression is
   * reduced to the specified default effect. If a predicate terminates with an exception, the expression fails.
   * <p>
   * If none of the conditions evaluate to true, the expression reduces to the {@link IO#NULL()} output.
   *
   * @param predicate1 the first predicate
   * @param effect1    the effect associated to the first predicate
   * @param predicate2 the second predicate
   * @param effect2    the effect associated to the second predicate
   * @param predicate3 the third predicate
   * @param effect3    the effect associated to the third predicate
   * @param predicate4 the forth predicate
   * @param effect4    the effect associated to the forth predicate
   * @param predicate5 the fifth predicate
   * @param effect5    the effect associated to the fifth predicate
   * @param <O>        the type of the computation result
   * @return a Cond expression
   */
  public static <O> CondExp<O> seq(final IO<Boolean> predicate1,
                                   final Supplier<IO<O>> effect1,
                                   final IO<Boolean> predicate2,
                                   final Supplier<IO<O>> effect2,
                                   final IO<Boolean> predicate3,
                                   final Supplier<IO<O>> effect3,
                                   final IO<Boolean> predicate4,
                                   final Supplier<IO<O>> effect4,
                                   final IO<Boolean> predicate5,
                                   final Supplier<IO<O>> effect5
                                  ) {

    return new CondExpSeq<>(List.of(requireNonNull(predicate1),
                                    requireNonNull(predicate2),
                                    requireNonNull(predicate3),
                                    requireNonNull(predicate4),
                                    requireNonNull(predicate5)
                                   ),
                            List.of(requireNonNull(effect1),
                                    requireNonNull(effect2),
                                    requireNonNull(effect3),
                                    requireNonNull(effect4),
                                    requireNonNull(effect5)
                                   ),
                            IO::NULL,
                            null
    );

  }

  /**
   * It creates a Cond expression which predicates are computed sequentially, one after the other. If a predicates
   * succeeds and is evaluated to true, the expression is reduced to its associated effect. Predicates are evaluated in
   * the order they are passed in the constructor. If all the predicates succeeds and all are evaluated to false, the
   * expression is reduced to the specified default effect. If a predicate terminates with an exception, the expression
   * fails.
   *
   * @param predicate1 the first predicate
   * @param effect1    the effect associated to the first predicate
   * @param predicate2 the second predicate
   * @param effect2    the effect associated to the second predicate
   * @param predicate3 the third predicate
   * @param effect3    the effect associated to the third predicate
   * @param predicate4 the forth predicate
   * @param effect4    the effect associated to the forth predicate
   * @param predicate5 the fifth predicate
   * @param effect5    the effect associated to the fifth predicate
   * @param predicate6 the sixth predicate
   * @param effect6    the effect associated to the sixth predicate
   * @param otherwise  the default effect, computed if all the predicates are false
   * @param <O>        the type of the computation result
   * @return a Cond expression
   */
  public static <O> CondExp<O> seq(final IO<Boolean> predicate1,
                                   final Supplier<IO<O>> effect1,
                                   final IO<Boolean> predicate2,
                                   final Supplier<IO<O>> effect2,
                                   final IO<Boolean> predicate3,
                                   final Supplier<IO<O>> effect3,
                                   final IO<Boolean> predicate4,
                                   final Supplier<IO<O>> effect4,
                                   final IO<Boolean> predicate5,
                                   final Supplier<IO<O>> effect5,
                                   final IO<Boolean> predicate6,
                                   final Supplier<IO<O>> effect6,
                                   final Supplier<IO<O>> otherwise
                                  ) {

    return new CondExpSeq<>(List.of(requireNonNull(predicate1),
                                    requireNonNull(predicate2),
                                    requireNonNull(predicate3),
                                    requireNonNull(predicate4),
                                    requireNonNull(predicate5),
                                    requireNonNull(predicate6)
                                   ),
                            List.of(requireNonNull(effect1),
                                    requireNonNull(effect2),
                                    requireNonNull(effect3),
                                    requireNonNull(effect4),
                                    requireNonNull(effect5),
                                    requireNonNull(effect6)
                                   ),
                            requireNonNull(otherwise),
                            null
    );
  }

  /**
   * It creates a Cond expression which predicates are computed sequentially, one after the other. If a predicates
   * succeeds and is evaluated to true, the expression is reduced to its associated effect. Predicates are evaluated in
   * the order they are passed in the constructor. If all the predicates succeeds and all are evaluated to false, the
   * expression is reduced to the specified default effect. If a predicate terminates with an exception, the expression
   * fails.
   * <p>
   * If none of the conditions evaluate to true, the expression reduces to the {@link IO#NULL()} output.
   *
   * @param predicate1 the first predicate
   * @param effect1    the effect associated to the first predicate
   * @param predicate2 the second predicate
   * @param effect2    the effect associated to the second predicate
   * @param predicate3 the third predicate
   * @param effect3    the effect associated to the third predicate
   * @param predicate4 the forth predicate
   * @param effect4    the effect associated to the forth predicate
   * @param predicate5 the fifth predicate
   * @param effect5    the effect associated to the fifth predicate
   * @param predicate6 the sixth predicate
   * @param effect6    the effect associated to the sixth predicate
   * @param <O>        the type of the computation result
   * @return a Cond expression
   */
  public static <O> CondExp<O> seq(final IO<Boolean> predicate1,
                                   final Supplier<IO<O>> effect1,
                                   final IO<Boolean> predicate2,
                                   final Supplier<IO<O>> effect2,
                                   final IO<Boolean> predicate3,
                                   final Supplier<IO<O>> effect3,
                                   final IO<Boolean> predicate4,
                                   final Supplier<IO<O>> effect4,
                                   final IO<Boolean> predicate5,
                                   final Supplier<IO<O>> effect5,
                                   final IO<Boolean> predicate6,
                                   final Supplier<IO<O>> effect6
                                  ) {

    return new CondExpSeq<>(List.of(requireNonNull(predicate1),
                                    requireNonNull(predicate2),
                                    requireNonNull(predicate3),
                                    requireNonNull(predicate4),
                                    requireNonNull(predicate5),
                                    requireNonNull(predicate6)
                                   ),
                            List.of(requireNonNull(effect1),
                                    requireNonNull(effect2),
                                    requireNonNull(effect3),
                                    requireNonNull(effect4),
                                    requireNonNull(effect5),
                                    requireNonNull(effect6)
                                   ),
                            IO::NULL,
                            null
    );
  }

  @Override
  public abstract CondExp<Output> retryEach(final Predicate<? super Throwable> predicate,
                                            final RetryPolicy policy
                                           );

  @Override
  public abstract CondExp<Output> debugEach(final EventBuilder<Output> messageBuilder);

  @Override
  public abstract CondExp<Output> debugEach(final String context);

  @Override
  public CondExp<Output> retryEach(final RetryPolicy policy) {
    return retryEach(e -> true,
                     policy);
  }

}
