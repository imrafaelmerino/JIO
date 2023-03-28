package jio.test.stub.httpserver;


import static java.util.Objects.requireNonNull;

/**
 * Stub that stands in for the handler of a POST http request
 */
public final class PostStub extends ReqHandlerStub {
    private PostStub(final BodyStub body,
                     final StatusCodeStub statusCode,
                     final HeadersStub headers
                    ) {
        super(requireNonNull(body),
              requireNonNull(statusCode),
              requireNonNull(headers),
              "post"
             );
    }

    /**
     * Creates a POST handler stub that build the http response from the given body, status code and headers stubs
     *
     * @param body       the body response stub
     * @param statusCode the status code response stub
     * @param headers    the headers response stub
     * @return a post stub
     */
    public static PostStub of(final BodyStub body,
                              final StatusCodeStub statusCode,
                              final HeadersStub headers
                             ) {
        return new PostStub(body,
                            statusCode,
                            headers
        );
    }

    /**
     * Creates a POST handler stub that build the http response from the given body and status code stubs
     *
     * @param body       the body response stub
     * @param statusCode the status code response stub
     * @return a post stub
     */
    public static PostStub of(final BodyStub body,
                              final StatusCodeStub statusCode
                             ) {
        return new PostStub(body,
                            statusCode,
                            HeadersStub.EMPTY
        );
    }
}
