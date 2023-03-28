package jio.test.stub.httpserver;

import com.sun.net.httpserver.Headers;

import java.io.InputStream;
import java.net.URI;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * Represents a stub that stands in for a http response which is modeled with a function
 * that takes an integer (counter that carries the number of http requests that hit the
 * server), an input stream (the body request), the request uri and the request
 * headers, and returns a value
 * @param <R> the type of the value returned by the stub: the body response (string), headers response or status code (integer)
 */
interface HttpRespStub<R> extends IntFunction<Function<InputStream, Function<URI, Function<Headers, R>>>> {}
