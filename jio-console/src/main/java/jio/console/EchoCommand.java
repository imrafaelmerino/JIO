package jio.console;

import jio.IO;
import jsonvalues.JsObj;

import java.util.function.Function;
import java.util.stream.Collectors;

class EchoCommand extends Command {

    private static final String COMMAND_NAME = "echo";

    public EchoCommand() {
        super(COMMAND_NAME,
              """
                      Prints out the message into the console.
                      Usage: echo {text}
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
            return nArgs == 0 ? IO.fromValue("") : IO.fromValue(Functions.joinTail(tokens));
        };
    }
}
