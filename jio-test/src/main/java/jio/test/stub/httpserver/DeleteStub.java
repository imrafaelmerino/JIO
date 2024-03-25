package jio.test.stub.httpserver;

import static java.util.Objects.requireNonNull;

import com.sun.net.httpserver.HttpHandler;

/**
 * Stub that stands in for the {@link HttpHandler} of a DELETE HTTP request.
 */
public final class DeleteStub extends ReqHandlerStub {

  private DeleteStub(final BodyStub body,
                     final StatusCodeStub statusCode,
                     final HeadersStub headers
                    ) {
    super(requireNonNull(body),
          requireNonNull(statusCode),
          requireNonNull(headers),
          "delete"
         );
  }

  /**
   * Creates a DELETE handler stub that builds the HTTP response from the given body, status code, and headers stubs.
   *
   * @param body       The body response stub.
   * @param statusCode The status code response stub.
   * @param headers    The headers response stub.
   * @return A delete stub.
   */
  public static DeleteStub of(final BodyStub body,
                              final StatusCodeStub statusCode,
                              final HeadersStub headers
                             ) {
    return new DeleteStub(body,
                          statusCode,
                          headers);
  }

  /**
   * Creates a DELETE handler stub that builds the HTTP response from the given body and status code stubs.
   *
   * @param body       The body response stub.
   * @param statusCode The status code response stub.
   * @return A delete stub.
   */
  public static DeleteStub of(final BodyStub body,
                              final StatusCodeStub statusCode
                             ) {
    return new DeleteStub(body,
                          statusCode,
                          HeadersStub.EMPTY);
  }
}
