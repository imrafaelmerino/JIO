package jio.test.stub.httpserver;

import com.sun.net.httpserver.Headers;
import java.io.InputStream;
import java.net.URI;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * Represents a stub that stands in for an HTTP response, which is modeled with a function. The function takes an
 * integer (counter that carries the number of HTTP requests that hit the server), an input stream (the request body),
 * the request URI, and the request headers, and returns a output of type {@code R}.
 *
 * @param <R> The type of the output returned by the stub: the response body (string), response headers, or response
 *            status code (integer).
 */
sealed interface HttpRespStub<R> extends
                                 IntFunction<Function<InputStream, Function<URI, Function<Headers, R>>>> permits
                                                                                                         BodyStub,
                                                                                                         HeadersStub,
                                                                                                         StatusCodeStub {

}
