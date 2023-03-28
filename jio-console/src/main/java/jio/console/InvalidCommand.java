package jio.console;

/**
 * Exception that represents a user error typing in a command: an argument is missing, a number was expected etc
 */
@SuppressWarnings("serial")
public class InvalidCommand extends Exception {


    /**
     * Constructor to create InvalidCommand exceptions from the command and the exact reason of the failure
     *
     * @param command the command
     * @param reason  the reason the command can not be executed
     */
    public InvalidCommand(final Command command,
                          final String reason
                         ) {
        super(ControlChars.RED.code +
                      String.format("Invalid command.\nReason: %s.\nCommand description: %s",
                                    reason,
                                    command.description
                                   )
                      + ControlChars.RESET.code
             );

    }
}
