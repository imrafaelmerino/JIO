package jio.console;

import jio.IO;
import jio.ListExp;
import jsonvalues.JsObj;

import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class HistoryCommand extends Command {

    private static final String COMMAND_NAME = "history";

    public HistoryCommand() {
        super(COMMAND_NAME,
              """
                 Lists all the executed commands and their position. It's possible to execute them
                 again passing in a list of positions or a interval.
                 Examples:
                     $command
                     $command 1
                     $command 1,2
                     $command 1..10""".replace("$command",COMMAND_NAME));
    }

    static final String intervalRegex = "\\d+\\.\\.\\d+";
    static final Pattern interval = Pattern.compile(intervalRegex);


    static final String someRegex = "-?\\d+(,-?\\d+)?";

    static final Pattern some = Pattern.compile(someRegex);


    @Override
    public Function<String[], IO<String>> apply(final JsObj conf,
                                                final State state
                                               ) {

        return tokens -> {
            int nArgs = tokens.length - 1;
            if (nArgs > 1) return Errors.TOO_MANY_ARGUMENTS.apply(nArgs, this);

            if (nArgs == 0) {
                int n = state.historyResults.size();
                return n == 0 ?
                        IO.fromValue("stack is empty!") :
                        IO.fromValue(
                                IntStream.range(0, n)
                                         .mapToObj(i -> i + " -> " + state.historyResults.get(i))
                                         .collect(Collectors.joining("\n"))
                                    );
            } else {
                int size = state.historyCommands.size();
                String token = tokens[1];
                if (some.matcher(token).matches()) {
                    ListExp<String> list = ListExp.seq();
                    String[] ns = token.split(",");
                    for (String s : ns) {
                        int n = Integer.parseInt(s);
                        if (n > size - 1) return IO.fromFailure(new IllegalArgumentException("history size " + size));
                        list = list.append(state.getHistoryCommand(n));
                    }
                    return list.map(it -> String.join("\n", it));
                } else if (interval.matcher(token).matches()) {
                    String[] bounds = tokens[1].split("\\.\\.");
                    if (bounds.length != 2) return IO.fromFailure(new IllegalArgumentException("n..m n<m expected"));
                    int min = Integer.parseInt(bounds[0]);
                    int max = Integer.parseInt(bounds[1]);
                    if (min > max) return IO.fromFailure(new IllegalArgumentException("n..m n<m expected"));
                    ListExp<String> list = ListExp.seq();
                    for (int i = min; i <= max; i++) list = list.append(state.getHistoryCommand(i));
                    return list.map(it -> String.join("\n", it));
                } else
                    return IO.fromFailure(new InvalidCommand(this, "argument doesnt follow the allowed patterns: " + intervalRegex + ", " + someRegex));

            }

        };


    }

}
