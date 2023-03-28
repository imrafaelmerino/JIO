package jio.test.stub.httpserver;

import static java.util.Objects.requireNonNull;

/**
 * Stub that stands in for the handler of a DELETE http request
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
     * Creates a DELETE handler stub that build the http response from the given body, status code and headers stubs
     * @param body the body response stub
     * @param statusCode the status code response stub
     * @param headers the headers response stub
     * @return a delete stub
     */
    public static DeleteStub of(final BodyStub body,
                                final StatusCodeStub statusCode,
                                final HeadersStub headers
                               ) {
        return new DeleteStub(body, statusCode, headers);
    }

    /**
     * Creates a DELETE handler stub that build the http response from the given body and status code stubs
     * @param body the body response stub
     * @param statusCode the status code response stub
     * @return a delete stub
     */
    public static DeleteStub of(final BodyStub body,
                                final StatusCodeStub statusCode
                               ) {
        return new DeleteStub(body, statusCode, HeadersStub.EMPTY);
    }
}
