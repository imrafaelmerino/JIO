package jio.cli;

import jio.IO;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Set of different programs modeled with JIO effects to print out text and interact with the user. These programs
 * include reading lines, integers, booleans, printing text to the console, and interacting with the user to input
 * data.
 */
public final class ConsoleReaders {

    /**
     * Effect that reads a line from the console.
     */
    public final static IO<String> READ_LINE = IO.task(() -> {

        Scanner in = new Scanner(System.in,
                                 StandardCharsets.UTF_8
        );
        String line = in.nextLine();
        ConsoleLogger.log("%s\n".formatted(line));
        return line;

    });


    private ConsoleReaders() {
    }


}
