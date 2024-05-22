package jio.cli;

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
final class ListCommand extends Command {

    private static final String COMMAND_NAME = "list";
    private final List<Command> commands;

    public ListCommand(List<Command> commands) {
        super(COMMAND_NAME,
              """
                          Lists all possible commands and their aliases (if defined). A prefix can be specified to filter the results.
                                            
                          Usage:
                              list [prefix]
                                            
                          Parameters:
                              [prefix]  - Optional. A string to filter commands that start with the specified prefix.
                                            
                          Examples:
                              list            (lists all available commands)
                              list json       (lists all commands starting with 'json')
                                            
                          Notes:
                              - If no prefix is specified, all commands will be listed.
                              - The list of commands is sorted alphabetically.
                      """);
        this.commands = commands;
    }

    @Override
    public Function<String[], IO<String>> apply(final JsObj conf,
                                                final State state
                                               ) {
        return tokens -> {
            List<String> list = commands.stream()
                                        .map(it -> "%-25s %s".formatted(it.name, it.alias != null ? "(%s)".formatted(it.alias) : ""))
                                        .sorted(Comparator.naturalOrder())
                                        .collect(Collectors.toList());
            if (tokens.length == 1) return IO.succeed(String.join("\n",
                                                                  list)
                                                     );
            String name = tokens[1];
            List<String> availableCommands = list.stream()
                                                 .filter(it -> it.startsWith(name))
                                                 .collect(Collectors.toList());
            if (availableCommands.isEmpty())
                return IO.succeed("No command starts with `%s`. Type `list` to see all the available commands.".formatted(name));
            return IO.succeed(String.join("\n", availableCommands));
        };
    }
}
