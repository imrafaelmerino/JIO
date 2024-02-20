package jio.console;

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
class LastCommand extends Command {

  static final Pattern pattern1 = Pattern.compile("last \\d+");
  static final Pattern pattern2 = Pattern.compile("last every (?<every>\\d+)$");

  static final Pattern pattern3 = Pattern.compile("last every (?<every>\\d+) for (?<for>\\d+)$");

  private static final String COMMAND_NAME = "last";

  public LastCommand() {
    super(COMMAND_NAME,
          """
              Executes the last command one or the specified number of times.
              It's also possible to repeat the command for a specified period in ms.
              Examples:
                  $command
                  $command 3
                  $command every 100
                  $command every 100 for 1000""".replace("$command",
                                                         COMMAND_NAME)
    );
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
        return lastCommand.then(s -> Programs.PRINT_NEW_LINE(s)
                                             .map(it -> s))
                          .repeat(s -> true,
                                  RetryPolicies.constantDelay(ofMillis(parseInt(tokens[2])))
                          );
      }

      if (pattern3.matcher(command)
                  .matches()) {
        return lastCommand.then(s -> Programs.PRINT_NEW_LINE(s)
                                             .map(it -> s)
        )
                          .repeat(s -> true,
                                  RetryPolicies.constantDelay(ofMillis(parseInt(tokens[2])))
                                               .limitRetriesByCumulativeDelay(ofMillis(parseInt(tokens[4])))
                          );
      }
      return IO.fail(new InvalidCommand(this,
                                        "Not a expected pattern"));

    };
  }

}
