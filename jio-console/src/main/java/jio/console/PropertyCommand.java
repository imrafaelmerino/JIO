package jio.console;

import jio.IO;
import jio.pbt.Property;
import jsonvalues.JsObj;

import java.util.Arrays;
import java.util.function.Function;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Command to execute {@link Property properties} with the command:
 * <pre>
 *     prop name
 * </pre>
 *
 * Properties can also be executed an arbitrary number of time either in parallel
 * or sequentially:
 *
 * <pre>
 *     prop name par 3
 *     prop name seq 5
 * </pre>
 *
 */
public class PropertyCommand extends Command {

    static final Pattern parPattern =
            Pattern.compile("prop \\w+ par \\d+");
    static final Pattern seqPattern =
            Pattern.compile("prop \\w+ seq \\d+");

    private final Property<?> prop;

    private static final String PREFIX_COMMAND = "prop";


    /**
     * Creates a PropertyCommand from a property
     * @param prop the property
     */
    public PropertyCommand(final Property<?> prop) {
        super(String.format("%s %s",
                            PREFIX_COMMAND,
                            requireNonNull(prop).getName()
                           ),
              prop.getDescription(),
              tokens ->
                      tokens[0].equalsIgnoreCase(PREFIX_COMMAND)
                              && tokens[1].equalsIgnoreCase(prop.getName())
             );
        this.prop = requireNonNull(prop);
    }

    @Override
    public Function<String[], IO<String>> apply(final JsObj conf,
                                                final State state
                                               ) {
        return tokens -> {
            String command = String.join(" ", Arrays.stream(tokens).toList());
            if (parPattern.matcher(command).matches()) {
                int n = Integer.parseInt(tokens[3]);
                return IO.fromValue(prop.repeatPar(n).toString());

            }
            if (seqPattern.matcher(command).matches()) {
                int n = Integer.parseInt(tokens[3]);
                return IO.fromValue(prop.repeatSeq(n).toString());
            }
            return IO.fromFailure( new CommandNotFoundException(command));


        };
    }
}
