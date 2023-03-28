package jio.test.stub.httpserver;

import static java.util.Objects.requireNonNull;

/**
 * Stub that stands in for the handler of a PUT http request
 */
public final class PutStub extends ReqHandlerStub {
    private PutStub(final BodyStub body,
                    final StatusCodeStub statusCode,
                    final HeadersStub headers
                   ) {
        super(requireNonNull(body),
              requireNonNull(statusCode),
              requireNonNull(headers),
              "put"
             );
    }


    /**
     * Creates a PUT handler stub that build the http response from the given body, status code and headers stubs
     * @param body the body response stub
     * @param statusCode the status code response stub
     * @param headers the headers response stub
     * @return a put stub
     */
    public static PutStub of(final BodyStub body,
                             final StatusCodeStub statusCode,
                             final HeadersStub headers
                            ) {
        return new PutStub(body, statusCode, headers);
    }

    /**
     * Creates a PUT handler stub that build the http response from the given body and status code stubs
     * @param body the body response stub
     * @param statusCode the status code response stub
     * @return a put stub
     */
    public static PutStub of(final BodyStub body,
                             final StatusCodeStub statusCode
                            ) {
        return new PutStub(body, statusCode, HeadersStub.EMPTY);
    }
}
