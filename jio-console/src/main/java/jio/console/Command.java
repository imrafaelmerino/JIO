package jio.console;


import jio.IO;
import jsonvalues.JsObj;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents a command that is modeled with a function that takes three parameters:
 *
 * <ul>
 *     <li>a JsObj with all the configuration information that is specified when starting the console</li>
 *     <li>the State, that is a map of variables and their values</li>
 *     <li>the command typed in by the user parsed into an array of tokens</li>
 * </ul>
 *
 * and returns a JIO effect that executes the command action and returns as a result a string.
 * The output of a command is by default stored in the state variable called output. It can be overwritten
 * with the method {@link #setSaveOutput(boolean)}
 *
 */
public abstract class Command implements BiFunction<JsObj, State, Function<String[], IO<String>>> {

    /**
     * if false, the output of the command is not stored in the state variable 'output'
     * @param saveOutput the flag
     * @return this command
     */
    public Command setSaveOutput(boolean saveOutput) {
        isSaveOutput = saveOutput;
        return this;
    }

    public boolean isSaveOutput = true;

    @Override
    public String toString() {
        return "Command{" +
                "name='" + name + '\'' +
                '}';
    }

    private final Predicate<String[]> isCommand;

    /**
     * Constructor to create a command from its name, description and a predicate to check if the text typed in by
     * the user corresponds to this command. The description will be printed out on the console when typing
     * the help command:
     * <pre>
     *     help name
     * </pre>
     * @param name the name of the command
     * @param description the description of the command
     * @param isCommand a predicate to test if the command typed by the user corresponds to this command
     */
    public Command(final String name,
                   final String description,
                   final Predicate<String[]> isCommand
                  ) {
        this.description = Objects.requireNonNull(description);
        this.name = Objects.requireNonNull(name);
        this.isCommand = Objects.requireNonNull(isCommand);
    }

    /**
     * Constructor to create a command from its name and description. The predicate to
     * check if the text typed in by the user corresponds to this command is
     * @param name the name of the command
     * @param description the description of the command
     */
    public Command(final String name,
                   final String description
                  ) {
        this(name,
             description,
             tokens -> name.equalsIgnoreCase(tokens[0])
            );
    }

    /**
     * Description of the command
     */
    public final String description;

    /**
     * name of the command
     */
    public final String name;


    /**
     * Returns a function that given an array of tokens that represents the user input, returns
     * an empty optional if the user input doesn't correspond to this command or a JIO effect
     * that executes the command action and returns a string as the output.
     * @param conf the configuration
     * @param state the state
     * @return a function that takes the array of tokens typed in by the client and returns the
     * command action wrapped into an optional (if the optional is empty means the user input
     * doesn correspond to this command)
     */
    Function<String[], Optional<IO<String>>> executeIfMatch(JsObj conf, State state) {
        return tokens -> {
            try {
                return isCommand.test(tokens) ?
                        Optional.of(apply(conf, state).apply(tokens)) :
                        Optional.empty();
            } catch (Exception e) {
                return Optional.of(IO.fromFailure(e));
            }
        };
    }

    @Override
    public Function<String[], IO<String>> apply(JsObj tuple2s, State state) {
        return null;
    }
}
