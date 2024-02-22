package jio.console;

import fun.tuple.Pair;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import jio.IO;
import jio.RetryPolicies;
import jsonvalues.JsObj;

/**
 * Represents a command to execute a script file containing multiple commands. It reads the specified file and executes
 * all the commands found in it. Usage: - script /path/to/script.txt
 */
class ScriptCommand extends Command {

  private static final String COMMAND_NAME = "script";
  private final Console console;

  public ScriptCommand(Console console) {
    super(COMMAND_NAME,
          """
              Reads the specified file and executes all the commands.
              Examples:
                  $command /Users/username/myscript.txt""".replace("$command",
                                                                   COMMAND_NAME
                                                                  )
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
        return Programs.ASK_FOR_INPUT(new Programs.AskForInputParams("Type de absolute path of the script",
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
        return IO.fail(new FileNotFoundException("The file '" + file + "' doesnt exist"));
      }
      return execScript(conf,
                        path);

    };
  }

  private IO<String> execScript(JsObj conf,
                                Path path) {
    try {
      List<String> lines = Files.readAllLines(path);
      List<IO<String>> list = lines.stream()
                                   .filter(line -> !line.isBlank())
                                   .map(line -> {
                                          Optional<Pair<Command, IO<String>>> opt = console.parse(conf,
                                                                                                  line.trim());
                                          if (opt.isPresent()) {
                                            return opt.get()
                                                      .second();
                                          }
                                          return IO.succeed(String.format("The line %s is not a supported command",
                                                                          line
                                                                         )
                                                           );
                                        }
                                       )
                                   .toList();

      return list.stream()
                 .reduce(IO.succeed(""),
                         (a,
                          b) -> a.then(as -> b.map(bs -> as + "\n" + bs))
                        );
    } catch (IOException e) {
      return IO.fail(new InvalidCommand(this,
                                        e.getMessage()));
    }

  }
}
