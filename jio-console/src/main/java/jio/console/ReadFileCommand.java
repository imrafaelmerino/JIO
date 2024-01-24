package jio.console;

import jio.IO;
import jio.RetryPolicies;
import jsonvalues.JsObj;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;

/**
 * Command to read the content from a file with the command:
 * <pre>
 *     file-read /path/to/file
 * </pre>
 * <p>
 * Users can specify the absolute path to the file they want to read, and the command will return the file's contents as
 * a string stored in the variable 'output'. If the file is not found, the command allows for multiple retries.
 * <p>
 * Examples:
 * <pre>
 *     file-read /Users/username/json.txt
 *     file-read $var
 * </pre>
 *
 * @see Command
 */
class ReadFileCommand extends Command {

  private static final String COMMAND_NAME = "file-read";

  public ReadFileCommand() {
    super(COMMAND_NAME,
          """
              Reads the content from a file.
              Examples:
                  $command /Users/username/json.txt
                  $command $var""".replace("$command",
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
        return Programs.ASK_FOR_INPUT(new Programs.AskForInputParams("Type de absolute path of the file",
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
    Path file = Paths.get(path);
    if (!file.toFile()
             .isFile()) {
      return IO.fail(new InvalidCommand(this,
                                        "File " + path + " not found"));
    }
    try {
      List<String> lines = Files.readAllLines(file);
      return IO.succeed(String.join("\n",
                                    lines));
    } catch (IOException e) {
      return IO.fail(new InvalidCommand(this,
                                        e.getMessage()
                     )
                    );
    }

  }
}
