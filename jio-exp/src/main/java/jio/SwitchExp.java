package jio;


import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * It's an immutable expression that implements multiple predicate-value branches like the Cond expression. However, it
 * evaluates a type I value and allows multiple value clauses based on evaluating that value. If none of the branches
 * patterns match the evaluated value, you can specify a fallback effect, or it will default to using {@link IO#NULL}
 *
 * @param <Input> the type of the value that will be matched against different patters to determine which branch will be
 *            executed
 * @param <Output> the type of the value this expression will be reduced
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
   * Creates a SwitchMatcher from a given value that will be evaluated and matched against the branches defined with the
   * {@link SwitchMatcher#match(Object, Lambda, Object, Lambda, Lambda) match} method
   *
   * @param input the input that will be evaluated
   * @param <I>   the type of the input
   * @param <O>   the type of the expression result
   * @return a SwitchMatcher
   */
  public static <I, O> SwitchMatcher<I, O> eval(final I input) {
    return new SwitchMatcher<>(IO.succeed(requireNonNull(input)));
  }

  /**
   * Creates a SwitchMatcher from a given effect that will be evaluated and matched against the branches defined with
   * the {@link SwitchMatcher#match(Object, Lambda, Object, Lambda, Lambda) match} method
   *
   * @param input the effect that will be evaluated
   * @param <I>   the type of the input
   * @param <O>   the type of the expression result
   * @return a SwitchMatcher
   */
  public static <I, O> SwitchMatcher<I, O> eval(final IO<I> input) {
    return new SwitchMatcher<>(requireNonNull(input));
  }

  private static <I, O> CompletableFuture<O> get(CompletableFuture<I> val,
                                                 List<Predicate<I>> tests,
                                                 List<Lambda<I, O>> lambdas,
                                                 Lambda<I, O> otherwise,
                                                 int condTestedSoFar
                                                ) {
    return condTestedSoFar == tests.size() ?
           val.thenCompose(i -> otherwise.apply(i)
                                         .get()) :
           val.thenCompose(i ->
                               tests.get(condTestedSoFar)
                                    .test(i) ?
                               lambdas.get(condTestedSoFar)
                                      .apply(i)
                                      .get() :
                               get(val,
                                   tests,
                                   lambdas,
                                   otherwise,
                                   condTestedSoFar + 1)
                          );


  }

  @Override
  CompletableFuture<Output> reduceExp() {
    return SwitchExp.get(val.get(),
                         predicates,
                         lambdas,
                         otherwise,
                         0
                        );
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
    return retryEach(e -> true,
                     policy);
  }


  @Override
  public SwitchExp<Input, Output> debugEach(final String context) {
    return debugEach(EventBuilder.of(this.getClass()
                                         .getSimpleName(),
                                     context));


  }

}
