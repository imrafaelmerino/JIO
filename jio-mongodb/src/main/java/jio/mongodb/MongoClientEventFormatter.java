package jio.mongodb;

import jdk.jfr.consumer.RecordedEvent;

import java.util.function.Function;

/**
 * Formats recorded events from a jio-mongodb operation into a human-readable string. Since it's just a function you can
 * define your own formatters
 */
public final class MongoClientEventFormatter implements Function<RecordedEvent, String> {

    /**
     * Formats a recorded event into a human-readable string representation.
     *
     * @param e The recorded event to be formatted.
     * @return A formatted string representing the MongoDB client operation, result, duration, and exception (if any).
     */
    @Override
    public String apply(RecordedEvent e) {
        assert e.getEventType().getName().equals("jio.mongodb");
        String exception = e.getValue("exception");
        boolean isSuccess = exception == null || exception.isEmpty();
        return isSuccess ?
                String.format("op: %s, result: %s duration: %s",
                              e.getValue("operation"),
                              e.getValue("result"),
                              e.getDuration().toNanos()
                             ) :
                String.format("op: %s, result: %s, duration: %s exception: %s",
                              e.getValue("operation"),
                              e.getValue("result"),
                              e.getDuration().toNanos(),
                              exception
                             );

    }
}
