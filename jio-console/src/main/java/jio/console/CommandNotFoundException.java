package jio.console;

/**
 * represents an error that happens when the user types in a command and it doesn't exist
 */
@SuppressWarnings("serial")
public class CommandNotFoundException extends Exception {

  /**
   * Creates a CommandNotFoundException from the specified command name
   *
   * @param name the name of the command
   */
  public CommandNotFoundException(String name) {
    super(String.format("Command '%s' not found. Type 'list' to see all possible commands.",
                        name)
    );
  }
}
