package jio.cli;

import jio.IO;
import jsonvalues.JsObj;
import jsonvalues.JsPath;
import jsonvalues.Json;

import java.util.function.Function;

/**
 * Class to create different commands to execute interactive programs that allow the user to compose a JSON given a
 * provided spec. The constructor takes three arguments: the command name and description, and the program that
 * interacts with the user to compose the JSON. To execute the command:
 *
 * <pre>
 * json command_name
 * </pre>
 * <p>
 * To get some help about the program and show the description:
 * <pre>
 * help json command_name
 * </pre>
 *
 * @see JsObjConsole
 */
public class JsObjConsoleCommand extends Command {

  private static final String COMMAND_NAME = "json-console";
  private final JsConsole<? extends Json<?>> program;

  /**
   * Constructor to create a JsObjConsoleCommand.
   *
   * @param name        the name of the command
   * @param description the description (will show up if the user types in the help command)
   * @param objConsole  the program that composes the JSON
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
          tokens -> tokens.length == 2
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
