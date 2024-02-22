package jio;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Predicate;

/**
 * Represents the result of evaluating an effect with the method {@link SwitchExp#eval(Object)} of a SwitchExp. This
 * result will be matched against different branches
 *
 * <pre>
 * {@code
 *           SwitchMatcher<Integer,String> matcher = SwitchExp.eval(IO.succeed(2));
 *           SwitchExp<I,O> exp = matcher.match(1, i -> IO.succeed("one"),
 *                                              2, i -> IO.succeed("two"),
 *                                              i -> IO.succeed("default")
 *                                             );
 *
 *           // or just in one
 *
 *           SwitchExp<I,O> exp = SwitchExp.<Integer, String>eval(IO.succeed(2))
 *                                         .match(1, i -> IO.succeed("one"),
 *                                                2, i -> IO.succeed("two"),
 *                                                i -> IO.succeed("default")
 *                                               )
 *
 * }
 * </pre>
 *
 * @param <Input>  the type of the value to be evaluated
 * @param <Output> the type of returned value of the expression
 */
public final class SwitchMatcher<Input, Output> {

  private final IO<Input> val;

  SwitchMatcher(IO<Input> val) {
    this.val = val;
  }

  /**
   * Matcher made up of two branches and a default effect. Each branch consists of a value that will be used to match
   * the result with the <code>equals</code> method, and an associated lambda that will be computed in case of success.
   * Branches predicates are evaluated sequentially.
   *
   * @param pattern1  the first value
   * @param lambda1   the lambda associated to the first value
   * @param pattern2  the second value
   * @param lambda2   the lambda associated to the second value
   * @param otherwise the default lambda, evaluated if no branch is matched
   * @return a SwitchExp
   */
  public SwitchExp<Input, Output> match(final Result<Input> pattern1,
                                        final Lambda<Result<Input>, Output> lambda1,
                                        final Result<Input> pattern2,
                                        final Lambda<Result<Input>, Output> lambda2,
                                        final Lambda<Result<Input>, Output> otherwise
                                       ) {
    requireNonNull(pattern1);
    requireNonNull(pattern2);
    return new SwitchExp<>(val,
                           List.of(input -> input.equals(pattern1),
                                   input -> input.equals(pattern2)
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2)),
                           requireNonNull(otherwise),
                           null);
  }

  /**
   * Matcher made up of two branches. Each branch consists of a value that will be used to match the result with the
   * <code>equals</code> method, and an associated lambda that will be computed in case of success.
   * Branches predicates are evaluated sequentially. If no pattern is tested true, the expression is reduced to the null
   * effect {@link IO#NULL()}
   *
   * @param pattern1 the first value
   * @param lambda1  the lambda associated to the first value
   * @param pattern2 the second value
   * @param lambda2  the lambda associated to the second value
   * @return a SwitchExp
   */
  public SwitchExp<Input, Output> match(final Result<Input> pattern1,
                                        final Lambda<Result<Input>, Output> lambda1,
                                        final Result<Input> pattern2,
                                        final Lambda<Result<Input>, Output> lambda2
                                       ) {
    requireNonNull(pattern1);
    requireNonNull(pattern2);
    return new SwitchExp<>(val,
                           List.of(input -> input.equals(pattern1),
                                   input -> input.equals(pattern2)
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2)),
                           _ -> IO.NULL(),
                           null);
  }

  /**
   * Matcher made up of two branches and a default effect. Each branch consists of a predicate that will be used to test
   * the result, and an associated lambda that will be computed in case of the predicate returns true. Branches
   * predicates are evaluated sequentially in the order they are passed in the method.
   *
   * @param pattern1  the first value
   * @param lambda1   the lambda associated to the first value
   * @param pattern2  the second value
   * @param lambda2   the lambda associated to the second value
   * @param otherwise the default lambda, evaluated if no branch is matched
   * @return a SwitchExp
   */
  public SwitchExp<Input, Output> matchPredicate(final Predicate<Result<Input>> pattern1,
                                                 final Lambda<Result<Input>, Output> lambda1,
                                                 final Predicate<Result<Input>> pattern2,
                                                 final Lambda<Result<Input>, Output> lambda2,
                                                 final Lambda<Result<Input>, Output> otherwise
                                                ) {

    return new SwitchExp<>(val,
                           List.of(requireNonNull(pattern1),
                                   requireNonNull(pattern2)
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2)),
                           requireNonNull(otherwise),
                           null
    );
  }

  /**
   * Matcher made up of two branches. Each branch consists of a predicate that will be used to test the result, and an
   * associated lambda that will be computed in case of the predicate returns true. Branches predicates are evaluated
   * sequentially in the order they are passed in the method. If no predicate is tested true, the expression is reduced
   * to the null effect {@link IO#NULL()}
   *
   * @param pattern1 the first value
   * @param lambda1  the lambda associated to the first value
   * @param pattern2 the second value
   * @param lambda2  the lambda associated to the second value
   * @return a SwitchExp
   * @see #matchPredicate(Predicate, Lambda, Predicate, Lambda, Lambda)
   */
  public SwitchExp<Input, Output> matchPredicate(final Predicate<Result<Input>> pattern1,
                                                 final Lambda<Result<Input>, Output> lambda1,
                                                 final Predicate<Result<Input>> pattern2,
                                                 final Lambda<Result<Input>, Output> lambda2
                                                ) {

    return new SwitchExp<>(val,
                           List.of(requireNonNull(pattern1),
                                   requireNonNull(pattern2)
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2)),
                           _ -> IO.NULL(),
                           null
    );
  }

  /**
   * Matcher made up of four branches and a default effect. Each branch consists of a list of values that will be used
   * to match the result with the <code>contains</code> method, and an associated lambda that will be computed in case
   * of success. Branches predicates are evaluated sequentially.
   *
   * @param pattern1  the first list
   * @param lambda1   the lambda associated to the first value
   * @param pattern2  the second list
   * @param lambda2   the lambda associated to the second value
   * @param otherwise the default lambda, evaluated if no branch is matched
   * @return a SwitchExp
   */
  public SwitchExp<Input, Output> matchList(final List<? extends Result<Input>> pattern1,
                                            final Lambda<Result<Input>, Output> lambda1,
                                            final List<? extends Result<Input>> pattern2,
                                            final Lambda<Result<Input>, Output> lambda2,
                                            final Lambda<Result<Input>, Output> otherwise
                                           ) {
    requireNonNull(pattern1);
    requireNonNull(pattern2);
    return new SwitchExp<>(val,
                           List.of(pattern1::contains,
                                   pattern2::contains
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2)),
                           requireNonNull(otherwise),
                           null
    );
  }

  /**
   * Matcher made up of four branches. Each branch consists of a list of values that will be used to match the result
   * with the <code>contains</code> method, and an associated lambda that will be computed in case of success. Branches
   * predicates are evaluated sequentially. If no pattern is tested true, the expression is reduced to the null effect
   * {@link IO#NULL()}
   *
   * @param pattern1 the first list
   * @param lambda1  the lambda associated to the first value
   * @param pattern2 the second list
   * @param lambda2  the lambda associated to the second value
   * @return a SwitchExp
   * @see #matchList(List, Lambda, List, Lambda, Lambda)
   */
  public SwitchExp<Input, Output> matchList(final List<? extends Result<Input>> pattern1,
                                            final Lambda<Result<Input>, Output> lambda1,
                                            final List<? extends Result<Input>> pattern2,
                                            final Lambda<Result<Input>, Output> lambda2
                                           ) {
    requireNonNull(pattern1);
    requireNonNull(pattern2);
    return new SwitchExp<>(val,
                           List.of(pattern1::contains,
                                   pattern2::contains
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2)),
                           _ -> IO.NULL(),
                           null
    );
  }

  /**
   * Matcher made up of four branches and a default effect. Each branch consists of a list of values that will be used
   * to match the result with the <code>contains</code> method, and an associated lambda that will be computed in case
   * of success. Branches predicates are evaluated sequentially.
   *
   * @param pattern1  the first list
   * @param lambda1   the lambda associated to the first value
   * @param pattern2  the second list
   * @param lambda2   the lambda associated to the second value
   * @param pattern3  the third list
   * @param lambda3   the lambda associated to the third value
   * @param otherwise the default lambda, evaluated if no branch is matched
   * @return a SwitchExp
   */
  public SwitchExp<Input, Output> matchList(final List<? extends Result<Input>> pattern1,
                                            final Lambda<Result<Input>, Output> lambda1,
                                            final List<? extends Result<Input>> pattern2,
                                            final Lambda<Result<Input>, Output> lambda2,
                                            final List<? extends Result<Input>> pattern3,
                                            final Lambda<Result<Input>, Output> lambda3,
                                            final Lambda<Result<Input>, Output> otherwise
                                           ) {
    requireNonNull(pattern1);
    requireNonNull(pattern2);
    requireNonNull(pattern3);
    return new SwitchExp<>(val,
                           List.of(pattern1::contains,
                                   pattern2::contains,
                                   pattern3::contains
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2),
                                   requireNonNull(lambda3)
                                  ),
                           requireNonNull(otherwise),
                           null
    );
  }

  /**
   * Matcher made up of four branches. Each branch consists of a list of values that will be used to match the result
   * with the <code>contains</code> method, and an associated lambda that will be computed in case of success. Branches
   * predicates are evaluated sequentially. If no pattern is tested true, the expression is reduced to the null effect
   * {@link IO#NULL()}
   *
   * @param pattern1 the first list
   * @param lambda1  the lambda associated to the first value
   * @param pattern2 the second list
   * @param lambda2  the lambda associated to the second value
   * @param pattern3 the third list
   * @param lambda3  the lambda associated to the third value
   * @return a SwitchExp
   * @see #matchList(List, Lambda, List, Lambda, List, Lambda, Lambda)
   */
  public SwitchExp<Input, Output> matchList(final List<? extends Result<Input>> pattern1,
                                            final Lambda<Result<Input>, Output> lambda1,
                                            final List<? extends Result<Input>> pattern2,
                                            final Lambda<Result<Input>, Output> lambda2,
                                            final List<? extends Result<Input>> pattern3,
                                            final Lambda<Result<Input>, Output> lambda3
                                           ) {
    requireNonNull(pattern1);
    requireNonNull(pattern2);
    requireNonNull(pattern3);
    return new SwitchExp<>(val,
                           List.of(pattern1::contains,
                                   pattern2::contains,
                                   pattern3::contains
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2),
                                   requireNonNull(lambda3)
                                  ),
                           _ -> IO.NULL(),
                           null
    );
  }

  /**
   * Matcher made up of three branches and a default effect. Each branch consists of a value that will be used to match
   * the result with the <code>equals</code> method, and an associated lambda that will be computed in case of success.
   * Branches predicates are evaluated sequentially.
   *
   * @param pattern1  the first value
   * @param lambda1   the lambda associated to the first value
   * @param pattern2  the second value
   * @param lambda2   the lambda associated to the second value
   * @param pattern3  the third value
   * @param lambda3   the lambda associated to the third value
   * @param otherwise the default lambda, evaluated if no branch is matched
   * @return a SwitchExp
   */
  public SwitchExp<Input, Output> match(final Result<Input> pattern1,
                                        final Lambda<Result<Input>, Output> lambda1,
                                        final Result<Input> pattern2,
                                        final Lambda<Result<Input>, Output> lambda2,
                                        final Result<Input> pattern3,
                                        final Lambda<Result<Input>, Output> lambda3,
                                        final Lambda<Result<Input>, Output> otherwise
                                       ) {
    requireNonNull(pattern1);
    requireNonNull(pattern2);
    requireNonNull(pattern3);
    return new SwitchExp<>(val,
                           List.of(input -> input.equals(pattern1),
                                   input -> input.equals(pattern2),
                                   input -> input.equals(pattern3)
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2),
                                   requireNonNull(lambda3)
                                  ),
                           otherwise,
                           null
    );

  }

  /**
   * Matcher made up of three branches. Each branch consists of a value that will be used to match the result with the
   * <code>equals</code> method, and an associated lambda that will be computed in case of success.
   * Branches predicates are evaluated sequentially. If no pattern is tested true, the expression is reduced to the null
   * effect {@link IO#NULL()}
   *
   * @param pattern1 the first value
   * @param lambda1  the lambda associated to the first value
   * @param pattern2 the second value
   * @param lambda2  the lambda associated to the second value
   * @param pattern3 the third value
   * @param lambda3  the lambda associated to the third value
   * @return a SwitchExp *
   */
  public SwitchExp<Input, Output> match(final Result<Input> pattern1,
                                        final Lambda<Result<Input>, Output> lambda1,
                                        final Result<Input> pattern2,
                                        final Lambda<Result<Input>, Output> lambda2,
                                        final Result<Input> pattern3,
                                        final Lambda<Result<Input>, Output> lambda3
                                       ) {
    requireNonNull(pattern1);
    requireNonNull(pattern2);
    requireNonNull(pattern3);
    return new SwitchExp<>(val,
                           List.of(input -> input.equals(pattern1),
                                   input -> input.equals(pattern2),
                                   input -> input.equals(pattern3)
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2),
                                   requireNonNull(lambda3)
                                  ),
                           _ -> IO.NULL(),
                           null
    );

  }

  /**
   * Matcher made up of three branches and a default effect. Each branch consists of a predicate that will be used to
   * test the result, and an associated lambda that will be computed in case of the predicate returns true. Branches
   * predicates are evaluated sequentially in the order they are passed in the method.
   *
   * @param pattern1  the first value
   * @param lambda1   the lambda associated to the first value
   * @param pattern2  the second value
   * @param lambda2   the lambda associated to the second value
   * @param pattern3  the third value
   * @param lambda3   the lambda associated to the third value
   * @param otherwise the default lambda, evaluated if no branch is matched
   * @return a SwitchExp
   */
  public SwitchExp<Input, Output> matchPredicate(final Predicate<Result<Input>> pattern1,
                                                 final Lambda<Result<Input>, Output> lambda1,
                                                 final Predicate<Result<Input>> pattern2,
                                                 final Lambda<Result<Input>, Output> lambda2,
                                                 final Predicate<Result<Input>> pattern3,
                                                 final Lambda<Result<Input>, Output> lambda3,
                                                 final Lambda<Result<Input>, Output> otherwise
                                                ) {

    return new SwitchExp<>(val,
                           List.of(requireNonNull(pattern1),
                                   requireNonNull(pattern2),
                                   requireNonNull(pattern3)
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2),
                                   requireNonNull(lambda3)
                                  ),
                           requireNonNull(otherwise),
                           null
    );

  }

  /**
   * Matcher made up of three branches. Each branch consists of a predicate that will be used to test the result, and an
   * associated lambda that will be computed in case of the predicate returns true. Branches predicates are evaluated
   * sequentially in the order they are passed in the method.If no pattern is tested true, the expression is * reduced
   * to the null effect {@link IO#NULL()}
   *
   * @param pattern1 the first value
   * @param lambda1  the lambda associated to the first value
   * @param pattern2 the second value
   * @param lambda2  the lambda associated to the second value
   * @param pattern3 the third value
   * @param lambda3  the lambda associated to the third value
   * @return a SwitchExp
   * @see #matchPredicate(Predicate, Lambda, Predicate, Lambda, Predicate, Lambda, Lambda)
   */
  public SwitchExp<Input, Output> matchPredicate(final Predicate<Result<Input>> pattern1,
                                                 final Lambda<Result<Input>, Output> lambda1,
                                                 final Predicate<Result<Input>> pattern2,
                                                 final Lambda<Result<Input>, Output> lambda2,
                                                 final Predicate<Result<Input>> pattern3,
                                                 final Lambda<Result<Input>, Output> lambda3
                                                ) {

    return new SwitchExp<>(val,
                           List.of(requireNonNull(pattern1),
                                   requireNonNull(pattern2),
                                   requireNonNull(pattern3)
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2),
                                   requireNonNull(lambda3)
                                  ),
                           _ -> IO.NULL(),
                           null
    );

  }

  /**
   * Matcher made up of four branches and a default effect. Each branch consists of a value that will be used to match
   * the result with the <code>equals</code> method, and an associated lambda that will be computed in case of success.
   * Branches predicates are evaluated sequentially.
   *
   * @param pattern1  the first value
   * @param lambda1   the lambda associated to the first value
   * @param pattern2  the second value
   * @param lambda2   the lambda associated to the second value
   * @param pattern3  the third value
   * @param lambda3   the lambda associated to the third value
   * @param pattern4  the forth value
   * @param lambda4   the lambda associated to the forth value
   * @param otherwise the default lambda, evaluated if no branch is matched
   * @return a SwitchExp
   */
  public SwitchExp<Input, Output> match(final Result<Input> pattern1,
                                        final Lambda<Result<Input>, Output> lambda1,
                                        final Result<Input> pattern2,
                                        final Lambda<Result<Input>, Output> lambda2,
                                        final Result<Input> pattern3,
                                        final Lambda<Result<Input>, Output> lambda3,
                                        final Result<Input> pattern4,
                                        final Lambda<Result<Input>, Output> lambda4,
                                        final Lambda<Result<Input>, Output> otherwise
                                       ) {
    requireNonNull(pattern1);
    requireNonNull(pattern2);
    requireNonNull(pattern3);
    requireNonNull(pattern4);

    return new SwitchExp<>(val,
                           List.of(input -> input.equals(pattern1),
                                   input -> input.equals(pattern2),
                                   input -> input.equals(pattern3),
                                   input -> input.equals(pattern4)
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2),
                                   requireNonNull(lambda3),
                                   requireNonNull(lambda4)
                                  ),
                           requireNonNull(otherwise),
                           null
    );
  }

  /**
   * Matcher made up of four branches. Each branch consists of a value that will be used to match the result with the
   * <code>equals</code> method, and an associated lambda that will be computed in case of success.
   * Branches predicates are evaluated sequentially.If no pattern is tested true, the expression is * reduced to the
   * null effect {@link IO#NULL()}
   *
   * @param pattern1 the first value
   * @param lambda1  the lambda associated to the first value
   * @param pattern2 the second value
   * @param lambda2  the lambda associated to the second value
   * @param pattern3 the third value
   * @param lambda3  the lambda associated to the third value
   * @param pattern4 the forth value
   * @param lambda4  the lambda associated to the forth value
   * @return a SwitchExp
   */
  public SwitchExp<Input, Output> match(final Result<Input> pattern1,
                                        final Lambda<Result<Input>, Output> lambda1,
                                        final Result<Input> pattern2,
                                        final Lambda<Result<Input>, Output> lambda2,
                                        final Result<Input> pattern3,
                                        final Lambda<Result<Input>, Output> lambda3,
                                        final Result<Input> pattern4,
                                        final Lambda<Result<Input>, Output> lambda4
                                       ) {
    requireNonNull(pattern1);
    requireNonNull(pattern2);
    requireNonNull(pattern3);
    requireNonNull(pattern4);

    return new SwitchExp<>(val,
                           List.of(input -> input.equals(pattern1),
                                   input -> input.equals(pattern2),
                                   input -> input.equals(pattern3),
                                   input -> input.equals(pattern4)
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2),
                                   requireNonNull(lambda3),
                                   requireNonNull(lambda4)
                                  ),
                           _ -> IO.NULL(),
                           null
    );
  }

  /**
   * Matcher made up of four branches and a default effect. Each branch consists of a predicate that will be used to
   * test the result, and an associated lambda that will be computed in case of the predicate returns true. Branches
   * predicates are evaluated sequentially in the order they are passed in the method.
   *
   * @param pattern1  the first value
   * @param lambda1   the lambda associated to the first value
   * @param pattern2  the second value
   * @param lambda2   the lambda associated to the second value
   * @param pattern3  the third value
   * @param lambda3   the lambda associated to the third value
   * @param pattern4  the forth value
   * @param lambda4   the lambda associated to the forth value
   * @param otherwise the default lambda, evaluated if no branch is matched
   * @return a SwitchExp
   */
  public SwitchExp<Input, Output> matchPredicate(final Predicate<Result<Input>> pattern1,
                                                 final Lambda<Result<Input>, Output> lambda1,
                                                 final Predicate<Result<Input>> pattern2,
                                                 final Lambda<Result<Input>, Output> lambda2,
                                                 final Predicate<Result<Input>> pattern3,
                                                 final Lambda<Result<Input>, Output> lambda3,
                                                 final Predicate<Result<Input>> pattern4,
                                                 final Lambda<Result<Input>, Output> lambda4,
                                                 final Lambda<Result<Input>, Output> otherwise
                                                ) {

    return new SwitchExp<>(val,
                           List.of(requireNonNull(pattern1),
                                   requireNonNull(pattern2),
                                   requireNonNull(pattern3),
                                   requireNonNull(pattern4)
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2),
                                   requireNonNull(lambda3),
                                   requireNonNull(lambda4)
                                  ),
                           requireNonNull(otherwise),
                           null
    );

  }

  /**
   * Matcher made up of four branches. Each branch consists of a predicate that will be used to test the result, and an
   * associated lambda that will be computed in case of the predicate returns true. Branches predicates are evaluated
   * sequentially in the order they are passed in the method. If no pattern is tested true, the expression is * reduced
   * to the null effect {@link IO#NULL()}
   *
   * @param pattern1 the first value
   * @param lambda1  the lambda associated to the first value
   * @param pattern2 the second value
   * @param lambda2  the lambda associated to the second value
   * @param pattern3 the third value
   * @param lambda3  the lambda associated to the third value
   * @param pattern4 the forth value
   * @param lambda4  the lambda associated to the forth value
   * @return a SwitchExp
   * @see #matchPredicate(Predicate, Lambda, Predicate, Lambda, Predicate, Lambda, Predicate, Lambda, Lambda)
   */
  public SwitchExp<Input, Output> matchPredicate(final Predicate<Result<Input>> pattern1,
                                                 final Lambda<Result<Input>, Output> lambda1,
                                                 final Predicate<Result<Input>> pattern2,
                                                 final Lambda<Result<Input>, Output> lambda2,
                                                 final Predicate<Result<Input>> pattern3,
                                                 final Lambda<Result<Input>, Output> lambda3,
                                                 final Predicate<Result<Input>> pattern4,
                                                 final Lambda<Result<Input>, Output> lambda4
                                                ) {

    return new SwitchExp<>(val,
                           List.of(requireNonNull(pattern1),
                                   requireNonNull(pattern2),
                                   requireNonNull(pattern3),
                                   requireNonNull(pattern4)
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2),
                                   requireNonNull(lambda3),
                                   requireNonNull(lambda4)
                                  ),
                           _ -> IO.NULL(),
                           null
    );

  }

  /**
   * Matcher made up of four branches and a default effect. Each branch consists of a list of values that will be used
   * to match the result with the <code>contains</code> method, and an associated lambda that will be computed in case
   * of success. Branches predicates are evaluated sequentially.
   *
   * @param pattern1  the first list
   * @param lambda1   the lambda associated to the first value
   * @param pattern2  the second list
   * @param lambda2   the lambda associated to the second value
   * @param pattern3  the third list
   * @param lambda3   the lambda associated to the third value
   * @param pattern4  the forth list
   * @param lambda4   the lambda associated to the forth value
   * @param otherwise the default lambda, evaluated if no branch is matched
   * @return a SwitchExp
   */
  public SwitchExp<Input, Output> matchList(final List<? extends Result<Input>> pattern1,
                                            final Lambda<Result<Input>, Output> lambda1,
                                            final List<? extends Result<Input>> pattern2,
                                            final Lambda<Result<Input>, Output> lambda2,
                                            final List<? extends Result<Input>> pattern3,
                                            final Lambda<Result<Input>, Output> lambda3,
                                            final List<? extends Result<Input>> pattern4,
                                            final Lambda<Result<Input>, Output> lambda4,
                                            final Lambda<Result<Input>, Output> otherwise
                                           ) {
    requireNonNull(pattern1);
    requireNonNull(pattern2);
    requireNonNull(pattern3);
    requireNonNull(pattern4);
    return new SwitchExp<>(val,
                           List.of(pattern1::contains,
                                   pattern2::contains,
                                   pattern3::contains,
                                   pattern4::contains
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2),
                                   requireNonNull(lambda3),
                                   requireNonNull(lambda4)
                                  ),
                           requireNonNull(otherwise),
                           null
    );
  }

  /**
   * Matcher made up of four branches. Each branch consists of a list of values that will be used to match the result
   * with the <code>contains</code> method, and an associated lambda that will be computed in case of success. Branches
   * predicates are evaluated sequentially.If no pattern is tested true, the expression is * reduced to the null effect
   * {@link IO#NULL()}
   *
   * @param pattern1 the first list
   * @param lambda1  the lambda associated to the first value
   * @param pattern2 the second list
   * @param lambda2  the lambda associated to the second value
   * @param pattern3 the third list
   * @param lambda3  the lambda associated to the third value
   * @param pattern4 the forth list
   * @param lambda4  the lambda associated to the forth value
   * @return a SwitchExp
   * @see #matchList(List, Lambda, List, Lambda, List, Lambda, List, Lambda, Lambda)
   */
  public SwitchExp<Input, Output> matchList(final List<? extends Result<Input>> pattern1,
                                            final Lambda<Result<Input>, Output> lambda1,
                                            final List<? extends Result<Input>> pattern2,
                                            final Lambda<Result<Input>, Output> lambda2,
                                            final List<? extends Result<Input>> pattern3,
                                            final Lambda<Result<Input>, Output> lambda3,
                                            final List<? extends Result<Input>> pattern4,
                                            final Lambda<Result<Input>, Output> lambda4
                                           ) {
    requireNonNull(pattern1);
    requireNonNull(pattern2);
    requireNonNull(pattern3);
    requireNonNull(pattern4);
    return new SwitchExp<>(val,
                           List.of(pattern1::contains,
                                   pattern2::contains,
                                   pattern3::contains,
                                   pattern4::contains
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2),
                                   requireNonNull(lambda3),
                                   requireNonNull(lambda4)
                                  ),
                           _ -> IO.NULL(),
                           null
    );
  }

  /**
   * Matcher made up of four branches and a default effect. Each branch consists of a list of values that will be used
   * to match the result with the <code>contains</code> method, and an associated lambda that will be computed in case
   * of success. Branches predicates are evaluated sequentially.
   *
   * @param pattern1  the first list
   * @param lambda1   the lambda associated to the first value
   * @param pattern2  the second list
   * @param lambda2   the lambda associated to the second value
   * @param pattern3  the third list
   * @param lambda3   the lambda associated to the third value
   * @param pattern4  the forth list
   * @param lambda4   the lambda associated to the forth value
   * @param pattern5  the fifth list
   * @param lambda5   the lambda associated to the fifth value
   * @param otherwise the default lambda, evaluated if no branch is matched
   * @return a SwitchExp
   */
  public SwitchExp<Input, Output> matchList(final List<? extends Result<Input>> pattern1,
                                            final Lambda<Result<Input>, Output> lambda1,
                                            final List<? extends Result<Input>> pattern2,
                                            final Lambda<Result<Input>, Output> lambda2,
                                            final List<? extends Result<Input>> pattern3,
                                            final Lambda<Result<Input>, Output> lambda3,
                                            final List<? extends Result<Input>> pattern4,
                                            final Lambda<Result<Input>, Output> lambda4,
                                            final List<? extends Result<Input>> pattern5,
                                            final Lambda<Result<Input>, Output> lambda5,
                                            final Lambda<Result<Input>, Output> otherwise

                                           ) {
    requireNonNull(pattern1);
    requireNonNull(pattern2);
    requireNonNull(pattern3);
    requireNonNull(pattern4);
    requireNonNull(pattern5);
    return new SwitchExp<>(val,
                           List.of(pattern1::contains,
                                   pattern2::contains,
                                   pattern3::contains,
                                   pattern4::contains,
                                   pattern5::contains
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2),
                                   requireNonNull(lambda3),
                                   requireNonNull(lambda4),
                                   requireNonNull(lambda5)
                                  ),
                           requireNonNull(otherwise),
                           null
    );
  }

  /**
   * Matcher made up of four branches. Each branch consists of a list of values that will be used to match the result
   * with the <code>contains</code> method, and an associated lambda that will be computed in case of success. Branches
   * predicates are evaluated sequentially. If no pattern is tested true, the expression is * reduced to the null effect
   * {@link IO#NULL()}
   *
   * @param pattern1 the first list
   * @param lambda1  the lambda associated to the first value
   * @param pattern2 the second list
   * @param lambda2  the lambda associated to the second value
   * @param pattern3 the third list
   * @param lambda3  the lambda associated to the third value
   * @param pattern4 the forth list
   * @param lambda4  the lambda associated to the forth value
   * @param pattern5 the fifth list
   * @param lambda5  the lambda associated to the fifth value
   * @return a SwitchExp
   * @see #matchList(List, Lambda, List, Lambda, List, Lambda, List, Lambda, List, Lambda, Lambda)
   */
  public SwitchExp<Input, Output> matchList(final List<? extends Result<Input>> pattern1,
                                            final Lambda<Result<Input>, Output> lambda1,
                                            final List<? extends Result<Input>> pattern2,
                                            final Lambda<Result<Input>, Output> lambda2,
                                            final List<? extends Result<Input>> pattern3,
                                            final Lambda<Result<Input>, Output> lambda3,
                                            final List<? extends Result<Input>> pattern4,
                                            final Lambda<Result<Input>, Output> lambda4,
                                            final List<? extends Result<Input>> pattern5,
                                            final Lambda<Result<Input>, Output> lambda5

                                           ) {
    requireNonNull(pattern1);
    requireNonNull(pattern2);
    requireNonNull(pattern3);
    requireNonNull(pattern4);
    requireNonNull(pattern5);
    return new SwitchExp<>(val,
                           List.of(pattern1::contains,
                                   pattern2::contains,
                                   pattern3::contains,
                                   pattern4::contains,
                                   pattern5::contains
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2),
                                   requireNonNull(lambda3),
                                   requireNonNull(lambda4),
                                   requireNonNull(lambda5)
                                  ),
                           _ -> IO.NULL(),
                           null
    );
  }

  /**
   * Matcher made up of four branches and a default effect. Each branch consists of a list of values that will be used
   * to match the result with the <code>contains</code> method, and an associated lambda that will be computed in case
   * of success. Branches predicates are evaluated sequentially.
   *
   * @param pattern1  the first list
   * @param lambda1   the lambda associated to the first value
   * @param pattern2  the second list
   * @param lambda2   the lambda associated to the second value
   * @param pattern3  the third list
   * @param lambda3   the lambda associated to the third value
   * @param pattern4  the forth list
   * @param lambda4   the lambda associated to the forth value
   * @param pattern5  the fifth list
   * @param lambda5   the lambda associated to the fifth value
   * @param pattern6  the sixth list
   * @param lambda6   the lambda associated to the sixth value
   * @param otherwise the default lambda, evaluated if no branch is matched
   * @return a SwitchExp
   */
  public SwitchExp<Input, Output> matchList(final List<? extends Result<Input>> pattern1,
                                            final Lambda<Result<Input>, Output> lambda1,
                                            final List<? extends Result<Input>> pattern2,
                                            final Lambda<Result<Input>, Output> lambda2,
                                            final List<? extends Result<Input>> pattern3,
                                            final Lambda<Result<Input>, Output> lambda3,
                                            final List<? extends Result<Input>> pattern4,
                                            final Lambda<Result<Input>, Output> lambda4,
                                            final List<? extends Result<Input>> pattern5,
                                            final Lambda<Result<Input>, Output> lambda5,
                                            final List<? extends Result<Input>> pattern6,
                                            final Lambda<Result<Input>, Output> lambda6,
                                            final Lambda<Result<Input>, Output> otherwise
                                           ) {
    requireNonNull(pattern1);
    requireNonNull(pattern2);
    requireNonNull(pattern3);
    requireNonNull(pattern4);
    requireNonNull(pattern5);
    requireNonNull(pattern6);
    return new SwitchExp<>(val,
                           List.of(pattern1::contains,
                                   pattern2::contains,
                                   pattern3::contains,
                                   pattern4::contains,
                                   pattern5::contains,
                                   pattern6::contains
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2),
                                   requireNonNull(lambda3),
                                   requireNonNull(lambda4),
                                   requireNonNull(lambda5),
                                   requireNonNull(lambda6)
                                  ),
                           requireNonNull(otherwise),
                           null
    );
  }

  /**
   * Matcher made up of four branches. Each branch consists of a list of values that will be used to match the result
   * with the <code>contains</code> method, and an associated lambda that will be computed in case of success. Branches
   * predicates are evaluated sequentially. If no pattern is tested true, the expression is * reduced to the null effect
   * {@link IO#NULL()}
   *
   * @param pattern1 the first list
   * @param lambda1  the lambda associated to the first value
   * @param pattern2 the second list
   * @param lambda2  the lambda associated to the second value
   * @param pattern3 the third list
   * @param lambda3  the lambda associated to the third value
   * @param pattern4 the forth list
   * @param lambda4  the lambda associated to the forth value
   * @param pattern5 the fifth list
   * @param lambda5  the lambda associated to the fifth value
   * @param pattern6 the sixth list
   * @param lambda6  the lambda associated to the sixth value
   * @return a SwitchExp
   * @see #matchList(List, Lambda, List, Lambda, List, Lambda, List, Lambda, List, Lambda, List, Lambda, Lambda)
   */
  public SwitchExp<Input, Output> matchList(final List<? extends Result<Input>> pattern1,
                                            final Lambda<Result<Input>, Output> lambda1,
                                            final List<? extends Result<Input>> pattern2,
                                            final Lambda<Result<Input>, Output> lambda2,
                                            final List<? extends Result<Input>> pattern3,
                                            final Lambda<Result<Input>, Output> lambda3,
                                            final List<? extends Result<Input>> pattern4,
                                            final Lambda<Result<Input>, Output> lambda4,
                                            final List<? extends Result<Input>> pattern5,
                                            final Lambda<Result<Input>, Output> lambda5,
                                            final List<? extends Result<Input>> pattern6,
                                            final Lambda<Result<Input>, Output> lambda6
                                           ) {
    requireNonNull(pattern1);
    requireNonNull(pattern2);
    requireNonNull(pattern3);
    requireNonNull(pattern4);
    requireNonNull(pattern5);
    requireNonNull(pattern6);
    return new SwitchExp<>(val,
                           List.of(pattern1::contains,
                                   pattern2::contains,
                                   pattern3::contains,
                                   pattern4::contains,
                                   pattern5::contains,
                                   pattern6::contains
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2),
                                   requireNonNull(lambda3),
                                   requireNonNull(lambda4),
                                   requireNonNull(lambda5),
                                   requireNonNull(lambda6)
                                  ),
                           _ -> IO.NULL(),
                           null
    );
  }

  /**
   * Matcher made up of five branches and a default effect. Each branch consists of a value that will be used to match
   * the result with the <code>equals</code> method, and an associated lambda that will be computed in case of success.
   * Branches predicates are evaluated sequentially.
   *
   * @param pattern1  the first value
   * @param lambda1   the lambda associated to the first value
   * @param pattern2  the second value
   * @param lambda2   the lambda associated to the second value
   * @param pattern3  the third value
   * @param lambda3   the lambda associated to the third value
   * @param pattern4  the forth value
   * @param lambda4   the lambda associated to the forth value
   * @param pattern5  the fifth value
   * @param lambda5   the lambda associated to the fifth value
   * @param otherwise the default lambda, evaluated if no branch is matched
   * @return a SwitchExp
   */
  public SwitchExp<Input, Output> match(final Result<Input> pattern1,
                                        final Lambda<Result<Input>, Output> lambda1,
                                        final Result<Input> pattern2,
                                        final Lambda<Result<Input>, Output> lambda2,
                                        final Result<Input> pattern3,
                                        final Lambda<Result<Input>, Output> lambda3,
                                        final Result<Input> pattern4,
                                        final Lambda<Result<Input>, Output> lambda4,
                                        final Result<Input> pattern5,
                                        final Lambda<Result<Input>, Output> lambda5,
                                        final Lambda<Result<Input>, Output> otherwise
                                       ) {
    requireNonNull(pattern1);
    requireNonNull(pattern2);
    requireNonNull(pattern3);
    requireNonNull(pattern4);
    requireNonNull(pattern5);
    return new SwitchExp<>(val,
                           List.of(input -> input.equals(pattern1),
                                   input -> input.equals(pattern2),
                                   input -> input.equals(pattern3),
                                   input -> input.equals(pattern4)
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2),
                                   requireNonNull(lambda3),
                                   requireNonNull(lambda4),
                                   requireNonNull(lambda5)
                                  ),
                           requireNonNull(otherwise),
                           null
    );
  }

  /**
   * Matcher made up of five branches. Each branch consists of a value that will be used to match the result with the
   * <code>equals</code> method, and an associated lambda that will be computed in case of success.
   * Branches predicates are evaluated sequentially. If no pattern is tested true, the expression is * reduced to the
   * null effect {@link IO#NULL()}
   *
   * @param pattern1 the first value
   * @param lambda1  the lambda associated to the first value
   * @param pattern2 the second value
   * @param lambda2  the lambda associated to the second value
   * @param pattern3 the third value
   * @param lambda3  the lambda associated to the third value
   * @param pattern4 the forth value
   * @param lambda4  the lambda associated to the forth value
   * @param pattern5 the fifth value
   * @param lambda5  the lambda associated to the fifth value
   * @return a SwitchExp
   */
  public SwitchExp<Input, Output> match(final Result<Input> pattern1,
                                        final Lambda<Result<Input>, Output> lambda1,
                                        final Result<Input> pattern2,
                                        final Lambda<Result<Input>, Output> lambda2,
                                        final Result<Input> pattern3,
                                        final Lambda<Result<Input>, Output> lambda3,
                                        final Result<Input> pattern4,
                                        final Lambda<Result<Input>, Output> lambda4,
                                        final Result<Input> pattern5,
                                        final Lambda<Result<Input>, Output> lambda5
                                       ) {
    requireNonNull(pattern1);
    requireNonNull(pattern2);
    requireNonNull(pattern3);
    requireNonNull(pattern4);
    requireNonNull(pattern5);
    return new SwitchExp<>(val,
                           List.of(input -> input.equals(pattern1),
                                   input -> input.equals(pattern2),
                                   input -> input.equals(pattern3),
                                   input -> input.equals(pattern4)
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2),
                                   requireNonNull(lambda3),
                                   requireNonNull(lambda4),
                                   requireNonNull(lambda5)
                                  ),
                           _ -> IO.NULL(),
                           null
    );
  }

  /**
   * Matcher made up of six branches and a default effect. Each branch consists of a value that will be used to match
   * the result with the <code>equals</code> method, and an associated lambda that will be computed in case of success.
   * Branches predicates are evaluated sequentially.
   *
   * @param pattern1  the first value
   * @param lambda1   the lambda associated to the first value
   * @param pattern2  the second value
   * @param lambda2   the lambda associated to the second value
   * @param pattern3  the third value
   * @param lambda3   the lambda associated to the third value
   * @param pattern4  the forth value
   * @param lambda4   the lambda associated to the forth value
   * @param pattern5  the fifth value
   * @param lambda5   the lambda associated to the fifth value
   * @param pattern6  the sixth value
   * @param lambda6   the lambda associated to the sixth value
   * @param otherwise the default lambda, evaluated if no branch is matched
   * @return a SwitchExp
   */
  public SwitchExp<Input, Output> match(final Result<Input> pattern1,
                                        final Lambda<Result<Input>, Output> lambda1,
                                        final Result<Input> pattern2,
                                        final Lambda<Result<Input>, Output> lambda2,
                                        final Result<Input> pattern3,
                                        final Lambda<Result<Input>, Output> lambda3,
                                        final Result<Input> pattern4,
                                        final Lambda<Result<Input>, Output> lambda4,
                                        final Result<Input> pattern5,
                                        final Lambda<Result<Input>, Output> lambda5,
                                        final Result<Input> pattern6,
                                        final Lambda<Result<Input>, Output> lambda6,
                                        final Lambda<Result<Input>, Output> otherwise
                                       ) {
    requireNonNull(pattern1);
    requireNonNull(pattern2);
    requireNonNull(pattern3);
    requireNonNull(pattern4);
    requireNonNull(pattern5);
    requireNonNull(pattern6);
    return new SwitchExp<>(val,
                           List.of(input -> input.equals(pattern1),
                                   input -> input.equals(pattern2),
                                   input -> input.equals(pattern3),
                                   input -> input.equals(pattern4),
                                   input -> input.equals(pattern5)
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2),
                                   requireNonNull(lambda3),
                                   requireNonNull(lambda4),
                                   requireNonNull(lambda5),
                                   requireNonNull(lambda6)
                                  ),
                           requireNonNull(otherwise),
                           null
    );
  }

  /**
   * Matcher made up of six branches. Each branch consists of a value that will be used to match the result with the
   * <code>equals</code> method, and an associated lambda that will be computed in case of success.
   * Branches predicates are evaluated sequentially. If no pattern is tested true, the expression is * reduced to the
   * null effect {@link IO#NULL()}
   *
   * @param pattern1 the first value
   * @param lambda1  the lambda associated to the first value
   * @param pattern2 the second value
   * @param lambda2  the lambda associated to the second value
   * @param pattern3 the third value
   * @param lambda3  the lambda associated to the third value
   * @param pattern4 the forth value
   * @param lambda4  the lambda associated to the forth value
   * @param pattern5 the fifth value
   * @param lambda5  the lambda associated to the fifth value
   * @param pattern6 the sixth value
   * @param lambda6  the lambda associated to the sixth value
   * @return a SwitchExp
   */
  public SwitchExp<Input, Output> match(final Result<Input> pattern1,
                                        final Lambda<Result<Input>, Output> lambda1,
                                        final Result<Input> pattern2,
                                        final Lambda<Result<Input>, Output> lambda2,
                                        final Result<Input> pattern3,
                                        final Lambda<Result<Input>, Output> lambda3,
                                        final Result<Input> pattern4,
                                        final Lambda<Result<Input>, Output> lambda4,
                                        final Result<Input> pattern5,
                                        final Lambda<Result<Input>, Output> lambda5,
                                        final Result<Input> pattern6,
                                        final Lambda<Result<Input>, Output> lambda6
                                       ) {
    requireNonNull(pattern1);
    requireNonNull(pattern2);
    requireNonNull(pattern3);
    requireNonNull(pattern4);
    requireNonNull(pattern5);
    requireNonNull(pattern6);
    return new SwitchExp<>(val,
                           List.of(input -> input.equals(pattern1),
                                   input -> input.equals(pattern2),
                                   input -> input.equals(pattern3),
                                   input -> input.equals(pattern4),
                                   input -> input.equals(pattern5)
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2),
                                   requireNonNull(lambda3),
                                   requireNonNull(lambda4),
                                   requireNonNull(lambda5),
                                   requireNonNull(lambda6)
                                  ),
                           _ -> IO.NULL(),
                           null
    );
  }

  /**
   * Matcher made up of six branches and a default effect. Each branch consists of a predicate that will be used to test
   * the result, and an associated lambda that will be computed in case of the predicate returns true. Branches
   * predicates are evaluated sequentially in the order they are passed in the method.
   *
   * @param pattern1  the first value
   * @param lambda1   the lambda associated to the first value
   * @param pattern2  the second value
   * @param lambda2   the lambda associated to the second value
   * @param pattern3  the third value
   * @param lambda3   the lambda associated to the third value
   * @param pattern4  the forth value
   * @param lambda4   the lambda associated to the forth value
   * @param pattern5  the fifth value
   * @param lambda5   the lambda associated to the fifth value
   * @param otherwise the default lambda, evaluated if no branch is matched
   * @return a SwitchExp
   */
  public SwitchExp<Input, Output> matchPredicate(final Predicate<Result<Input>> pattern1,
                                                 final Lambda<Result<Input>, Output> lambda1,
                                                 final Predicate<Result<Input>> pattern2,
                                                 final Lambda<Result<Input>, Output> lambda2,
                                                 final Predicate<Result<Input>> pattern3,
                                                 final Lambda<Result<Input>, Output> lambda3,
                                                 final Predicate<Result<Input>> pattern4,
                                                 final Lambda<Result<Input>, Output> lambda4,
                                                 final Predicate<Result<Input>> pattern5,
                                                 final Lambda<Result<Input>, Output> lambda5,
                                                 final Lambda<Result<Input>, Output> otherwise
                                                ) {
    return new SwitchExp<>(val,
                           List.of(requireNonNull(pattern1),
                                   requireNonNull(pattern2),
                                   requireNonNull(pattern3),
                                   requireNonNull(pattern4),
                                   requireNonNull(pattern5)
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2),
                                   requireNonNull(lambda3),
                                   requireNonNull(lambda4),
                                   requireNonNull(lambda5)
                                  ),
                           requireNonNull(otherwise),
                           null
    );
  }

  /**
   * Matcher made up of six branches. Each branch consists of a predicate that will be used to test the result, and an
   * associated lambda that will be computed in case of the predicate returns true. Branches predicates are evaluated
   * sequentially in the order they are passed in the method. If no pattern is tested true, the expression is * reduced
   * to the null effect {@link IO#NULL()}
   *
   * @param pattern1 the first value
   * @param lambda1  the lambda associated to the first value
   * @param pattern2 the second value
   * @param lambda2  the lambda associated to the second value
   * @param pattern3 the third value
   * @param lambda3  the lambda associated to the third value
   * @param pattern4 the forth value
   * @param lambda4  the lambda associated to the forth value
   * @param pattern5 the fifth value
   * @param lambda5  the lambda associated to the fifth value
   * @return a SwitchExp
   * @see #matchPredicate(Predicate, Lambda, Predicate, Lambda, Predicate, Lambda, Predicate, Lambda, Predicate, Lambda,
   * Lambda)
   */
  public SwitchExp<Input, Output> matchPredicate(final Predicate<Result<Input>> pattern1,
                                                 final Lambda<Result<Input>, Output> lambda1,
                                                 final Predicate<Result<Input>> pattern2,
                                                 final Lambda<Result<Input>, Output> lambda2,
                                                 final Predicate<Result<Input>> pattern3,
                                                 final Lambda<Result<Input>, Output> lambda3,
                                                 final Predicate<Result<Input>> pattern4,
                                                 final Lambda<Result<Input>, Output> lambda4,
                                                 final Predicate<Result<Input>> pattern5,
                                                 final Lambda<Result<Input>, Output> lambda5
                                                ) {
    return new SwitchExp<>(val,
                           List.of(requireNonNull(pattern1),
                                   requireNonNull(pattern2),
                                   requireNonNull(pattern3),
                                   requireNonNull(pattern4),
                                   requireNonNull(pattern5)
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2),
                                   requireNonNull(lambda3),
                                   requireNonNull(lambda4),
                                   requireNonNull(lambda5)
                                  ),
                           _ -> IO.NULL(),
                           null
    );
  }

  /**
   * Matcher made up of six branches and a default effect. Each branch consists of a predicate that will be used to test
   * the result, and an associated lambda that will be computed in case of the predicate returns true. Branches
   * predicates are evaluated sequentially in the order they are passed in the method.
   *
   * @param pattern1  the first value
   * @param lambda1   the lambda associated to the first value
   * @param pattern2  the second value
   * @param lambda2   the lambda associated to the second value
   * @param pattern3  the third value
   * @param lambda3   the lambda associated to the third value
   * @param pattern4  the forth value
   * @param lambda4   the lambda associated to the forth value
   * @param pattern5  the fifth value
   * @param lambda5   the lambda associated to the fifth value
   * @param pattern6  the sixth value
   * @param lambda6   the lambda associated to the sixth value
   * @param otherwise the default lambda, evaluated if no branch is matched
   * @return a SwitchExp
   */
  public SwitchExp<Input, Output> matchPredicate(final Predicate<Result<Input>> pattern1,
                                                 final Lambda<Result<Input>, Output> lambda1,
                                                 final Predicate<Result<Input>> pattern2,
                                                 final Lambda<Result<Input>, Output> lambda2,
                                                 final Predicate<Result<Input>> pattern3,
                                                 final Lambda<Result<Input>, Output> lambda3,
                                                 final Predicate<Result<Input>> pattern4,
                                                 final Lambda<Result<Input>, Output> lambda4,
                                                 final Predicate<Result<Input>> pattern5,
                                                 final Lambda<Result<Input>, Output> lambda5,
                                                 final Predicate<Result<Input>> pattern6,
                                                 final Lambda<Result<Input>, Output> lambda6,
                                                 final Lambda<Result<Input>, Output> otherwise
                                                ) {
    return new SwitchExp<>(val,
                           List.of(requireNonNull(pattern1),
                                   requireNonNull(pattern2),
                                   requireNonNull(pattern3),
                                   requireNonNull(pattern4),
                                   requireNonNull(pattern5),
                                   requireNonNull(pattern6)
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2),
                                   requireNonNull(lambda3),
                                   requireNonNull(lambda4),
                                   requireNonNull(lambda5),
                                   requireNonNull(lambda6)
                                  ),
                           requireNonNull(otherwise),
                           null
    );
  }

  /**
   * Matcher made up of six branches. Each branch consists of a predicate that will be used to test the result, and an
   * associated lambda that will be computed in case of the predicate returns true. Branches predicates are evaluated
   * sequentially in the order they are passed in the method. If no pattern is tested true, the expression is * reduced
   * to the null effect {@link IO#NULL()}
   *
   * @param pattern1 the first value
   * @param lambda1  the lambda associated to the first value
   * @param pattern2 the second value
   * @param lambda2  the lambda associated to the second value
   * @param pattern3 the third value
   * @param lambda3  the lambda associated to the third value
   * @param pattern4 the forth value
   * @param lambda4  the lambda associated to the forth value
   * @param pattern5 the fifth value
   * @param lambda5  the lambda associated to the fifth value
   * @param pattern6 the sixth value
   * @param lambda6  the lambda associated to the sixth value
   * @return a SwitchExp
   * @see #matchPredicate(Predicate, Lambda, Predicate, Lambda, Predicate, Lambda, Predicate, Lambda, Predicate, Lambda,
   * Predicate, Lambda, Lambda)
   */
  public SwitchExp<Input, Output> matchPredicate(final Predicate<Result<Input>> pattern1,
                                                 final Lambda<Result<Input>, Output> lambda1,
                                                 final Predicate<Result<Input>> pattern2,
                                                 final Lambda<Result<Input>, Output> lambda2,
                                                 final Predicate<Result<Input>> pattern3,
                                                 final Lambda<Result<Input>, Output> lambda3,
                                                 final Predicate<Result<Input>> pattern4,
                                                 final Lambda<Result<Input>, Output> lambda4,
                                                 final Predicate<Result<Input>> pattern5,
                                                 final Lambda<Result<Input>, Output> lambda5,
                                                 final Predicate<Result<Input>> pattern6,
                                                 final Lambda<Result<Input>, Output> lambda6
                                                ) {
    return new SwitchExp<>(val,
                           List.of(requireNonNull(pattern1),
                                   requireNonNull(pattern2),
                                   requireNonNull(pattern3),
                                   requireNonNull(pattern4),
                                   requireNonNull(pattern5),
                                   requireNonNull(pattern6)
                                  ),
                           List.of(requireNonNull(lambda1),
                                   requireNonNull(lambda2),
                                   requireNonNull(lambda3),
                                   requireNonNull(lambda4),
                                   requireNonNull(lambda5),
                                   requireNonNull(lambda6)
                                  ),
                           _ -> IO.NULL(),
                           null
    );
  }

}
