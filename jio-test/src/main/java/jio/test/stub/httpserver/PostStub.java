package jio.test.stub.httpserver;

import static java.util.Objects.requireNonNull;

import com.sun.net.httpserver.HttpHandler;

/**
 * Stub that stands in for the {@link HttpHandler} of a POST HTTP request.
 */
public final class PostStub extends ReqHandlerStub {

  private PostStub(final BodyStub body,
                   final StatusCodeStub statusCode,
                   final HeadersStub headers
                  ) {
    super(requireNonNull(body),
          requireNonNull(statusCode),
          requireNonNull(headers),
          "POST"
         );
  }

  /**
   * Creates a POST handler stub that builds the HTTP response from the given body, status code, and headers stubs.
   *
   * @param body       The body response stub.
   * @param statusCode The status code response stub.
   * @param headers    The headers response stub.
   * @return A post stub.
   */
  public static PostStub of(final BodyStub body,
                            final StatusCodeStub statusCode,
                            final HeadersStub headers
                           ) {
    return new PostStub(body,
                        statusCode,
                        headers);
  }

  /**
   * Creates a POST handler stub that builds the HTTP response from the given body and status code stubs.
   *
   * @param body       The body response stub.
   * @param statusCode The status code response stub.
   * @return A post stub.
   */
  public static PostStub of(final BodyStub body,
                            final StatusCodeStub statusCode
                           ) {
    return new PostStub(body,
                        statusCode,
                        HeadersStub.EMPTY);
  }
}
