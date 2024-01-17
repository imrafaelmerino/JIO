package jio.mongodb;

import jdk.jfr.consumer.RecordedEvent;

import java.util.function.Function;

import static jio.mongodb.MongoEvent.EXCEPTION_LABEL;

/**
 * Formats recorded events from a jio-mongodb operation into a human-readable string. Since it's just a function you can
 * define your own formatters
 */
public final class MongoClientEventFormatter implements Function<RecordedEvent, String> {

    /**
     * The singleton instance of HttpClientEventFormatter.
     */
    public static final MongoClientEventFormatter INSTANCE = new MongoClientEventFormatter();
    static final String OPERATION_LABEL = "operation";
    static final String RESULT_LABEL = "result";

    private MongoClientEventFormatter() {
    }

    /**
     * Formats a recorded event into a human-readable string representation.
     *
     * @param e The recorded event to be formatted.
     * @return A formatted string representing the MongoDB client operation, result, duration, and exception (if any).
     */
    @Override
    public String apply(RecordedEvent e) {
        assert e.getEventType().getName().equals("jio.mongodb");
        String exception = e.getValue(EXCEPTION_LABEL);
        boolean isSuccess = exception == null || exception.isEmpty();
        return isSuccess ?
                String.format("op: %s, result: %s duration: %s",
                              e.getValue(OPERATION_LABEL),
                              e.getValue(RESULT_LABEL),
                              e.getDuration().toNanos()
                             ) :
                String.format("op: %s, result: %s, duration: %s exception: %s",
                              e.getValue(OPERATION_LABEL),
                              e.getValue(RESULT_LABEL),
                              e.getDuration().toNanos(),
                              exception
                             );

    }
}
