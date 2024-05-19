package jio.cli;

import jio.IO;
import jio.RetryPolicies;
import jio.cli.ConsolePrograms.AskForInputParams;
import jsonvalues.JsObj;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.function.Function;

/**
 * Represents a command that writes the content of the output variable into the specified file. If the file exists, it
 * appends the content to the file.
 * <p>
 * Usage: {@code file-dump {path_file}}
 * <p>
 * Examples: {@code file-dump /Users/rmerinogarcia/dump.txt} {@code file-dump $var}
 * <p>
 * The content of the {@code output} variable contains the result of the last command executed (if the command is
 * configured to do so).
 */
final class DumpCommand extends Command {

    private static final String COMMAND_NAME = "file-dump";

    public DumpCommand() {
        super(COMMAND_NAME,
              """
                      Write the content of the output variable into the specified file (appending if the file exists).
                      Usage: $command {path_file}
                      Examples:
                          $command (prompts for for the absolute path of the file)
                          $command /Users/rmerinogarcia/dump.txt""".replace("$command",
                                                                            COMMAND_NAME)
             );
    }

    @Override

    public Function<String[], IO<String>> apply(final JsObj conf,
                                                final State state
                                               ) {
        return tokens -> {
            int nArgs = tokens.length - 1;
            if (nArgs == 0) {
                return ConsolePrograms.ASK_FOR_INPUT(new AskForInputParams("Type de absolute path of the file",
                                                                           path -> {
                                                                               Path parent = Paths.get(path)
                                                                                                  .getParent();

                                                                               return parent != null && parent
                                                                                       .toFile()
                                                                                       .isDirectory();
                                                                           },
                                                                           "Folder not found",
                                                                           RetryPolicies.limitRetries(3)
                                                     )
                                                    )
                                      .then(path -> dumpToFile(state,
                                                               path));
            }

            return dumpToFile(state,
                              Functions.joinTail(tokens));

        };
    }

    private IO<String> dumpToFile(State state,
                                  String path
                                 ) {
        return IO.task(() -> {
            Path file = Paths.get(path);
            if (!file.getParent()
                     .toFile()
                     .isDirectory()) {
                throw new IllegalArgumentException(STR."Folder \{file.getParent()} not found");
            }

            String content = state.variables.getOrDefault("output",
                                                          "");
            Files.writeString(file,
                              content,
                              StandardOpenOption.CREATE,
                              StandardOpenOption.APPEND
                             );

            return "Dumped %s bytes into %s".formatted(content.getBytes(StandardCharsets.UTF_8).length,
                                                       file);
        });
    }
}
