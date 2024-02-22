package jio.test.stub.httpserver;

import static java.util.Objects.requireNonNull;

import com.sun.net.httpserver.HttpHandler;

/**
 * Stub that stands in for the {@link HttpHandler} of an OPTIONS HTTP request.
 */
public final class OptionsStub extends ReqHandlerStub {

  private OptionsStub(final BodyStub body,
                      final StatusCodeStub statusCode,
                      final HeadersStub headers
                     ) {
    super(requireNonNull(body),
          requireNonNull(statusCode),
          requireNonNull(headers),
          "options"
         );
  }

  /**
   * Creates a GET handler stub that builds the HTTP response from the given body, status code, and headers stubs.
   *
   * @param body       The body response stub.
   * @param statusCode The status code response stub.
   * @param headers    The headers response stub.
   * @return A GET stub.
   */
  public static OptionsStub of(final BodyStub body,
                               final StatusCodeStub statusCode,
                               final HeadersStub headers
                              ) {
    return new OptionsStub(body,
                           statusCode,
                           headers);
  }

  /**
   * Creates a GET handler stub that builds the HTTP response from the given body and status code stubs.
   *
   * @param body       The body response stub.
   * @param statusCode The status code response stub.
   * @return A GET stub.
   */
  public static OptionsStub of(final BodyStub body,
                               final StatusCodeStub statusCode
                              ) {
    return new OptionsStub(body,
                           statusCode,
                           HeadersStub.EMPTY);
  }
}
