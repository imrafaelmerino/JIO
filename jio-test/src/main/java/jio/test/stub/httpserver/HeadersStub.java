package jio.test.stub.httpserver;

import com.sun.net.httpserver.Headers;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Stub that stands in for the response headers of a http request.
 *
 */
public interface HeadersStub extends HttpRespStub<Headers> {

    /**
     * Stub that always set empty the headers response
     */
    HeadersStub EMPTY = n -> reqBody -> uri -> headers -> new Headers();

    /**
     * Stub that always set the specified map as the response headers
     * @param map the map of headers
     * @return a header stub
     */
    static HeadersStub cons(Map<String, List<String>> map) {
        Headers respHeaders = new Headers();
        respHeaders.putAll(Objects.requireNonNull(map));
        return n -> reqBody -> uri -> reqHeaders -> respHeaders;
    }


}
