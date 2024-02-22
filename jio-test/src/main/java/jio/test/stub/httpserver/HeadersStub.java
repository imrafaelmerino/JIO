package jio.test.stub.httpserver;

import com.sun.net.httpserver.Headers;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A stub that stands in for the response {@link Headers} of an HTTP request.
 */
public non-sealed interface HeadersStub extends HttpRespStub<Headers> {

  /**
   * An empty headers stub that always sets empty headers as the response headers.
   */
  HeadersStub EMPTY = n -> reqBody -> uri -> headers -> new Headers();

  /**
   * Creates a headers stub that always sets the specified map as the response headers.
   *
   * @param map The map of headers to be set as response headers.
   * @return A header stub that sets the specified headers.
   * @throws NullPointerException if the provided map is null.
   */
  static HeadersStub cons(Map<String, List<String>> map) {
    Headers respHeaders = new Headers();
    respHeaders.putAll(Objects.requireNonNull(map));
    return n -> reqBody -> uri -> reqHeaders -> respHeaders;
  }

}
