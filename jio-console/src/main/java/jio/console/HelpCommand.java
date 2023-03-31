package jio.console;

import jio.IO;
import jsonvalues.JsObj;

import java.util.List;
import java.util.function.Function;

class HelpCommand extends Command {

    private static final String COMMAND_NAME = "help";
    private final List<Command> commands;

    public HelpCommand(List<Command> commands) {
        super(COMMAND_NAME,
              """
                 Prints out the description of the specified command.
                 Examples:
                     help list
                     help dump""");
        this.commands = commands;
    }

    @Override
    public Function<String[], IO<String>> apply(final JsObj conf,
                                                final State state
                                               ) {
        return tokens -> {
            int nArgs = tokens.length - 1;
            if (nArgs == 0) return IO.fromValue("Type the name of a command or list to see al the possible commands");
            String commandName = Functions.joinTail(tokens);
            return commands.stream()
                           .filter(it -> it.name.equalsIgnoreCase(commandName))
                           .findFirst()
                           .map(command -> IO.fromValue(command.description))
                           .orElse(IO.fromFailure(new CommandNotFoundException(commandName)));
        };
    }
}
