package jio.console;

import jio.IO;
import jsonvalues.JsObj;

import java.util.function.Function;

class ClearCommand extends Command {

    private static final String COMMAND_NAME = "clear";

    public ClearCommand() {
        super(COMMAND_NAME,
              "Clear the console");
    }

    @Override
    public Function<String[], IO<String>> apply(final JsObj conf,
                                                final State state
                                               ) {
        return tokens -> {
            int nArgs = tokens.length - 1;
            if (nArgs > 1) return Errors.TOO_MANY_ARGUMENTS.apply(nArgs, this);
            return IO.fromValue(ControlChars.CLEAR.code + ControlChars.RESET.code);
        };
    }
}
