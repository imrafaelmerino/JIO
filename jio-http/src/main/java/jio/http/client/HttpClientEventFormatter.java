package jio.http.client;

import jdk.jfr.consumer.RecordedEvent;

import java.util.function.Function;

import static jio.http.client.ReqEvent.*;

/**
 * A class that converts Java Flight Recorder (JFR) RecordedEvents related to HTTP client operations to formatted
 * strings. This class is intended to be used as a Function for transforming RecordedEvents into human-readable
 * strings.
 *
 * <p>
 * The formatting includes information such as the HTTP method, URI, result, status code (for successful events),
 * exception (for events with errors), duration, and request counter.
 * </p>
 *
 * <p>
 * The formatted output for a successful event is: "method: %s, uri: %s, result: %s, status-code: %s, duration: %s,
 * req-counter: %s".
 * </p>
 *
 * <p>
 * The formatted output for an event with an exception is: "method: %s, uri: %s, result: %s, exception: %s, duration:
 * %s, req-counter: %s".
 * </p>
 *
 * <p>
 * Note: This class is designed to work with the JFR events created by jio-http. Since it's just * a function you can
 * define your own formatters
 * </p>
 */
public final class HttpClientEventFormatter implements Function<RecordedEvent, String> {

    /**
     * The singleton instance of HttpClientEventFormatter.
     */
    public static final HttpClientEventFormatter INSTANCE = new HttpClientEventFormatter();

    private HttpClientEventFormatter() {
    }

    @Override
    public String apply(RecordedEvent e) {
        assert e.getEventType().getName().equals("jio.httpclient");
        String exception = e.getValue(EXCEPTION_LABEL);
        boolean isSuccess = exception == null || exception.isEmpty();
        if (isSuccess)
            return String.format("method: %s, uri: %s, result: %s, status-code: %s duration: %s, req-counter: %s",
                                 e.getValue(METHOD_LABEL),
                                 e.getValue(URI_LABEL),
                                 e.getValue(RESULT_LABEL),
                                 e.getValue(STATUS_CODE_LABEL),
                                 e.getDuration().toMillis(),
                                 e.getValue(REQ_COUNTER_LABEL)
                                );
        return String.format("method: %s, uri: %s, result: %s, exception: %s, duration: %s, req-counter: %s",
                             e.getValue(METHOD_LABEL),
                             e.getValue(URI_LABEL),
                             e.getValue(RESULT_LABEL),
                             exception,
                             e.getDuration().toMillis(),
                             e.getValue(REQ_COUNTER_LABEL)
                            );

    }
}
