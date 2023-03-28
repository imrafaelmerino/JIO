package jio.test.stub.httpserver;

import static java.util.Objects.requireNonNull;

/**
 * Stub that stands in for the handler of a PATCH http request
 */
public final class PatchStub extends ReqHandlerStub {
    private PatchStub(final BodyStub body,
                      final StatusCodeStub statusCode,
                      final HeadersStub headers
                     ) {
        super(requireNonNull(body),
              requireNonNull(statusCode),
              requireNonNull(headers),
              "patch"
             );
    }

    /**
     * Creates a PATCH handler stub that build the http response from the given body, status code and headers stubs
     * @param body the body response stub
     * @param statusCode the status code response stub
     * @param headers the headers response stub
     * @return a patch stub
     */
    public static PatchStub of(final BodyStub body,
                               final StatusCodeStub statusCode,
                               final HeadersStub headers
                              ) {
        return new PatchStub(body, statusCode, headers);
    }

    /**
     * Creates a PATCH handler stub that build the http response from the given body and status code stubs
     * @param body the body response stub
     * @param statusCode the status code response stub
     * @return a patch stub
     */
    public static PatchStub of(final BodyStub body,
                               final StatusCodeStub statusCode
                              ) {
        return new PatchStub(body, statusCode, HeadersStub.EMPTY);
    }
}
