package jio.console;

import jio.IO;
import jsonvalues.JsObj;
import jsonvalues.JsPath;
import jsonvalues.Json;

import java.util.function.Function;

/**
 * Class to create different commands to execute interactive programs that allows the user to compose a Json
 * given a provided spec. The constructor takes three arguments: the command name and description,
 * and the program that interacts with the user to compose the Json. To execute the command:
 *
 * <pre>
 *     json command_name
 * </pre>
 *
 * To get some help about the program and show the description:
 * <pre>
 *     help json command_name
 * </pre>
 *
 */
public class JsObjConsoleCommand extends Command {
    private final JsConsole<? extends Json<?>> program;
    private static final String COMMAND_NAME = "json";


    /**
     * Constructor to create a JsObjConsoleCommand
     * @param name the name of the command
     * @param description the description (will show up if the user types in the help command)
     * @param objConsole the program that composes the Json
     *
     * @see JsObjConsole
     */
    public JsObjConsoleCommand(final String name,
                               final String description,
                               final JsObjConsole objConsole
                              ) {
        super(String.format("%s %s",
                            COMMAND_NAME,
                            name),
              description,
              tokens ->
                      tokens.length == 2
                              && tokens[0].equalsIgnoreCase(COMMAND_NAME)
                              && tokens[1].equalsIgnoreCase(name));
        this.program = objConsole;
    }

    @Override
    public Function<String[], IO<String>> apply(final JsObj conf,
                                                final State state
                                               ) {
        return tokens -> program.apply(JsPath.empty())
                                .map(Json::toPrettyString);
    }
}
