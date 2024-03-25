package jio;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import jio.Result.Failure;

/**
 * Represents an immutable expression that implements multiple predicate-output branches like the Cond expression.
 * However, it evaluates a type I output and allows multiple output clauses based on evaluating that output. If none
 * of the branches  patterns match the evaluated output, you can specify a fallback effect, or it will default to
 * using {@link IO#NULL}
 *
 * @param <Input>  the type of the output that will be matched against different patters to determine which branch will
 *                 be executed
 * @param <Output> the type of the output this expression will be reduced
 */
public final class SwitchExp<Input, Output> extends Exp<Output> {

  private final IO<Input> val;
  private final List<Predicate<Input>> predicates;
  private final List<Lambda<Input, Output>> lambdas;
  private final Lambda<Input, Output> otherwise;

  SwitchExp(final IO<Input> val,
            final List<Predicate<Input>> predicates,
            final List<Lambda<Input, Output>> lambdas,
            final Lambda<Input, Output> otherwise,
            final Function<EvalExpEvent, BiConsumer<Output, Throwable>> debugger
           ) {
    super(debugger);
    this.val = val;
    this.predicates = predicates;
    this.lambdas = lambdas;
    this.otherwise = otherwise;
  }

  /**
   * Creates a SwitchMatcher from a given output that will be evaluated and matched against different branches
   *
   * @param input    the input that will be evaluated
   * @param <Input>  the type of the input
   * @param <Output> the type of the expression result
   * @return a SwitchMatcher
   */
  public static <Input, Output> SwitchMatcher<Input, Output> eval(final Input input) {
    return new SwitchMatcher<>(IO.succeed(requireNonNull(input)));
  }

  /**
   * Creates a SwitchMatcher from a given effect that will be evaluated and matched against different branches
   *
   * @param input    the effect that will be evaluated
   * @param <Input>  the type of the input
   * @param <Output> the type of the expression result
   * @return a SwitchMatcher
   */
  public static <Input, Output> SwitchMatcher<Input, Output> eval(final IO<Input> input) {
    return new SwitchMatcher<>(requireNonNull(input));
  }

  private static <Input, Output> Result<Output> get(Input input,
                                                    List<Predicate<Input>> tests,
                                                    List<Lambda<Input, Output>> lambdas,
                                                    Lambda<Input, Output> otherwise,
                                                    int condTestedSoFar
                                                   ) {
    if (condTestedSoFar == tests.size()) {
      return otherwise.apply(input)
                      .compute();
    }
    return tests.get(condTestedSoFar)
                .test(input) ? lambdas.get(condTestedSoFar)
                                      .apply(input)
                                      .compute() : get(input,
                                                      tests,
                                                      lambdas,
                                                      otherwise,
                                                      condTestedSoFar + 1);

  }

  @Override
  Result<Output> reduceExp() {
    try {
      return SwitchExp.get(val.call().getOutputOrThrow(),
                           predicates,
                           lambdas,
                           otherwise,
                           0
                          );
    } catch (Exception e) {
      return new Failure<>(e);
    }
  }

  /**
   * Creates a new ListExp expression where the given retry policy is applied recursively to every subexpression when an
   * exception is tested true against the specified predicate.
   *
   * @param predicate the predicate to test exceptions
   * @param policy    the retry policy
   * @return a new ListExp
   */
  @Override
  public SwitchExp<Input, Output> retryEach(final Predicate<? super Throwable> predicate,
                                            final RetryPolicy policy
                                           ) {
    requireNonNull(predicate);
    requireNonNull(policy);
    return new SwitchExp<>(val.retry(predicate,
                                     policy),
                           predicates,
                           lambdas.stream()
                                  .map(it -> it.map(a -> a.retry(predicate,
                                                                 policy)))
                                  .toList(),
                           otherwise.map(it -> it.retry(predicate,
                                                        policy)),
                           jfrPublisher

    );
  }

  @Override
  public SwitchExp<Input, Output> debugEach(final EventBuilder<Output> eventBuilder
                                           ) {
    return new SwitchExp<>(DebuggerHelper.debugIO(val,
                                                  "%s-eval".formatted(eventBuilder.exp),
                                                  eventBuilder.context
                                                 ),
                           predicates,
                           DebuggerHelper.debugLambdas(lambdas,
                                                       "%s-branch".formatted(eventBuilder.exp),
                                                       eventBuilder.context
                                                      ),
                           DebuggerHelper.debugLambda(otherwise,
                                                      "%s-otherwise".formatted(eventBuilder.exp),
                                                      eventBuilder.context
                                                     ),
                           getJFRPublisher(eventBuilder)

    );
  }

  @Override
  public SwitchExp<Input, Output> retryEach(final RetryPolicy policy) {
    return retryEach(_ -> true,
                     policy);
  }

  @Override
  public SwitchExp<Input, Output> debugEach(final String context) {
    return debugEach(EventBuilder.of(this.getClass()
                                         .getSimpleName(),
                                     context));

  }

}
