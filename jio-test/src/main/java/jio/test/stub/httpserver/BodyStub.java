package jio.test.stub.httpserver;

import java.time.Duration;
import java.util.Objects;

/**
 * A stub that stands in for the body response of an HTTP request.
 */
public interface BodyStub extends HttpRespStub<String> {

    /**
     * Creates a body stub that always returns the given body as the HTTP response body.
     *
     * @param body The body to be returned.
     * @return A body stub that returns the specified body.
     * @throws NullPointerException if the provided body is null.
     */
    static BodyStub cons(final String body) {
        Objects.requireNonNull(body);
        return n -> reqBody -> uri -> headers -> body;
    }

    /**
     * Creates a body stub that always returns the given body as the HTTP response body
     * after the specified delay.
     *
     * @param body  The body to be returned.
     * @param delay The delay before returning the body.
     * @return A body stub that returns the specified body after the delay.
     * @throws NullPointerException if the provided body or delay is null.
     */
    static BodyStub consAfter(final String body,
                              final Duration delay
                             ) {
        Objects.requireNonNull(body);
        Objects.requireNonNull(delay);
        return n -> reqBody -> uri -> headers -> {
            try {
                Thread.sleep(delay.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return body;
        };
    }
}
