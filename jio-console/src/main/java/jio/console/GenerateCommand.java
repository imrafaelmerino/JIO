package jio.console;

import fun.gen.Gen;
import jio.IO;
import jsonvalues.JsObj;

import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Class to create different commands to execute interactive programs that generates random data
 * given a provided generator. The constructor takes three arguments: the command name and description,
 * and the generator. To execute the command:
 *
 * <pre>
 *     gen command_name
 * </pre>
 * <p>
 * To get some help about the program and show the description:
 * <pre>
 *     help gen command_name
 * </pre>
 */
public class GenerateCommand extends Command {
    private final Supplier<String> gen;

    private static final String PREFIX_COMMAND = "gen";


    /**
     * Constructor to create a GenerateCommand
     *
     * @param name        the name of the command
     * @param description the description (will show up if the user types in the help command)
     * @param gen         the generator
     * @see JsObjConsole
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
              tokens ->
                      tokens.length == 2
                              && tokens[0].equalsIgnoreCase(PREFIX_COMMAND)
                              && tokens[1].equalsIgnoreCase(name)
             );
        this.gen = gen.apply(new Random());
    }

    @Override
    public Function<String[], IO<String>> apply(final JsObj conf,
                                                final State state
                                               ) {
        return tokens -> IO.fromSupplier(gen);

    }
}
