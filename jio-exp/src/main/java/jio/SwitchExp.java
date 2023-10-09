package jio;


import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * It's an immutable expression that implements multiple predicate-value branches like the Cond expression. However, it
 * evaluates a type I value and allows multiple value clauses based on evaluating that value.
 *
 * @param <O> the type of the value this expression will be reduced
 */
public final class SwitchExp<I, O> extends Exp<O> {
    private final IO<I> val;
    private final List<Predicate<I>> predicates;
    private final List<Lambda<I, O>> lambdas;
    private final Lambda<I, O> otherwise;

    SwitchExp(final IO<I> val,
              final List<Predicate<I>> predicates,
              final List<Lambda<I, O>> lambdas,
              final Lambda<I, O> otherwise,
              final Function<ExpEvent, BiConsumer<O, Throwable>> debugger
             ) {
        super(debugger);
        this.val = val;
        this.predicates = predicates;
        this.lambdas = lambdas;
        this.otherwise = otherwise;
    }

    /**
     * Creates a SwitchMatcher from a given value that will be evaluated and matched against the branches defined with
     * the {@link SwitchMatcher#match(Object, Lambda, Object, Lambda, Lambda) match} method
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
                val.thenCompose(i -> otherwise.apply(i).get()) :
                val.thenCompose(i ->
                                        tests.get(condTestedSoFar).test(i) ?
                                                lambdas.get(condTestedSoFar).apply(i).get() :
                                                get(val, tests, lambdas, otherwise, condTestedSoFar + 1)
                               );


    }

    @Override
    CompletableFuture<O> reduceExp() {
        return SwitchExp.get(val.get(),
                             predicates,
                             lambdas,
                             otherwise,
                             0
                            );
    }

    /**
     * Creates a new ListExp expression where the given retry policy is applied recursively to every subexpression when
     * an exception is tested true against the specified predicate.
     *
     * @param predicate the predicate to test exceptions
     * @param policy    the retry policy
     * @return a new ListExp
     */
    @Override
    public SwitchExp<I, O> retryEach(final Predicate<Throwable> predicate,
                                     final RetryPolicy policy
                                    ) {
        requireNonNull(predicate);
        requireNonNull(policy);
        return new SwitchExp<>(val.retry(predicate, policy),
                               predicates,
                               lambdas.stream().map(it -> it.map(a -> a.retry(predicate, policy))).toList(),
                               otherwise.map(it -> it.retry(predicate, policy)),
                               jfrPublisher

        );
    }


    @Override
    public SwitchExp<I, O> debugEach(final EventBuilder<O> eventBuilder
                                    ) {
        return new SwitchExp<>(LoggerHelper.debugIO(val,
                                                    "%s-eval".formatted(eventBuilder.exp),
                                                    eventBuilder.context
                                                   ),
                               predicates,
                               LoggerHelper.debugLambdas(lambdas,
                                                         "%s-branch".formatted(eventBuilder.exp),
                                                         eventBuilder.context
                                                        ),
                               LoggerHelper.debugLambda(otherwise,
                                                        "%s-otherwise".formatted(eventBuilder.exp),
                                                        eventBuilder.context
                                                       ),
                               getJFRPublisher(eventBuilder)

        );
    }


    @Override
    public SwitchExp<I, O> retryEach(final RetryPolicy policy) {
        return retryEach(e -> true, policy);
    }


    @Override
    public SwitchExp<I, O> debugEach(final String context) {
        return debugEach(new EventBuilder<>(this.getClass().getSimpleName(), context));


    }
}
