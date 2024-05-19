package jio.cli;

import jio.IO;
import jio.ListExp;
import jsonvalues.JsObj;

import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents a command that lists executed commands and their positions in the command history. Users can execute
 * previous commands again by specifying their positions or intervals. Usage: {@code history [position(s) | interval]}
 * <p>
 * This command allows users to view the command history and optionally execute previous commands by specifying their
 * positions or intervals. The command supports various syntaxes:
 *
 * <pre>
 * history // Lists all executed commands and their positions
 * history 1 // Lists the command at position 1
 * history 1,2 // Lists commands at positions 1 and 2
 * history 1..10 // Lists commands in the interval [1, 10]
 * </pre>
 * <p>
 * The history command provides flexibility in reviewing and re-executing previous commands.
 */
class HistoryCommand extends Command {

    static final String intervalRegex = "\\d+\\.\\.\\d+";
    static final Pattern interval = Pattern.compile(intervalRegex);
    static final String someRegex = "-?\\d+(,-?\\d+)?";
    static final Pattern some = Pattern.compile(someRegex);
    private static final String COMMAND_NAME = "history";

    public HistoryCommand() {
        super(COMMAND_NAME,
              """
                          Lists all executed commands and their positions in the command history.
                          Users can execute previous commands again by specifying their positions or intervals.
                      
                          Usage:
                              history [position(s) | interval]
                      
                          Parameters:
                              [position(s) | interval]  - Optional. Specifies the positions or interval of commands to list.
                                                         Can be a single position, comma-separated positions, or an interval.
                      
                          Examples:
                              history         (lists all executed commands and their positions)
                              history 1       (lists the command at position 1)
                              history 1,2     (lists commands at positions 1 and 2)
                              history 1..10   (lists commands in the interval [1, 10])
                      
                          Description:
                              The 'history' command provides flexibility in reviewing executed commands and re-executing them.
                              Users can specify positions or intervals to list specific commands from the history.
                      
                          Notes:
                              - Positions are 0-indexed, where 0 represents the first executed command.
                              - Ensure that the specified positions or interval are within the bounds of the command history.
                              - Invalid positions or intervals will result in an error message.
                      """.strip());
    }

    @Override
    public Function<String[], IO<String>> apply(final JsObj conf,
                                                final State state
                                               ) {

        return tokens -> {
            int nArgs = tokens.length - 1;
            if (nArgs == 0) {
                int n = state.historyResults.size();
                return n == 0 ? IO.succeed("stack is empty!") : IO.succeed(
                        IntStream.range(0,
                                        n)
                                 .mapToObj(i -> STR."\{i} -> \{state.historyResults.get(i)}")
                                 .collect(Collectors.joining("\n"))
                                                                          );
            } else {
                int size = state.historyCommands.size();
                String token = tokens[1];
                if (some.matcher(token)
                        .matches()) {
                    ListExp<String> list = ListExp.seq();
                    String[] ns = token.split(",",-1);
                    for (String s : ns) {
                        int n = Integer.parseInt(s);
                        if (n > size - 1) {
                            return IO.fail(new IllegalArgumentException("history size " + size));
                        }
                        list = list.append(state.getHistoryCommand(n));
                    }
                    return list.map(it -> String.join("\n",
                                                      it));
                } else if (interval.matcher(token)
                                   .matches()) {
                    String[] bounds = tokens[1].split("\\.\\.",-1);
                    if (bounds.length != 2) {
                        return IO.fail(new IllegalArgumentException("n..m n < m expected"));
                    }
                    int min = Integer.parseInt(bounds[0]);
                    int max = Integer.parseInt(bounds[1]);
                    if (min > max) {
                        return IO.fail(new IllegalArgumentException("n..m n < m expected"));
                    }
                    ListExp<String> list = ListExp.seq();
                    for (int i = min; i <= max; i++) {
                        list = list.append(state.getHistoryCommand(i));
                    }
                    return list.map(it -> String.join("\n",
                                                      it));
                } else {
                    return IO.fail(new IllegalArgumentException(STR."argument doesnt follow the allowed patterns: \{intervalRegex}, \{someRegex}"));
                }

            }

        };

    }

}
