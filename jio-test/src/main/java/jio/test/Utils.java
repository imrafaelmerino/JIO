package jio.test;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
        if (time >= 1000_000_000) return Duration.ofNanos(time).toSeconds() + " sg";
        if (time >= 1000_1000) return Duration.ofNanos(time).toMillis() + " ms";
        return time + " ns";
    }

    /**
     * Categorizes an HTTP status code into different groups based on its numerical value.
     *
     * @param code The HTTP status code to categorize.
     * @return A string representing the category of the status code.
     */
    public static String categorizeHttpStatusCode(int code){
        if(code < 200) return "INFORMATIONAL";
        if(code < 300) return "SUCCESS";
        if(code < 400) return "REDIRECTION";
        if(code < 500) return "CLIENT_ERROR";
        return "SERVER_ERROR";
    }


}
