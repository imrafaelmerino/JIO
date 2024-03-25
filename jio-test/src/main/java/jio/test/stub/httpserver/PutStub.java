package jio.test.stub.httpserver;

import static java.util.Objects.requireNonNull;

import com.sun.net.httpserver.HttpHandler;

/**
 * Stub that stands in for the {@link HttpHandler} of a PUT HTTP request.
 */
public final class PutStub extends ReqHandlerStub {

  private PutStub(final BodyStub body,
                  final StatusCodeStub statusCode,
                  final HeadersStub headers
                 ) {
    super(requireNonNull(body),
          requireNonNull(statusCode),
          requireNonNull(headers),
          "PUT"
         );
  }

  /**
   * Creates a PUT handler stub that builds the HTTP response from the given body, status code, and headers stubs.
   *
   * @param body       The body response stub.
   * @param statusCode The status code response stub.
   * @param headers    The headers response stub.
   * @return A put stub.
   */
  public static PutStub of(final BodyStub body,
                           final StatusCodeStub statusCode,
                           final HeadersStub headers
                          ) {
    return new PutStub(body,
                       statusCode,
                       headers);
  }

  /**
   * Creates a PUT handler stub that builds the HTTP response from the given body and status code stubs.
   *
   * @param body       The body response stub.
   * @param statusCode The status code response stub.
   * @return A put stub.
   */
  public static PutStub of(final BodyStub body,
                           final StatusCodeStub statusCode
                          ) {
    return new PutStub(body,
                       statusCode,
                       HeadersStub.EMPTY);
  }
}
