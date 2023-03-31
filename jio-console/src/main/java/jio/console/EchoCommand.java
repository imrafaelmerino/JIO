package jio.console;

import jio.IO;
import jsonvalues.JsObj;

import java.util.function.Function;

class EchoCommand extends Command {

    private static final String COMMAND_NAME = "echo";

    public EchoCommand() {
        super(COMMAND_NAME,
              """
                      Prints out the message into the console.
                      Example:
                          $command hi, how are you doing?
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
                return Programs.ASK_FOR_INPUT(new Programs.AskForInputParams("Type the name of variable"))
                               .then(var -> IO.fromValue(state.variables.get(var)));

            return IO.fromValue(Functions.joinTail(tokens));
        };
    }
}
