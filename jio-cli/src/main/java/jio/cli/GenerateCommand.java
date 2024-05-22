package jio.cli;

import fun.gen.Gen;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import jio.IO;
import jsonvalues.JsObj;

/**
 * Represents a command that generates random data using a provided generator. Usage: {@code gen command_name}
 * <p>
 * This command is used to execute interactive programs that generate random data. It takes three arguments in its
 * constructor: the command name, a description (shown in the help command), and a generator. To execute the command,
 * users can use the following syntax:
 *
 * <pre>
 * gen generator_name
 * </pre>
 * <p>
 * To get help about the program and view its description, users can type:
 *
 * <pre>
 * help gen command_name
 * </pre>
 * <p>
 * The generated data is obtained from the provided generator when the command is executed.
 */
public class GenerateCommand extends Command {

  private static final String PREFIX_COMMAND = "gen";
  private final Supplier<String> gen;

  /**
   * Constructs a new {@code GenerateCommand} with the specified name, description, and generator.
   *
   * @param name        the name of the command
   * @param description the description (shown in the help command)
   * @param gen         the generator for generating random data
   */
  public GenerateCommand(final String name,
                         final String description,
                         final Gen<String> gen
                        ) {
    super(String.format("%s %s",
                        PREFIX_COMMAND,
                        name
                       ),
          description,
          tokens -> tokens.length == 2
                    && tokens[0].equalsIgnoreCase(PREFIX_COMMAND)
                    && tokens[1].equalsIgnoreCase(name)
         );
    this.gen = gen.apply(new Random());
  }

  /**
   * Returns a function that takes an array of tokens representing user input (unused in this command) and generates
   * random data using the provided generator.
   *
   * @param conf  the configuration (unused in this command)
   * @param state the state (unused in this command)
   * @return a function that generates random data
   */
  @Override
  public Function<String[], IO<String>> apply(final JsObj conf,
                                              final State state
                                             ) {
    return _ -> IO.lazy(gen);

  }
}
