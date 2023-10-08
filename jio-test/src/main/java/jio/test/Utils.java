package jio.test;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Utils {

    private Utils() {
    }

    public static String formatTime(long time) {
        if (time < 0) throw new IllegalArgumentException("time < 0");
        if (time >= 1000_000_000) return Duration.ofNanos(time).toSeconds() + " sg";
        if (time >= 1000_1000) return Duration.ofNanos(time).toMillis() + " ms";
        return time + " ns";
    }


}
