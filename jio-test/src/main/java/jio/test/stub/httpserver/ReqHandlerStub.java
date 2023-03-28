package jio.test.stub.httpserver;

abstract class ReqHandlerStub extends AbstractReqHandlerStub {

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
