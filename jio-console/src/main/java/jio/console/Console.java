package jio.console;

import fun.tuple.Pair;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import jio.IO;
import jio.time.Clock;
import jsonvalues.JsObj;

/**
 * Creates a REPL (read eval print loop) program from a list of user commands. It's executed with the method
 * {@link #eval(JsObj)}. The commands returned by the method {@link #getPredefinedCommands()} are always loaded to the
 * console.
 *
 * <p>Predefined Commands:</p>
 * <ul>
 * <li>{@link ListCommand}: Lists available commands.</li>
 * <li>{@link ReadVarCommand}: Reads a variable output.</li>
 * <li>{@link SetVarCommand}: Sets a variable output.</li>
 * <li>{@link LastCommand}: Executes the last command.</li>
 * <li>{@link HistoryCommand}: Shows the command history.</li>
 * <li>{@link HelpCommand}: Displays help for available commands.</li>
 * <li>{@link DumpCommand}: Dumps the current state.</li>
 * <li>{@link Base64EncodeCommand}: Encodes a string to Base64.</li>
 * <li>{@link Base64DecodeCommand}: Decodes a Base64 string.</li>
 * <li>{@link AddToListCommand}: Adds a output into a list variable.</li>
 * <li>{@link ClearVarCommand}: Removes a variable from the state.</li>
 * <li>{@link EncodeURLCommand}: Encodes a URL.</li>
 * <li>{@link ExitCommand}: Exits the console program.</li>
 * <li>{@link JsPairsCommand}: Lists key-output pairs of a JSON object.</li>
 * <li>{@link JsPrettyCommand}: Pretty-prints a JSON object.</li>
 * <li>{@link JsGetValueCommand}: Gets a output from a JSON object.</li>
 * <li>{@link ClearCommand}: Clears the console screen.</li>
 * <li>{@link EchoCommand}: Displays a message.</li>
 * <li>{@link ScriptCommand}: Executes a script.</li>
 * <li>{@link ReadFileCommand}: Reads a file.</li>
 * </ul>
 *
 * <p>To add new commands, create classes that implement the {@link Command} interface
 * or extend existing command classes. Then, add these command instances to the list
 * of user commands passed to the constructor.</p>
 */
public final class Console {

  final State state;
  final List<Command> commands;

  /**
   * Constructor to create a Console from a list of user commands.
   *
   * @param userCommands the list of commands
   */
  public Console(List<Command> userCommands) {
    Objects.requireNonNull(userCommands);
    this.state = new State();
    this.commands = getPredefinedCommands();
    this.commands.addAll(userCommands);
  }

  private List<Command> getPredefinedCommands() {
    List<Command> commands = new ArrayList<>();
    commands.add(new ListCommand(commands).setSaveOutput(false));
    commands.add(new ReadVarCommand().setSaveOutput(false));
    commands.add(new SetVarCommand().setSaveOutput(false));
    commands.add(new LastCommand());
    commands.add(new HistoryCommand().setSaveOutput(false));
    commands.add(new HelpCommand(commands).setSaveOutput(false));
    commands.add(new DumpCommand().setSaveOutput(false));
    commands.add(new Base64EncodeCommand());
    commands.add(new AddToListCommand());
    commands.add(new ClearVarCommand());
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
   * Executes the console program and the REP (read, eval, print) loop starts executing. A JSON can be specified, and it
   * will be passed into every command in case some configuration is needed.
   *
   * @param conf the configuration JSON
   */
  public void eval(JsObj conf) {
    System.out.println("""
             ___ ___ _______      _______ _______ __    _ _______ _______ ___     _______\s
            |   |   |       |    |       |       |  |  | |       |       |   |   |       |
            |   |   |   _   |____|       |   _   |   |_| |  _____|   _   |   |   |    ___|
            |   |   |  | |  |____|       |  | |  |       | |_____|  | |  |   |   |   |___\s
         ___|   |   |  |_|  |    |      _|  |_|  |  _    |_____  |  |_|  |   |___|    ___|
        |       |   |       |    |     |_|       | | |   |_____| |       |       |   |___\s
        |_______|___|_______|    |_______|_______|_|  |__|_______|_______|_______|_______|""");
    for (;;) {
      var unused = Programs.READ_LINE
                                     .then(line -> {
                                       if (line.isBlank()) {
                                         return IO.NULL();
                                       }
                                       String trimmedLine = line.trim();
                                       Optional<Pair<Command, IO<String>>> opt = parse(conf,
                                                                                       trimmedLine);
                                       if (opt.isPresent()) {
                                         IO<String> command = opt.get()
                                                                 .second()
                                                                 .peekSuccess(output -> {
                                                                   if (output != null && opt.get()
                                                                                            .first().isSaveOutput) {
                                                                     state.variables.put("output",
                                                                                         output
                                                                     );
                                                                   }
                                                                 }
                                                                 );

                                         state.historyCommands.add(command);
                                         return IO.lazy(Clock.realTime)
                                                  .then(tic -> command.map(result -> Pair.of(tic,
                                                                                             result)))
                                                  .peek(pair -> state.historyResults
                                                                                    .add(String.format("%s, OK, %s ms, %s ",
                                                                                                       trimmedLine,
                                                                                                       Duration.ofMillis(System.currentTimeMillis()
                                                                                                                         - pair.first())
                                                                                                               .toMillis(),
                                                                                                       Instant.ofEpochMilli(pair.first())
                                                                                    )
                                                                                    ),
                                                        error -> state.historyResults.add(String.format("%s, KO, %s",
                                                                                                        trimmedLine,
                                                                                                        Instant.now()
                                                        )
                                                        )
                                                  )
                                                  .map(Pair::second);
                                       }
                                       return IO.fail(new CommandNotFoundException(line));
                                     })
                                     .then(it -> it != null ? Programs.PRINT_NEW_LINE(it + "\n") : IO.NULL(),
                                           e -> Programs.PRINT_NEW_LINE(e.getMessage() + "\n")
                                     )
                                     .result();
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
          tokens[i] = state.variables.get(varName);
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
        Optional<IO<String>> opt = command.executeIfMatch(conf,
                                                          state)
                                          .apply(replaceVars(state,
                                                             line.split(" ")));
        if (opt.isPresent()) {
          return Optional.of(Pair.of(command,
                                     opt.get()));
        }
      } catch (Exception e) {
        return Optional.of(Pair.of(command,
                                   IO.fail(e)));
      }
    }
    return Optional.empty();
  }

}
