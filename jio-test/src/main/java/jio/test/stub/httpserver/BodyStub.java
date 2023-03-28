package jio.test.stub.httpserver;

import java.time.Duration;
import java.util.Objects;

/**
 * Stub that stands in for the body response of a http request.
 *
 */
public interface BodyStub extends HttpRespStub<String> {

    /**
     * stub that always returns the given body
     * @param body the body
     * @return a body stub
     */
    static BodyStub cons(final String body) {
        Objects.requireNonNull(body);
        return n -> reqBody -> uri -> headers -> body;
    }

    /**
     * stub that always returns the given body after the specified delay
     * @param body the body
     * @param delay the delay
     * @return a body stub
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
