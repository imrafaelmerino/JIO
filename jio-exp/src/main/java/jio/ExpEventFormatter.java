package jio;

import jdk.jfr.consumer.RecordedEvent;

import java.util.Objects;
import java.util.function.Function;

import static jio.ExpEvent.*;

/**
 * A class that converts Java Flight Recorder (JFR) RecordedEvents related to expressions to formatted strings.
 * This class is intended to be used as a Function for transforming RecordedEvents into human-readable strings.
 *
 * <p>
 * The formatting includes information such as the expression, result, output, duration, and context.
 * </p>
 *
 * <p>
 * The formatted output for a successful event is: "expression: %s, result: %s, output: %s, duration: %s, context: %s".
 * </p>
 *
 * <p>
 * The formatted output for an event with an exception is: "expression: %s, result: %s, exception: %s, duration:
 * %s, context: %s".
 * </p>
 *
 * <p>
 * Note: This class is designed to work with the JFR events created by jio-exp. Since it's just a function you can
 * define your own formatters
 * </p>
 */
public final class ExpEventFormatter implements Function<RecordedEvent, String> {

    /**
     * The singleton instance of ExpEventFormatter with the default identity formatter for output.
     */
    public static final ExpEventFormatter INSTANCE = new ExpEventFormatter(Function.identity());

    /**
     * The function used to format the output string.
     */
    public final Function<String, String> formatOutput;

    /**
     * Constructs an ExpEventFormatter with a custom output formatter.
     *
     * @param formatOutput The function to format the output string.
     */
    public ExpEventFormatter(Function<String, String> formatOutput) {
        this.formatOutput = Objects.requireNonNull(formatOutput);
    }

    /**
     * Converts a RecordedEvent to a formatted string.
     *
     * @param e The RecordedEvent to be converted.
     * @return A formatted string representing the information from the RecordedEvent.
     */
    @Override
    public String apply(RecordedEvent e) {
        assert e.getEventType().getName().equals("jio.exp");
        String exception = e.getValue(EXCEPTION_LABEL);
        boolean isSuccess = exception == null || exception.isEmpty();

        return String.format("expression: %s, result: %s, output: %s, duration: %s, context: %s",
                             e.getValue(EXCEPTION_LABEL),
                             e.getValue(RESULT_LABEL),
                             isSuccess ? formatOutput.apply(e.getValue(VALUE_LABEL)) : exception,
                             e.getDuration().toMillis(),
                             e.getValue(CONTEXT_LABEL)
                            );
    }
}
