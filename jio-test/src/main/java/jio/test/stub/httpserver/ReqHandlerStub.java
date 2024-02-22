package jio.test.stub.httpserver;

/**
 * Abstract base class for creating request handler stubs for HTTP methods like PUT, POST, PATCH, etc.
 */
sealed abstract class ReqHandlerStub extends AbstractReqHandlerStub permits DeleteStub, GetStub,
                                                                            OptionsStub, PatchStub, PostStub, PutStub {

  /**
   * Constructs a request handler stub with the specified components.
   *
   * @param body       The body response stub.
   * @param statusCode The status code response stub.
   * @param headers    The headers response stub.
   * @param method     The HTTP method associated with this request handler stub.
   */
  ReqHandlerStub(final BodyStub body,
                 final StatusCodeStub statusCode,
                 final HeadersStub headers,
                 final String method
                ) {
    super(e -> headers.apply(counter)
                      .apply(e.getRequestBody())
                      .apply(e.getRequestURI())
                      .apply(e.getRequestHeaders()),
          e -> statusCode.apply(counter)
                         .apply(e.getRequestBody())
                         .apply(e.getRequestURI())
                         .apply(e.getRequestHeaders()),
          e -> body.apply(counter)
                   .apply(e.getRequestBody())
                   .apply(e.getRequestURI())
                   .apply(e.getRequestHeaders()),
          method
         );
  }
}
