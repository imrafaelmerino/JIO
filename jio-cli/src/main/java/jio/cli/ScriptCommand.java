package jio.cli;

import fun.tuple.Pair;
import jio.ExceptionFun;
import jio.IO;
import jio.Result;
import jio.Result.Failure;
import jio.Result.Success;
import jio.RetryPolicies;
import jio.cli.ConsolePrograms.AskForInputParams;
import jsonvalues.JsObj;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;

/**
 * Represents a command to execute a script file containing multiple commands. It reads the specified file and executes
 * all the commands found in it. Usage: - script /path/to/script.txt
 */
final class ScriptCommand extends Command {

    private static final String COMMAND_NAME = "script";
    private final Console console;

    public ScriptCommand(Console console) {
        super(COMMAND_NAME,
              """
                          Reads the specified file and executes all the commands in it.
                      
                          Usage:
                              script <path_to_script>
                      
                          Parameters:
                              <path_to_script>  - The absolute or relative path to the script file containing the commands.
                      
                          Examples:
                              script              (will ask you for the path to the script interactively)
                              script ./scripts/test_commands.txt
                      
                          Description:
                              The 'script' command reads a file containing multiple commands and executes each command sequentially.
                              The file should contain one command per line. Lines that are blank or contain unsupported commands
                              will be ignored with an appropriate message.
                      
                          Notes:
                              - If the <path_to_script> is not provided, you will be prompted to enter it interactively.
                              - Ensure the script file exists and is accessible.
                              - Supported commands in the script file will be executed in the order they appear.
                              - If the script file contains invalid or unsupported commands, they will be reported but execution will continue.
                      """.strip()
             );

        this.console = console;
    }

    @Override
    public Function<String[], IO<String>> apply(final JsObj conf,
                                                final State state
                                               ) {
        return tokens -> {
            int nArgs = tokens.length - 1;
            if (nArgs == 0) {
                return ConsolePrograms.ASK_FOR_INPUT(new AskForInputParams("Type de absolute path of the script",
                                                                           path -> Paths.get(path)
                                                                                        .toFile()
                                                                                        .isFile(),
                                                                           "Script not found",
                                                                           RetryPolicies.limitRetries(3)
                                                     )
                                                    )
                                      .then(path -> execScript(conf,
                                                               Paths.get(path)));
            }

            Path path = Paths.get(Functions.joinTail(tokens));
            File file = path.toFile();
            if (!file.exists()) {
                return IO.fail(new FileNotFoundException(STR."The file '\{file}' doesnt exist"));
            }

            return execScript(conf,
                              path);

        };
    }

    private IO<String> execScript(JsObj conf,
                                  Path path
                                 ) {
        return IO.task(() -> {
            List<String> lines = Files.readAllLines(path);
            List<String> results = lines.stream()
                                        .filter(line -> !line.isBlank())
                                        .map(line -> {
                                                 Pair<Command, IO<String>> commandEffect =
                                                         console.findCommand(conf,
                                                                             line.trim());
                                                 if (commandEffect != null) {
                                                     Result<String> result = commandEffect
                                                             .second()
                                                             .compute();
                                                     return switch (result) {
                                                         case Success<String> s -> s.output();
                                                         case Failure<String> f ->
                                                                 ExceptionFun.findUltimateCause(f.exception())
                                                                             .toString();
                                                     };
                                                 }
                                                 return String.format("The line `%s` is not a valid command",
                                                                      line
                                                                     );
                                             }
                                            )
                                        .toList();
            return String.join("\n",
                               results);
        });


    }
}
