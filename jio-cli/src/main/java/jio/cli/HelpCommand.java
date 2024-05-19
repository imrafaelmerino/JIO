package jio.cli;

import jio.IO;
import jsonvalues.JsObj;

import java.util.List;
import java.util.function.Function;

/**
 * Represents a command that provides descriptions of other commands. Usage: {@code help command_name}
 * <p>
 * This command allows users to obtain descriptions of specified commands. It takes a list of available commands in its
 * constructor to provide descriptions for them. Users can use the following syntax to get the description of a
 * command:
 *
 * <pre>
 * help command_name
 * </pre>
 * <p>
 * If no command name is provided, it will display a message prompting users to type the name of a command or "list" to
 * see all possible commands.
 */
class HelpCommand extends Command {

  private static final String COMMAND_NAME = "help";
  private final List<Command> commands;

  public HelpCommand(List<Command> commands) {
    super(COMMAND_NAME,
          """
          Welcome to jio-cli:
            - To view a list of all available commands, type `list`.
            - To get detailed information about a specific command, type `help <command>`.
            - You can create and save variables using: `var-set <name> <value>`.
            - To read variables, use: `var-get <name>` or `echo $<name>`.
            - The result of the last executed command is stored in a special variable called `output`.
              Note: Some commands do not store anything, depending on their configuration.
            - You can extend jio-cli by writing your own commands!
          """.strip()
);
    this.commands = commands;
  }

  @Override
  public Function<String[], IO<String>> apply(final JsObj conf,
                                              final State state
  ) {
    return tokens -> {
      int nArgs = tokens.length - 1;
      if (nArgs == 0) {
        return IO.succeed(description);
      }
      String commandName = Functions.joinTail(tokens);
      return commands.stream()
                     .filter(it -> it.name.equalsIgnoreCase(commandName))
                     .findFirst()
                     .map(command -> IO.succeed(command.description))
                     .orElse(IO.succeed("Command `%s` doesn't exist.".formatted(commandName)));
    };
  }
}
