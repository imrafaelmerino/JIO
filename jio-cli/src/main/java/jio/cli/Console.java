package jio.cli;

import fun.tuple.Pair;
import jio.ExceptionFun;
import jio.IO;
import jio.Result;
import jio.time.Clock;
import jsonvalues.JsObj;
import jsonvalues.JsObjPair;
import jsonvalues.JsPath;
import jsonvalues.spec.JsObjSpec;
import jsonvalues.spec.JsSpecs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
 * final State state;o the constructor.</p>
 */
public final class Console {

    private static final String CONF_FIELD = "conf";
    private static final String SESSION_FILE_DIR_FIELD = "session_file_dir";
    private static final String COLORS_FIELD = "colors";
    private static final String WELCOME_MESSAGE_FIELD = "welcome_message";
    private static final String prompt_field = "prompt";
    private static final String result_field = "result";
    private final static String error_field = "error";
    private final static String aliases_field = "aliases";
    private final static JsObjSpec confSpec = JsObjSpec.of("conf",
                                                           JsObjSpec.of("aliases", JsSpecs.mapOfStr(),
                                                                        "welcome_message", JsSpecs.str(),
                                                                        "session_file_dir", JsSpecs.str(),
                                                                        "colors", JsObjSpec.of("error", JsSpecs.str(),
                                                                                               "result", JsSpecs.str(),
                                                                                               "prompt", JsSpecs.str()
                                                                                              )
                                                                       )
                                                                    .withAllOptKeys()
                                                                    .lenient()
                                                          )
                                                       .withOptKeys("conf")
                                                       .lenient();
    static String welcomeMessage = "Welcome to jio-cli!";
    final State state;
    final List<Command> commands;
    //special command executed when the user typed a invalid command to list the
    //available options
    final Command listCommand;

    /**
     * Constructor to create a Console from a list of user commands.
     *
     * @param userCommands the list of commands
     */
    public Console(List<Command> userCommands) {
        Objects.requireNonNull(userCommands);
        this.state = new State();
        this.commands = getPredefinedCommands();
        listCommand = new ListCommand(commands).setSaveOutput(false);
        this.commands.add(listCommand);
        this.commands.addAll(userCommands);
    }

    private void init(final JsObj conf, List<Command> commands) {
        String logFileDir = conf.getStr(JsPath.path("/%s/%s".formatted(CONF_FIELD, SESSION_FILE_DIR_FIELD)));
        if (logFileDir != null && !logFileDir.isEmpty() && !logFileDir.isBlank()) {
            Path logFilePath = Path.of(logFileDir);
            if (!Files.exists(logFilePath)) {
                throw new IllegalArgumentException("The folder `%s` doesn't exist".formatted(logFileDir));
            }
            String logFile = ".jio-cli-session-%s.log".formatted(Instant.now()
                                                                        .getEpochSecond());
            ConsoleLogger.logFile = Path.of(logFileDir,
                                            logFile);
        }
        String promptColor = conf.getStr(JsPath.path("/%s/%s/%s".formatted(CONF_FIELD, COLORS_FIELD, prompt_field)));
        if (promptColor != null && !promptColor.isEmpty() && !promptColor.isBlank()) {
            ConsolePrinter.promptColor = promptColor;
        }
        String resultColor = conf.getStr(JsPath.path("/%s/%s/%s".formatted(CONF_FIELD, COLORS_FIELD, result_field)));
        if (resultColor != null && !resultColor.isEmpty() && !resultColor.isBlank()) {
            ConsolePrinter.successResultColor = resultColor;
        }

        String errorColor = conf.getStr(JsPath.path("/%s/%s/%s".formatted(CONF_FIELD, COLORS_FIELD, error_field)));
        if (errorColor != null && !errorColor.isEmpty() && !errorColor.isBlank()) {
            ConsolePrinter.errorResultColor = errorColor;
        }

        String welcomeMessageConf = conf.getStr(JsPath.path("/%s/%s".formatted(CONF_FIELD, WELCOME_MESSAGE_FIELD)));
        if (welcomeMessageConf != null) welcomeMessage = welcomeMessageConf;

        JsObj aliases = conf.getObj(JsPath.path("/%s/%s".formatted(CONF_FIELD, aliases_field)));
        if (aliases != null) {
            for (JsObjPair commandAlias : aliases) {
                String commandName = commandAlias.key();
                String alias = commandAlias.value().toJsStr().value;
                for (Command command : commands) {
                    if (command.name.equalsIgnoreCase(commandName))
                        command.setAlias(alias);
                }
            }
        }

    }

