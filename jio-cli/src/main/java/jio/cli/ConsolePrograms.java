package jio.cli;

import fun.tuple.Pair;
import fun.tuple.Triple;
import jio.*;

import java.util.List;
import java.util.function.Predicate;

import static jio.cli.ConsoleReaders.READ_LINE;

public final class ConsolePrograms {

    private ConsolePrograms() {
    }

    /**
     * Creates an effect that prompts the user for input and reads a string from the console.
     *
     * @param params the parameters to specify how to interact with the user
     * @return a JIO effect that reads the user's input
     */
    public static IO<String> ASK_FOR_INPUT(AskForInputParams params) {

        return IO.lazy(() -> ConsolePrinter.printlnPrompt("\n%s".formatted(params.promptMessage)))
                 .then(_ -> IO.lazy(() -> ConsolePrinter.printPrompt("~ "))
                              .then(_ -> READ_LINE.then(input -> {
                                                            return params.inputValidator.test(input) ?
                                                                    IO.succeed(input) :
                                                                    IO.lazy(() -> ConsolePrinter.printError("\n%s".formatted(params.errorMessage)))
                                                                      .then(_ -> IO.fail(new IllegalArgumentException()));
                                                        }
                                                       )
                                   )
                      )
                 .retry(params.policy);
    }

    /**
     * Creates an effect that prompts the user for input multiple times, collecting a list of strings.
     *
     * @param params the parameters to specify how to interact with the user
     * @param others additional sets of parameters for more input prompts
     * @return a JIO effect that reads multiple lines of user input as a list
     */
    public static IO<List<String>> ASK_FOR_INPUTS(AskForInputParams params,
                                                  AskForInputParams... others
                                                 ) {

        var seq = ListExp.seq(ASK_FOR_INPUT(params));

        for (AskForInputParams other : others) {
            seq = seq.append(ASK_FOR_INPUT(other));
        }

        return seq;
    }

    /**
     * Creates an effect that prompts the user for input twice, returning a pair of strings.
     *
     * @param params1 the parameters to specify how to interact with the user for the first input
     * @param params2 the parameters to specify how to interact with the user for the second input
     * @return a JIO effect that reads two lines of user input as a pair
     */
    public static IO<Pair<String, String>> ASK_FOR_PAIR(AskForInputParams params1,
                                                        AskForInputParams params2
                                                       ) {

        return PairExp.seq(ASK_FOR_INPUT(params1),
                           ASK_FOR_INPUT(params2)
                          );
    }

    /**
     * Creates an effect that prompts the user for input three times, returning a triple of strings.
     *
     * @param params1 the parameters to specify how to interact with the user for the first input
     * @param params2 the parameters to specify how to interact with the user for the second input
     * @param params3 the parameters to specify how to interact with the user for the third input
     * @return a JIO effect that reads three lines of user input as a triple
     */
    public static IO<Triple<String, String, String>> ASK_FOR_TRIPLE(AskForInputParams params1,
                                                                    AskForInputParams params2,
                                                                    AskForInputParams params3
                                                                   ) {

        return TripleExp.seq(ASK_FOR_INPUT(params1),
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
                 _ -> true,
                 "",
                 RetryPolicies.limitRetries(2)
                );
        }

    }
}
