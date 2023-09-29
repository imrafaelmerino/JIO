package jio.console;

/**
 * Represents an exception that occurs when a user enters an invalid command or command arguments in the console.
 * This exception provides details about the specific reason for the failure and includes information about the
 * description of the command for user reference.
 */
public class InvalidCommand extends Exception {


    /**
     * Constructs a new {@code InvalidCommand} exception with the given command and the exact reason for the failure.
     *
     * @param command The command that the user attempted to execute.
     * @param reason  The reason the command cannot be executed due to invalid input.
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
