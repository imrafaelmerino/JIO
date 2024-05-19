package jio.cli;

import jio.IO;
import jio.ListExp;
import jio.RetryPolicies;
import jsonvalues.JsObj;

import java.util.Arrays;
import java.util.function.Function;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static java.time.Duration.ofMillis;

/**
 * Represents a command to execute the last command one or more times, optionally with a repetition interval or
 * duration. It provides flexibility in repeating the last command based on user input.
 * <p>
 * Examples of valid input patterns: - Execute the last command once: "last" - Execute the last command a specified
 * number of times: "last 3" - Execute the last command at regular intervals: "last every 100" - Execute the last
 * command at regular intervals for a specified duration: "last every 100 for 1000"
 *
 * @see Command
 */
final class LastCommand extends Command {

    static final Pattern pattern1 = Pattern.compile("last \\d+");
    static final Pattern pattern2 = Pattern.compile("last every (?<every>\\d+)$");

    static final Pattern pattern3 = Pattern.compile("last every (?<every>\\d+) for (?<for>\\d+)$");

    private static final String COMMAND_NAME = "last";

    public LastCommand() {
        super(COMMAND_NAME,
              """
                          Executes the last command one or more times, optionally with a repetition interval or duration.
                      
                          Usage:
                              last [count]
                              last every <interval>
                              last every <interval> for <duration>
                      
                          Parameters:
                              [count]       - Optional. Number of times to repeat the last command.
                              <interval>    - Interval in milliseconds between repetitions.
                              <duration>    - Total duration in milliseconds to keep repeating the command.
                      
                          Examples:
                              last                  (executes the last command once)
                              last 3                (executes the last command 3 times)
                              last every 100        (executes the last command every 100 milliseconds)
                              last every 100 for 1000 (executes the last command every 100 milliseconds for a total of 1000 milliseconds)
                      
                          Description:
                              The 'last' command re-executes the most recently executed command. You can specify how many times
                              to repeat the command, set an interval for repetitions, or define a total duration for the repetitions.
                      
                          Notes:
                              - If the history is empty, an appropriate message will be shown.
                              - Ensure that the parameters are provided correctly to avoid pattern mismatches.
                      """);
    }

    @Override
    public Function<String[], IO<String>> apply(final JsObj conf,
                                                final State state
                                               ) {
        return tokens -> {
            if (state.historyCommands.isEmpty()) {
                return IO.succeed("The history stack is empty!");
            }
            IO<String> lastCommand = state.getHistoryCommand(state.historyCommands.size() - 1);

            if (tokens.length == 1) {
                return lastCommand;
            }

            String command = String.join(" ",
                                         Arrays.stream(tokens)
                                               .toList());
            if (pattern1.matcher(command)
                        .matches()) {
                ListExp<String> list = ListExp.par(lastCommand);
                for (int i = 1; i < parseInt(tokens[1]); i++) {
                    list = list.append(lastCommand);
                }
                return list.map(it -> String.join("\n",
                                                  it));
            }
            if (pattern2.matcher(command)
                        .matches()) {
                return lastCommand.then(s -> IO.lazy(() -> ConsolePrinter.printlnResult(s)))
                                  .repeat(_ -> true,
                                          RetryPolicies.constantDelay(ofMillis(parseInt(tokens[2])))
                                         );
            }

            if (pattern3.matcher(command)
                        .matches()) {
                return lastCommand.then(s -> IO.lazy(() -> ConsolePrinter.printlnResult(s)))
                                  .repeat(_ -> true,
                                          RetryPolicies.constantDelay(ofMillis(parseInt(tokens[2])))
                                                       .limitRetriesByCumulativeDelay(ofMillis(parseInt(tokens[4])))
                                         );
            }
            return IO.fail(new IllegalArgumentException("Not a expected pattern"));

        };
    }

}
