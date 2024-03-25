package jio.console;

import static java.util.Objects.requireNonNull;

import fun.tuple.Pair;
import fun.tuple.Triple;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;
import jio.IO;
import jio.ListExp;
import jio.PairExp;
import jio.RetryPolicies;
import jio.RetryPolicy;
import jio.TripleExp;

/**
 * Set of different programs modeled with JIO effects to print out text and interact with the user. These programs
 * include reading lines, integers, booleans, printing text to the console, and interacting with the user to input
 * data.
 */
public final class Programs {

  /**
   * Effect that reads a line from the console.
   */
  public final static IO<String> READ_LINE = IO.task(() -> {

    Scanner in = new Scanner(System.in,
                             StandardCharsets.UTF_8
    );
    return in.nextLine();

  });
  /**
   * Effect that reads an integer from the console.
   */
  public static IO<Integer> READ_INT = IO.task(() -> {
    Scanner in = new Scanner(System.in,
                             StandardCharsets.UTF_8);
    return in.nextInt();
  });
  /**
   * Effect that reads a boolean from the console.
   */
  public static IO<Boolean> READ_BOOLEAN = IO.task(() -> {
    Scanner in = new Scanner(System.in,
                             StandardCharsets.UTF_8);
    return in.nextBoolean();
  });

  /**
   * Effect that prints out the given line on the console.
   *
   * @param line the line to print
   * @return a JIO effect
   */
  public static IO<Void> PRINT_LINE(final String line) {
    return IO.lazy(() -> {
      System.out.print(line);
      return null;
    });
  }

  /**
   * Effect that prints out a line on the console, applying the specified control character color.
   *
   * @param line  the line to print
   * @param color the control character color to apply
   * @return a JIO effect
   */
  public static IO<Void> PRINT_LINE(final String line,
                                    final ControlChars color
  ) {
    requireNonNull(color);
    return IO.task(() -> {
      System.out.print(color.code + line + ControlChars.RESET);
      return null;
    }
    );
  }

  /**
   * Effect that prints out the given new line on the console.
   *
   * @param line the line to print
   * @return a JIO effect
   */
  public static IO<Void> PRINT_NEW_LINE(final String line) {
    return IO.task(() -> {
      System.out.println(line);
      return null;
    });
  }

  /**
   * Effect that prints out a new line on the console, applying the specified control character color.
   *
   * @param line  the line to print
   * @param color the control character color to apply
   * @return a JIO effect
   */
  public static IO<Void> PRINT_NEW_LINE(final String line,
                                        final ControlChars color
  ) {
    requireNonNull(color);
    return IO.task(() -> {
      System.out.println(color.code + line + ControlChars.RESET);
      return null;
    });
  }

  /**
   * Creates an effect that prompts the user for input and reads a string from the console.
   *
   * @param params the parameters to specify how to interact with the user
   * @return a JIO effect that reads the user's input
   */
  public static IO<String> ASK_FOR_INPUT(AskForInputParams params) {

    return PRINT_NEW_LINE(params.promptMessage)
                                               .then($ -> READ_LINE.then(input -> params.inputValidator.test(input) ? IO
                                                                                                                        .succeed(input)
                                                   : IO.fail(new IllegalArgumentException(params.errorMessage)))
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
           e -> true,
           "",
           RetryPolicies.limitRetries(2)
      );
    }

  }

}
