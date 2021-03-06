package jio.console;


import fun.tuple.Pair;
import jio.IO;
import jio.time.Clock;
import jsonvalues.JsObj;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Creates a REPL (read eval print loop) program from a list of user commands.
 * It's executed with the method {@link #eval(JsObj)}. The commands returned by
 * the method {@link #getPredefinedCommands()} are always loaded to the console.
 */
public final class Console {

    /**
     * Constructor to create a Console from a list of user commands
     *
     * @param userCommands the list of commands
     */
    public Console(List<Command> userCommands) {
        Objects.requireNonNull(userCommands);
        this.state = new State();
        this.commands = getPredefinedCommands();
        this.commands.addAll(userCommands);
    }

    final State state;
    final List<Command> commands;

    private List<Command> getPredefinedCommands() {
        List<Command> commands = new ArrayList<>();
        commands.add(new ListCommand(commands).setSaveOutput(false));
        commands.add(new ReadVarCommand().setSaveOutput(false));
        commands.add(new SetVarCommand().setSaveOutput(false));
        commands.add(new AddToListCommand().setSaveOutput(false));
        commands.add(new LastCommand());
        commands.add(new HistoryCommand().setSaveOutput(false));
        commands.add(new HelpCommand(commands).setSaveOutput(false));
        commands.add(new DumpCommand().setSaveOutput(false));
        commands.add(new Base64EncodeCommand());
        commands.add(new Base64DecodeCommand());
        commands.add(new EncodeURLCommand());
        commands.add(new ExitCommand());
        commands.add(new JsPairsCommand().setSaveOutput(false));
        commands.add(new JsPrettyCommand());
        commands.add(new JsGetValueCommand());
        commands.add(new ClearCommand().setSaveOutput(false));
        commands.add(new EchoCommand());
        commands.add(new ScriptCommand(this));
        commands.add(new ReadFileCommand());
        return commands;
    }

    /**
     * Executes the console program and the REP (read, eval, print) loop starts executing.
     * A json can be specified, and it will be passed into every command in case some configuration
     * is needed
     *
     * @param conf the configuration json
     */
    public void eval(JsObj conf) {

        for (; ; ) {
            Programs.READ_LINE
                    .then(line -> {
                        if (line.isBlank()) return IO.NULL();
                        String trimmedLine = line.trim();
                        Optional<Pair<Command, IO<String>>> opt = parse(conf, trimmedLine);
                        if (opt.isPresent()) {
                            IO<String> command = opt.get().second()
                                                    .peekSuccess(output -> {
                                                                     if (output != null && opt.get().first().isSaveOutput)
                                                                         state.stringVariables.put("output",
                                                                                                   output
                                                                                                  );
                                                                 }
                                                                );


                            state.historyCommands.add(command);
                            return IO.fromSupplier(Clock.realTime)
                                     .then(tic -> command.map(result -> Pair.of(tic, result)))
                                     .peek(pair ->
                                                   state.historyResults
                                                           .add(String.format("%s, OK, %s ms, %s ",
                                                                              trimmedLine,
                                                                              Duration.ofMillis(System.currentTimeMillis() - pair.first()).toMillis(),
                                                                              Instant.ofEpochMilli(pair.first())
                                                                             )
                                                               ),
                                           error ->
                                                   state.historyResults.add(String.format("%s, KO, %s",
                                                                                          trimmedLine,
                                                                                          Instant.now()
                                                                                         )
                                                                           )
                                          )
                                     .map(Pair::second);
                        }
                        return IO.fromFailure(new CommandNotFoundException(line));
                    })
                    .then(it -> it != null ? Programs.PRINT_NEW_LINE(it + "\n") : IO.NULL(),
                          e -> Programs.PRINT_NEW_LINE(e.getMessage() + "\n")
                         )
                    .join();
        }

    }

    private String[] replaceVars(final State state,
                                 final String[] tokens
                                ) {
        for (int i = 1; i < tokens.length; i++) {
            var token = tokens[i];
            if (token.startsWith("$")) {
                String varName = token.substring(1);
                if (!varName.isEmpty()) {
                    tokens[i] = state.stringVariables.get(varName);
                }
            }
        }
        return tokens;
    }

    Optional<Pair<Command, IO<String>>> parse(JsObj conf,
                                              String line
                                             ) {
        for (Command command : commands) {
            try {
                Optional<IO<String>> opt = command.executeIfMatch(conf, state)
                                                  .apply(replaceVars(state, line.split("\s")));
                if (opt.isPresent()) return Optional.of(Pair.of(command, opt.get()));
            } catch (Exception e) {
                return Optional.of(Pair.of(command, IO.fromFailure(e)));
            }
        }
        return Optional.empty();
    }


}
