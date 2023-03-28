package jio.console;

@SuppressWarnings("serial")
class CommandNotFoundException extends Exception {

    public CommandNotFoundException(String name) {
        super(String.format("Command '%s' not found. Type 'list' to see all possible commands.",
                            name)
             );
    }
}
