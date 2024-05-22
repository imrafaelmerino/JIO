package jio.cli;

import jio.IO;
import jsonvalues.JsObj;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents a command that is modeled with a function that takes three parameters:
 *
 * <ul>
 * <li>a {@link JsObj} with all the configuration information that is specified when starting the console</li>
 * <li>the {@link State}, which is a map of variables and their values</li>
 * <li>the command typed in by the user parsed into an array of tokens</li>
 * </ul>
 *
 * <p>A command returns a JIO effect that executes the command action and returns a string as a result.
 * The output of a command is, by default, stored in the state variable called "output." This behavior can
 * be controlled with the method {@link #setSaveOutput(boolean)}.</p>
 *
 * <p>Commands are used in the console to perform various actions based on user input.</p>
 */
public abstract class Command implements BiFunction<JsObj, State, Function<String[], IO<String>>> {

    /**
     * Description of the command.
     */
    public final String description;
    /**
     * Name of the command.
     */
    public final String name;
    private final Predicate<String[]> isCommand;
    /**
     * Flag to control whether the output of the command is saved in the state variable 'output.' By default, it is set
     * to true, meaning that output is saved.
     */
    public boolean isSaveOutput = true;
    public String alias;

    /**
     * Constructor to create a command from its name, description, and a predicate to check if the text typed in by the
     * user corresponds to this command. The description will be printed out on the console when typing the help
     * command:
     * <pre>
     * help name
     * </pre>
     *
     * @param name        the name of the command
     * @param description the description of the command
     * @param isCommand   a predicate to test if the command typed by the user corresponds to this command
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
     * Constructor to create a command from its name and description. The predicate to check if the text typed in by the
     * user corresponds to this command is:
     *
     * @param name        the name of the command
     * @param description the description of the command
     */
    public Command(final String name,
                   final String description
                  ) {
        this(name,
             description.strip(),
             tokens -> name.equalsIgnoreCase(tokens[0])
            );
    }

    /**
     * Sets whether the output of the command should be saved in the state variable 'output.'
     *
     * @param saveOutput the flag
     * @return this command
     */
    public Command setSaveOutput(boolean saveOutput) {
        isSaveOutput = saveOutput;
        return this;
    }

    /**
     * An alias (normally a very short name) for this command
     *
     * @param alias the alias name
     */
    public Command setAlias(String alias) {
        this.alias = Objects.requireNonNull(alias);
        return this;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns a function that, given an array of tokens representing the user input, returns `null` if the user input
     * doesn't correspond to this command, or a JIO effect that will execute the command action .
     *
     * @param conf  the configuration
     * @param state the state
     * @return a function that takes the array of tokens typed in by the client and returns the command effect that will
     * execute the command. If returns null, it means the user input doesn't correspond to this command.
     */
    Function<String[], IO<String>> createEffect(JsObj conf,
                                                State state
                                               ) {
        return tokens -> {
            try {
                return (alias != null && alias.equalsIgnoreCase(tokens[0])) || isCommand.test(tokens) ?
                        apply(conf,
                              state).apply(tokens)
                        : null;
            } catch (Exception e) {
                return IO.fail(e);
            }
        };
    }

}
