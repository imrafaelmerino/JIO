package jio.test.stub.httpserver;

import static java.util.Objects.requireNonNull;

/**
 * Stub that stands in for the handler of a GET http request
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
     * Creates a GET handler stub that build the http response from the given body, status code and headers stubs
     * @param body the body response stub
     * @param statusCode the status code response stub
     * @param headers the headers response stub
     * @return a get stub
     */
    public static GetStub of(final BodyStub body,
                             final StatusCodeStub statusCode,
                             final HeadersStub headers
                            ) {
        return new GetStub(body, statusCode, headers);
    }

    /**
     * Creates a GET handler stub that build the http response from the given body and status code stubs
     * @param body the body response stub
     * @param statusCode the status code response stub
     * @return a get stub
     */
    public static GetStub of(final BodyStub body,
                             final StatusCodeStub statusCode
                            ) {
        return new GetStub(body, statusCode, HeadersStub.EMPTY);
    }
}
