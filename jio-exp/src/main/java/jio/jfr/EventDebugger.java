package jio.jfr;

import jdk.jfr.Configuration;
import jdk.jfr.consumer.EventStream;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingStream;

import java.io.IOException;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * An abstract base class for debugging events using Java Flight Recorder (JFR).
 * Provides methods for configuring event recording and handling recorded events.
 */
public abstract class EventDebugger {

    private final EventStream es;

    /**
     * Constructs an EventDebugger instance with the specified event name, configuration name, and event consumer.
     *
     * @param eventName The name of the event to capture.
     * @param confName  The name of the JFR configuration to use.
     * @param consumer  The consumer function to handle recorded events.
     */
    public EventDebugger(final String eventName,
                         final String confName,
                         final Consumer<RecordedEvent> consumer
                        ) {
        try {
            Configuration config = Configuration.getConfiguration(Objects.requireNonNull(confName));
            es = new RecordingStream(Objects.requireNonNull(config));
            es.onEvent(eventName, Objects.requireNonNull(consumer));
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Constructs an EventDebugger instance with the specified event name and event consumer,
     * using the default JFR configuration.
     *
     * @param eventName The name of the event to capture.
     * @param consumer  The consumer function to handle recorded events.
     */
    public EventDebugger(final String eventName,
                         final Consumer<RecordedEvent> consumer
                        ) {
        this(eventName, "default", consumer);
    }

    /**
     * Starts asynchronous event recording for the specified duration.
     *
     * @param duration The duration (in milliseconds) for which to capture events.
     */
    public void startAsync(final int duration) {
        if (es != null) {
            es.setEndTime(Instant.now().plus(Duration.ofMillis(duration)));
            es.startAsync();
        }
    }

    /**
     * Closes the EventStream, stopping event recording.
     */
    public void close() {
        if (es != null) es.close();
    }

    /**
     * Blocks until event recording is terminated.
     * Throws a RuntimeException if interrupted.
     */
    public void awaitTermination() {
        try {
            if (es != null) es.awaitTermination();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
