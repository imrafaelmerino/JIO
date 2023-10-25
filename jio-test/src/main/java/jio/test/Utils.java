package jio.test;

/**
 * Utility class
 */
public final class Utils {

    private Utils() {
    }

    /**
     * Formats a given time duration in nanoseconds into a human-readable string.
     *
     * @param time The time in nanoseconds.
     * @return A formatted string representing the time duration.
     */
    public static String formatTime(long time) {
        if (time < 0) throw new IllegalArgumentException("time < 0");
        if (time >= 1000_000_000) return "%.3f sg".formatted(time / 1000_000_000d);
        if (time >= 1000_000) return "%.3f ms".formatted(time / 1000_000d);
        if (time >= 1000) return "%.3f µs".formatted(time / 1000d);
        return "%d ns".formatted(time);
    }

    /**
     * Categorizes an HTTP status code into different groups based on its numerical value.
     *
     * @param code The HTTP status code to categorize.
     * @return A string representing the category of the status code.
     */
    public static String categorizeHttpStatusCode(int code) {
        if (code < 200) return "INFORMATIONAL";
        if (code < 300) return "SUCCESS";
        if (code < 400) return "REDIRECTION";
        if (code < 500) return "CLIENT_ERROR";
        return "SERVER_ERROR";
    }

}
