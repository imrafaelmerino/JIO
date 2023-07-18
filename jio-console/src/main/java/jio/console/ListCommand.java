package jio.console;

import jio.IO;
import jsonvalues.JsObj;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

class ListCommand extends Command {
    private static final String COMMAND_NAME = "list";
    private final List<Command> commands;

    public ListCommand(List<Command> commands) {
        super(COMMAND_NAME,
              """
                 List all possible commands. A prefix can be specified to filter out the results.
                 jio.chatgpt.Examples:
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
            if (nArgs > 1) return Errors.TOO_MANY_ARGUMENTS.apply(nArgs,
                                                                  this);
            List<String> list =
                    commands.stream()
                            .map(it -> it.name)
                            .sorted(Comparator.naturalOrder())
                            .collect(Collectors.toList());
            return tokens.length == 1 ?
                    IO.fromValue(String.join("\n", list)) :
                    IO.fromValue(list.stream()
                                     .filter(it -> it.startsWith(tokens[1]))
                                     .collect(Collectors.joining("\n"))
                                );
        };
    }
}
