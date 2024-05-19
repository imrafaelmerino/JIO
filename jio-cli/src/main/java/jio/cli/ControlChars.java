package jio.cli;

/**
 * Some ANSI escape sequences.
 */
public enum ControlChars {

    /**
     * clears the terminal window
     */
    CLEAR("\u001b[H\u001b[2J"),

    /**
     * removes any active control sequence. Classical use to stop printing in some color:
     * <pre>
     * System.out.println(RED + "printing in read" + RESET + "printing back in black")
     * </pre>
     */
    RESET("\u001b[0m"),

    /**
     * Regular black color
     */
    BLACK("\033[0;30m"),
    /**
     * Regular red color
     */
    RED("\033[0;31m"),
    /**
     * Regular green color
     */
    GREEN("\033[0;32m"),
    /**
     * Regular yellow color
     */
    YELLOW("\033[0;33m"),
    /**
     * Regular blue color
     */
    BLUE("\033[0;34m"),
    /**
     * Regular magenta color
     */
    MAGENTA("\033[0;35m"),
    /**
     * Regular cyan color
     */
    CYAN("\033[0;36m"),
    /**
     * Regular white color
     */
    WHITE("\033[0;37m"),

    /**
     * bold black color
     */
    BLACK_BOLD("\033[1;30m"),
    /**
     * bold red color
     */
    RED_BOLD("\033[1;31m"),
    /**
     * bold green color
     */
    GREEN_BOLD("\033[1;32m"),
    /**
     * bold yellow color
     */
    YELLOW_BOLD("\033[1;33m"),
    /**
     * bold blue color
     */
    BLUE_BOLD("\033[1;34m"),
    /**
     * bold magenta color
     */
    MAGENTA_BOLD("\033[1;35m"),
    /**
     * bold cyan color
     */
    CYAN_BOLD("\033[1;36m"),
    /**
     * bold white color
     */
    WHITE_BOLD("\033[1;37m"),

    /**
     * underline black color
     */
    BLACK_UNDERLINED("\033[4;30m"),
    /**
     * underline red color
     */
    RED_UNDERLINED("\033[4;31m"),
    /**
     * underline green color
     */
    GREEN_UNDERLINED("\033[4;32m"),
    /**
     * underline yellow color
     */
    YELLOW_UNDERLINED("\033[4;33m"),
    /**
     * underline blue color
     */
    BLUE_UNDERLINED("\033[4;34m"),
    /**
     * underline magenta color
     */
    MAGENTA_UNDERLINED("\033[4;35m"),
    /**
     * underline cyan color
     */
    CYAN_UNDERLINED("\033[4;36m"),
    /**
     * underline white color
     */
    WHITE_UNDERLINED("\033[4;37m"),

    /**
     * background black color
     */
    BLACK_BACKGROUND("\033[40m"),
    /**
     * background red color
     */
    RED_BACKGROUND("\033[41m"),
    /**
     * background green color
     */
    GREEN_BACKGROUND("\033[42m"),
    /**
     * background yellow color
     */
    YELLOW_BACKGROUND("\033[43m"),
    /**
     * background blue color
     */
    BLUE_BACKGROUND("\033[44m"),
    /**
     * background magenta color
     */
    MAGENTA_BACKGROUND("\033[45m"),
    /**
     * background cyan color
     */
    CYAN_BACKGROUND("\033[46m"),
    /**
     * background white color
     */
    WHITE_BACKGROUND("\033[47m");

    /**
     * the control code as a string
     */
    public final String code;

    ControlChars(String code) {
        this.code = code;
    }
}