package jio.console;

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
 *     help command_name
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
                       Welcome to jio-console:
                         . To know the list of available commands, type `list`
                         . To know more about a specific command, type `help $command`
                         . You can create variables and save them: `var-set name Rafa`
                         . You can read variables: `var-get name` or `echo $name`
                         . The result of the last executed command is stored in special variable called `output`
                            But some commands doesn't store anything (configurable when creating the command)
                         . You can extend jio-console and write your own commands!
                      """);
        this.commands = commands;
    }

    @Override
    public Function<String[], IO<String>> apply(final JsObj conf,
                                                final State state
                                               ) {
        return tokens -> {
            int nArgs = tokens.length - 1;
            if (nArgs == 0) return IO.succeed("Type the name of a command or list to see al the possible commands");
            String commandName = Functions.joinTail(tokens);
            return commands.stream()
                           .filter(it -> it.name.equalsIgnoreCase(commandName))
                           .findFirst()
                           .map(command -> IO.succeed(command.description))
                           .orElse(IO.fail(new CommandNotFoundException(commandName)));
        };
    }
}
