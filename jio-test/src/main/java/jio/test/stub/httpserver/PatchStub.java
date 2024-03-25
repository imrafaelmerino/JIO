package jio.test.stub.httpserver;

import static java.util.Objects.requireNonNull;

import com.sun.net.httpserver.HttpHandler;

/**
 * Stub that stands in for the {@link HttpHandler} of a PATCH HTTP request.
 */
public final class PatchStub extends ReqHandlerStub {

  private PatchStub(final BodyStub body,
                    final StatusCodeStub statusCode,
                    final HeadersStub headers
                   ) {
    super(requireNonNull(body),
          requireNonNull(statusCode),
          requireNonNull(headers),
          "PATCH"
         );
  }

  /**
   * Creates a PATCH handler stub that builds the HTTP response from the given body, status code, and headers stubs.
   *
   * @param body       The body response stub.
   * @param statusCode The status code response stub.
   * @param headers    The headers response stub.
   * @return A PATCH stub.
   */
  public static PatchStub of(final BodyStub body,
                             final StatusCodeStub statusCode,
                             final HeadersStub headers
                            ) {
    return new PatchStub(body,
                         statusCode,
                         headers);
  }

  /**
   * Creates a PATCH handler stub that builds the HTTP response from the given body and status code stubs.
   *
   * @param body       The body response stub.
   * @param statusCode The status code response stub.
   * @return A PATCH stub.
   */
  public static PatchStub of(final BodyStub body,
                             final StatusCodeStub statusCode
                            ) {
    return new PatchStub(body,
                         statusCode,
                         HeadersStub.EMPTY);
  }
}
