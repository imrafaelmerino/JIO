package jio.cli;

import jio.IO;
import jio.RetryPolicies;
import jio.cli.ConsolePrograms.AskForInputParams;
import jsonvalues.JsObj;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;

/**
 * Command to read the content from a file with the command:
 * <pre>
 * file-read /path/to/file
 * </pre>
 * <p>
 * Users can specify the absolute path to the file they want to read, and the command will return the file's contents as
 * a string stored in the variable 'output'. If the file is not found, the command allows for multiple retries.
 * <p>
 * Examples:
 * <pre>
 * file-read /Users/username/json.txt
 * file-read $var
 * </pre>
 *
 * @see Command
 */
final class ReadFileCommand extends Command {

    private static final String COMMAND_NAME = "file-read";

    public ReadFileCommand() {
        super(COMMAND_NAME,
              """
                          Reads the content from a specified file.
                      
                          Usage:
                              file-read <path_to_file>
                      
                          Parameters:
                              <path_to_file>  - The absolute or relative path to the file to be read.
                      
                          Examples:
                              file-read             (will ask you for the absolute path of the file interactively)
                              file-read /Users/username/json.txt
                              file-read ./files/data.txt
                      
                          Description:
                              The 'file-read' command reads and returns the content of the specified file. If the file
                              is not found, you will be prompted to try again up to three times.
                      
                          Notes:
                              - If the <path_to_file> is not provided, you will be prompted to enter it interactively.
                              - Ensure the file exists and is accessible.
                              - The file contents will be returned as a single string with lines separated by newlines.
                      """
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
                                                                           path -> Paths.get(path)
                                                                                        .toFile()
                                                                                        .isFile(),
                                                                           "File not found",
                                                                           RetryPolicies.limitRetries(3)
                                                     )
                                                    )
                                      .then(this::readFile);
            }
            return readFile(Functions.joinTail(tokens));

        };
    }

    private IO<String> readFile(String path) {
        return IO.task(() -> {
            Path file = Paths.get(path);
            if (!file.toFile()
                     .isFile()) {
                throw new IllegalArgumentException("File %s not found".formatted(path));
            }
            List<String> lines = Files.readAllLines(file);
            return String.join("\n",
                               lines);
        });

    }
}
