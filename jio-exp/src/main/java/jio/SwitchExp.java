package jio;


import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * It's an immutable expression that implements multiple predicate-value branches like the
 * Cond expression. However, it evaluates a type I value and allows multiple value clauses based on evaluating that value.
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
              final Function<ExpEvent,BiConsumer<O, Throwable>> logger
             ) {
        super(logger);
        this.val = val;
        this.predicates = predicates;
        this.lambdas = lambdas;
        this.otherwise = otherwise;
    }

    /**
     * Creates a SwitchMatcher from a given value that will be evaluated and matched against the branches
     * defined with the {@link SwitchMatcher#match(Object, Lambda, Object, Lambda, Lambda) match} method
     * @param input the input that will be evaluated
     * @return a SwitchMatcher
     * @param <I> the type of the input
     * @param <O> the type of the expression result
     */
    public static <I, O> SwitchMatcher<I, O> eval(final I input) {
        return new SwitchMatcher<>(IO.fromValue(requireNonNull(input)));
    }

    /**
     * Creates a SwitchMatcher from a given effect that will be evaluated and matched against the branches
     * defined with the {@link SwitchMatcher#match(Object, Lambda, Object, Lambda, Lambda) match} method
     * @param input the effect that will be evaluated
     * @return a SwitchMatcher
     * @param <I> the type of the input
     * @param <O> the type of the expression result
     */
    public static <I, O> SwitchMatcher<I, O> eval(final IO<I> input) {
        return new SwitchMatcher<>(requireNonNull(input));
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

    /**
     * Creates a new ListExp expression where the given retry policy is applied recursively
     * to every subexpression when an exception is tested true against the specified predicate.
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

    /**
     * Creates a new SwitchExp that will write to the given logger information about every
     * computation evaluated to reduce this expression (like {@link #debugEach(String)} does).
     * A final log message created with the specified builder is written after reducing
     * the whole expression
     *
     * @param builder the builder to create the log message from the result of the expression
     * @return a new SwitchExp
     * @see #debugEach(String) debugEach
     */
    @Override
    public SwitchExp<I, O> debugEach(final EventBuilder<O> builder
                                    ) {
        return new SwitchExp<>(LoggerHelper.debugIO(val,
                                                    String.format("%s-eval", this.getClass().getSimpleName()),
                                                    builder.context
                                                   ),
                               predicates,
                               LoggerHelper.debugLambdas(lambdas,
                                                         this.getClass().getSimpleName() + "-branch",
                                                         builder.context
                                                        ),
                               LoggerHelper.debugLambda(otherwise,
                                                        String.format("%s-otherwise", this.getClass().getSimpleName()),
                                                        builder.context
                                                       ),
                               getJFRPublisher(builder)

        );
    }


    @Override
    public SwitchExp<I, O> retryEach(final RetryPolicy policy) {
        return retryEach(e -> true, policy);
    }


    /**
     * Creates a new SwitchExp that will print out on the console information about every
     * computation evaluated to reduce this expression. The given context will be associated
     * to every subexpression and printed out to correlate all the evaluations (contextual
     * logging).
     * <p>
     * The line format is the following:
     * <p>
     * datetime thread logger [context] elapsed_time success|exception expression|subexpression result?
     * <p>
     * Find bellow an example:
     *
     * <pre>
     * {@code
     *
     *           SwitchExp.<Integer, String>eval(IO.succeed(2))
     *                                     .match(1, i -> IO.succeed("one"),
     *                                            2, i -> IO.succeed("two"),
     *                                            i -> IO.succeed("default")
     *                                           )
     *                                     .debugEach("context")
     *                                     .join()
     *
     *
     * }
     * </pre>
     *
     * 2023-02-04T17:58:43.148662+01:00 main DEBUGGER [context] 7299292 success SwitchExp-eval 2
     * 2023-02-04T17:58:43.157197+01:00 main DEBUGGER [context] 60417 success SwitchExp-branch[1]
     * 2023-02-04T17:58:43.157785+01:00 main DEBUGGER [context] 11846250 success SwitchExp two
     *
     * @param context the context shared by all the subexpressions that will be printed out
     * @return a new SwitchExp
     */
    @Override
    public SwitchExp<I, O> debugEach(final String context) {
        return debugEach(EventBuilder.<O>ofExp(this.getClass().getSimpleName()).setContext(context));


    }
}
