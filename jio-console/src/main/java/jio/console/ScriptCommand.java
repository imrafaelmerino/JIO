package jio.console;

import fun.tuple.Pair;
import jio.IO;
import jio.RetryPolicies;
import jsonvalues.JsObj;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

class ScriptCommand extends Command {
    private final Console console;

    private static final String COMMAND_NAME = "script";


    public ScriptCommand(Console console) {
        super(COMMAND_NAME,
              """
                      Reads the specified file and executes all the commands.
                      jio.chatgpt.Examples:
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
            if (nArgs == 0)
                return Programs.ASK_FOR_INPUT(new Programs.AskForInputParams("Type de absolute path of the script",
                                                                             path -> Paths.get(path).toFile().isFile(),
                                                                             "Script not found",
                                                                             RetryPolicies.limitRetries(3)
                                              )
                                             )
                               .then(path -> execScript(conf, Paths.get(path)));

            Path path = Paths.get(Functions.joinTail(tokens));
            File file = path.toFile();
            if (!file.exists())
                return IO.fromFailure(new FileNotFoundException("The file '" + file + "' doesnt exist"));
            return execScript(conf, path);


        };
    }

    private IO<String> execScript(JsObj conf, Path path) {
        try {
            List<String> lines = Files.readAllLines(path);
            List<IO<String>> list =
                    lines.stream()
                         .filter(line -> !line.isBlank())
                         .map(line -> {
                                  Optional<Pair<Command, IO<String>>> opt = console.parse(conf, line.trim());
                                  if (opt.isPresent()) return opt.get().second();
                                  return IO.fromValue(String.format("The line %s is not a supported command",
                                                                    line
                                                                   )
                                                     );
                              }
                             )
                         .toList();

            return list.stream()
                       .reduce(IO.fromValue(""),
                               (a, b) -> a.then(as -> b.map(bs -> as + "\n" + bs))
                              );
        } catch (IOException e) {
            return IO.fromFailure(new InvalidCommand(this, e.getMessage()));
        }

    }
}
