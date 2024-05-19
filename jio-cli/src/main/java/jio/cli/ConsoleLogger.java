package jio.cli;


import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class ConsoleLogger {

    static Path logFile;

    private ConsoleLogger() {

    }

    public static void log(final String text) {
        if (logFile != null) {
            try {
                Files.writeString(logFile,
                                  text,
                                  StandardOpenOption.APPEND,
                                  StandardOpenOption.CREATE);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
