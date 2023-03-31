package jio.console;

import jio.IO;
import jio.RetryPolicies;
import jsonvalues.JsObj;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.function.Function;

class DumpCommand extends Command {

    private static final String COMMAND_NAME = "dump";

    public DumpCommand() {
        super(COMMAND_NAME,
              """
                      Write the content of the output variable into the specified file (appending if the file exists).
                      Examples:
                          $command /Users/rmerinogarcia/dump.txt
                          $command $var""".replace("$command", COMMAND_NAME)
             );
    }

    @Override

    public Function<String[], IO<String>> apply(final JsObj conf,
                                                final State state
                                               ) {
        return tokens -> {
            int nArgs = tokens.length - 1;
            if (nArgs == 0)
                return Programs.ASK_FOR_INPUT(new Programs.AskForInputParams("Type de absolute path of the file",
                                                                             path -> Paths.get(path).getParent().toFile().isDirectory(),
                                                                             "Folder not found",
                                                                             RetryPolicies.limitRetries(3)
                                              )
                                             )
                               .then(path -> dumpToFile(state, path));

            return dumpToFile(state, Functions.joinTail(tokens));


        };
    }


    private IO<String> dumpToFile(State state, String path) {
        try {
            Path file = Paths.get(path);
            if (!file.getParent().toFile().isDirectory())
                return IO.fromFailure(new InvalidCommand(this, "Folder " + file.getParent() + " not found"));

            Files.writeString(file,
                              state.variables.getOrDefault("output", "") + "\n",
                              StandardOpenOption.CREATE,
                              StandardOpenOption.APPEND
                             );
        } catch (IOException e) {
            return IO.fromFailure(e);
        }
        return IO.NULL();
    }
}
