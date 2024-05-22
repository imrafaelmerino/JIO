package jio.test.pbt;

import jio.IO;
import jio.cli.Command;
import jio.cli.State;
import jsonvalues.JsObj;

import java.util.Arrays;
import java.util.function.Function;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Command to execute {@link Property properties} with the command:
 * <pre>
 * prop name
 * </pre>
 * <p>
 * Properties can also be executed an arbitrary number of time either in parallel or sequentially:
 *
 * <pre>
 * prop name par 3
 * prop name seq 5
 * </pre>
 */
class PropertyCommand extends Command {

    static final Pattern parPattern = Pattern.compile("prop \\w+ par \\d+");
    static final Pattern seqPattern = Pattern.compile("prop \\w+ seq \\d+");
    private static final String PREFIX_COMMAND = "prop";
    private final Property<?> prop;

    /**
     * Creates a PropertyCommand from a property.
     *
     * @param prop The property to execute.
     */
    private PropertyCommand(final Property<?> prop) {
        super(String.format("%s %s",
                            PREFIX_COMMAND,
                            requireNonNull(prop).name
                           ),
              prop.description,
              tokens -> tokens[0].equalsIgnoreCase(PREFIX_COMMAND)
                        && tokens[1].equalsIgnoreCase(prop.name)
             );
        this.prop = requireNonNull(prop);
    }

    /**
     * Creates a PropertyCommand from a property.
     *
     * @param prop The property to execute.
     */
    public static PropertyCommand of(final Property<?> prop) {
        return new PropertyCommand(prop);
    }

    @Override
    public Function<String[], IO<String>> apply(final JsObj conf,
                                                final State state
                                               ) {
        return tokens -> {
            String command = String.join(" ",
                                         Arrays.stream(tokens)
                                               .toList());
            if (parPattern.matcher(command)
                          .matches()) {
                int n = Integer.parseInt(tokens[3]);
                return IO.succeed(prop.repeatPar(n)
                                      .create(conf)
                                      .compute()
                                      .toString());
            }
            if (seqPattern.matcher(command)
                          .matches()) {
                int n = Integer.parseInt(tokens[3]);
                return IO.succeed(prop.repeatPar(n)
                                      .create(conf)
                                      .compute()
                                      .toString());
            }
            return prop.create(conf)
                       .map(Report::toString);

        };
    }

}
