package jio.console;

import fun.tuple.Pair;
import fun.tuple.Triple;
import jio.*;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * Set of different programs modeled with JIO effects to print out text and interact with the user
 */
public final class Programs {

    /**
     * effect that reads a line from the console
     */
    public static IO<String> READ_LINE = IO.fromEffect(() -> {
        try {
            Scanner in = new Scanner(System.in,
                                     StandardCharsets.UTF_8
            );
            return CompletableFuture.completedFuture(in.nextLine());
        } catch (Exception exception) {
            return CompletableFuture.failedFuture(exception);
        }
    });
    /**
     * effect that reads an integer from the console
     */
    public static IO<Integer> READ_INT = IO.fromEffect(() -> {
        try {
            Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);
            return CompletableFuture.completedFuture(in.nextInt());
        } catch (Exception exception) {
            return CompletableFuture.failedFuture(exception);
        }
    });
    /**
     * effecdt that reads a boolean from the console
     */
    public static IO<Boolean> READ_BOOLEAN = IO.fromEffect(() -> {
        try {
            Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);
            return CompletableFuture.completedFuture(in.nextBoolean());
        } catch (Exception exception) {
            return CompletableFuture.failedFuture(exception);
        }
    });

    /**
     * effect that prints out the given line on the console
     *
     * @param line the line
     * @return a JIO effect
     */
    public static IO<Void> PRINT_LINE(final String line) {
        return IO.fromEffect(() -> {
            try {
                System.out.print(line);
                return CompletableFuture.completedFuture(null);
            } catch (Exception exception) {
                return CompletableFuture.failedFuture(exception);
            }

        });
    }

    /**
     * effect that prints out a line on the console prepending the specified control char.
     * and appending the {@link ControlChars#RESET reset} control char
     *
     * @param line  the line
     * @param color the control char
     * @return a JIO effect
     */
    public static IO<Void> PRINT_LINE(final String line,
                                      final ControlChars color
                                     ) {
        requireNonNull(color);
        return IO.fromEffect(() -> {
                             try {
                                 System.out.print(color.code + line + ControlChars.RESET);
                                 return CompletableFuture.completedFuture(null);
                             } catch (Exception exception) {
                                 return CompletableFuture.failedFuture(exception);
                             }
                         }
                            );
    }

    /**
     * effect that prints out the given new line on the console
     *
     * @param line the line
     * @return a JIO effect
     */
    public static IO<Void> PRINT_NEW_LINE(final String line) {
        return IO.fromEffect(() -> {
            try {
                System.out.println(line);
                return CompletableFuture.completedFuture(null);
            } catch (Exception exception) {
                return CompletableFuture.failedFuture(exception);
            }
        });
    }

    /**
     * effect that prints out a new line on the console prepending the specified control char.
     * and appending the {@link ControlChars#RESET reset} control char
     *
     * @param line  the line
     * @param color the control char
     * @return a JIO effect
     */
    public static IO<Void> PRINT_NEW_LINE(final String line,
                                          final ControlChars color
                                         ) {
        requireNonNull(color);
        return IO.fromEffect(() -> {
            try {
                System.out.println(color.code + line + ControlChars.RESET);
                return CompletableFuture.completedFuture(null);
            } catch (Exception exception) {
                return CompletableFuture.failedFuture(exception);
            }
        });
    }

    /**
     * Creates an effect that ask the user for typing in a string that will be
     * returned by the effect
     *
     * @param params the parameters to specify how to interact with the user
     * @return a JIO effect
     */
    public static IO<String> ASK_FOR_INPUT(AskForInputParams params) {
        return PRINT_NEW_LINE(params.promptMessage)
                .then($ -> READ_LINE.then(input -> params.inputValidator.test(input) ?
                        IO.fromValue(input) :
                        IO.fromFailure(new IllegalArgumentException(params.errorMessage)))
                     )
                .retry(params.policy);
    }

    /**
     * Creates an effect that ask the user for typing in two strings, one after the other,
     * that will be returned as a pair
     *
     * @param params1 the parameters to specify how to interact with the user to ask for the first value
     * @param params2 the parameters to specify how to interact with the user to ask for the second value
     * @return a JIO effect
     */
    public static IO<Pair<String, String>> ASK_FOR_PAIR(AskForInputParams params1,
                                                        AskForInputParams params2
                                                       ) {

        return PairExp.seq(ASK_FOR_INPUT(params1),
                           ASK_FOR_INPUT(params2)
                          );
    }

    /**
     * Creates an effect that ask the user for typing in thre strings, one after the other,
     * that will be returned as a triple
     *
     * @param params1 the parameters to specify how to interact with the user to ask for the first value
     * @param params2 the parameters to specify how to interact with the user to ask for the second value
     * @param params3 the parameters to specify how to interact with the user to ask for the third value
     * @return a JIO effect
     */
    public static IO<Triple<String, String, String>> ASK_FOR_TRIPLE(AskForInputParams params1,
                                                                    AskForInputParams params2,
                                                                    AskForInputParams params3
                                                                   ) {

        return
                TripleExp.seq(ASK_FOR_INPUT(params1),
                              ASK_FOR_INPUT(params2),
                              ASK_FOR_INPUT(params3)
                             );


    }

    /**
     * List of parameters to be considered when asking the user for typing in some text
     *
     * @param promptMessage  the message shown to the user
     * @param inputValidator the predicate that is evaluated to true if the user input is valid
     * @param errorMessage   the message error shown to the user in case the inputValidator is evaluated to false
     * @param policy         retry policy to be applied in case of the user input is invalid
     */
    public record AskForInputParams(String promptMessage,
                                    Predicate<String> inputValidator,
                                    String errorMessage,
                                    RetryPolicy policy
    ) {

        /**
         * Constructor that consider all the user input as valid
         *
         * @param promptMessage the message shown to the user
         */
        public AskForInputParams(String promptMessage) {
            this(promptMessage,
                 e -> true,
                 "",
                 RetryPolicies.limitRetries(2)
                );
        }

    }
}
