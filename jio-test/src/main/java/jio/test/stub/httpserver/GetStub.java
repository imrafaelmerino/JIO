package jio.test.stub.httpserver;

import static java.util.Objects.requireNonNull;

import com.sun.net.httpserver.HttpHandler;

/**
 * Stub that stands in for the {@link HttpHandler} of a GET HTTP request.
 */
public final class GetStub extends ReqHandlerStub {

  private GetStub(final BodyStub body,
                  final StatusCodeStub statusCode,
                  final HeadersStub headers
                 ) {
    super(requireNonNull(body),
          requireNonNull(statusCode),
          requireNonNull(headers),
          "get"
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
  public static GetStub of(final BodyStub body,
                           final StatusCodeStub statusCode,
                           final HeadersStub headers
                          ) {
    return new GetStub(body,
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
  public static GetStub of(final BodyStub body,
                           final StatusCodeStub statusCode
                          ) {
    return new GetStub(body,
                       statusCode,
                       HeadersStub.EMPTY);
  }
}
