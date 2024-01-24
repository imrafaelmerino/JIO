package jio.console;

import jio.IO;
import jsonvalues.JsObj;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a command to list all possible commands, optionally filtered by a specified prefix. It provides users with
 * a list of available commands and allows them to filter the results by prefix.
 * <p>
 * Examples of valid input patterns: - List all possible commands: "list" - List commands filtered by a prefix: "list
 * js"
 *
 * @see Command
 */
class ListCommand extends Command {

  private static final String COMMAND_NAME = "list";
  private final List<Command> commands;

  public ListCommand(List<Command> commands) {
    super(COMMAND_NAME,
          """
              List all possible commands. A prefix can be specified to filter out the results.
              Examples:
                  $command
                  $command js""".replace("$command",
                                         COMMAND_NAME));
    this.commands = commands;
  }

  @Override
  public Function<String[], IO<String>> apply(final JsObj conf,
                                              final State state
                                             ) {
    return tokens ->
    {
      int nArgs = tokens.length - 1;
        if (nArgs > 1) {
            return Errors.TOO_MANY_ARGUMENTS.apply(nArgs,
                                                   this);
        }
      List<String> list =
          commands.stream()
                  .map(it -> it.name)
                  .sorted(Comparator.naturalOrder())
                  .collect(Collectors.toList());
      return tokens.length == 1 ?
             IO.succeed(String.join("\n",
                                    list)) :
             IO.succeed(list.stream()
                            .filter(it -> it.startsWith(tokens[1]))
                            .collect(Collectors.joining("\n"))
                       );
    };
  }
}