    private List<Command> getPredefinedCommands() {
        List<Command> commands = new ArrayList<>();
        commands.add(new ReadVarCommand().setSaveOutput(false));
        commands.add(new SetVarCommand().setSaveOutput(false));
        commands.add(new LastCommand());
        commands.add(new HistoryCommand().setSaveOutput(false));
        commands.add(new HelpCommand(commands).setSaveOutput(false));
        commands.add(new DumpCommand().setSaveOutput(false));
        commands.add(new Base64EncodeCommand());
        commands.add(new ClearVarCommand().setSaveOutput(false));
        commands.add(new Base64DecodeCommand());
        commands.add(new EncodeURLCommand());
        commands.add(new ExitCommand());
        commands.add(new JsPairsCommand().setSaveOutput(false));
        commands.add(new JsPrettyCommand().setSaveOutput(false));
        commands.add(new JsGetValueCommand());
        commands.add(new ClearCommand().setSaveOutput(false));
        commands.add(new EchoCommand());
        commands.add(new ScriptCommand(this));
        commands.add(new ReadFileCommand());
        commands.add(new PrintConfCommand());
        return commands;
    }

    /**
     * Executes the console program and the REP (read, eval, print) loop starts executing. A JSON can be specified, and
     * it will be passed into every command in case some configuration is needed.
     *
     * @param conf the configuration JSON
     */
    public void eval(JsObj conf) {
        var errors = confSpec.test(conf);
        if (!errors.isEmpty())
            throw new IllegalArgumentException("Invalid configuration file: %s".formatted(errors.toString()));
        init(conf, commands);
        System.out.println(ControlChars.CLEAR.code + ControlChars.RESET.code);
        ConsolePrinter.printlnPrompt(welcomeMessage);
        while (true) {
            ConsolePrinter.printPrompt("~ ");
            var result = ConsoleReaders.READ_LINE
                    .then(line -> executeCommand(conf,
                                                 line)
                         )
                    .compute();

            switch (result) {
                case Result.Success<String> s -> {
                    if (s.output() != null) {
                        ConsolePrinter.printlnResult("%n%s%n".formatted(s.output()));
                    }
                }
                case Result.Failure<String> f ->
                        ConsolePrinter.printlnError("%n%s%n".formatted(ExceptionFun.findUltimateCause(f.exception())));
            }

        }

    }

    public IO<String> executeCommand(final JsObj conf,
                                     final String commandName
                                    ) {
        if (commandName == null || commandName.isEmpty() || commandName.isBlank()) {
            return IO.NULL();
        }
        String trimmedCommandName = commandName.trim();
        Pair<Command, IO<String>> opt = parse(conf,
                                              trimmedCommandName);
        IO<String> command = opt
                .second()
                .peekSuccess(output -> {
                                 if (output != null && opt
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
                                                            result))
                      )
                 .peek(pair -> state
                               .historyResults
                               .add(String.format("%-40s %-5s %-20s",
                                                  trimmedCommandName,
                                                  "OK",
                                                  "%d ms".formatted(Duration.ofMillis(System.currentTimeMillis()
                                                                                      - pair.first())
                                                                            .toMillis())
                                                 )
                                   ),
                       _ -> state
                               .historyResults
                               .add(String.format("%-30s %-5s",
                                                  trimmedCommandName,
                                                  "KO"
                                                 )
                                   )
                      )
                 .map(Pair::second);

    }

    private String[] replaceVars(final State state,
                                 final String[] commandTokens
                                ) {
        for (int i = 1; i < commandTokens.length; i++) {
            var token = commandTokens[i];
            if (token.startsWith("$")) {
                String varName = token.substring(1);
                if (!varName.isEmpty()) {
                    commandTokens[i] = state.variables.get(varName);
                }
            }
        }
        return commandTokens;
    }

    Pair<Command, IO<String>> parse(JsObj conf,
                                    String commandLine
                                   ) {
        Pair<Command, IO<String>> command = findCommand(conf, commandLine);
        if (command != null) return command;
        //no command is found, execute list command to find any posible command starting with the chars
        //typed by the user
        commandLine = "list %s".formatted(commandLine);

        IO<String> effect =
                listCommand.apply(conf, state).apply(replaceVars(state,
                                                                 commandLine.split(" "))
                                                    );
        return Pair.of(listCommand, effect);

    }

    Pair<Command, IO<String>> findCommand(JsObj conf,
                                          String commandLine
                                         ) {
        for (Command command : commands) {
            try {
                IO<String> effect =
                        command.createEffect(conf,
                                             state
                                            )
                               .apply(replaceVars(state,
                                                  commandLine.split(" ")
                                                 )
                                     );
                if (effect != null) {
                    return Pair.of(command,
                                   effect
                                  );
                }
            } catch (Exception e) {
                return Pair.of(command,
                               IO.fail(e)
                              );
            }
        }
        return null;
    }

}
