package jio.test.stub.httpserver;

/**
 * Stub that stands in for the status code of the response of a http request.
 *
 */
public interface StatusCodeStub extends HttpRespStub<Integer> {

    /**
     * stub that always returns the specified status code
     * @param code the status code
     * @return a status code stub
     */
    static StatusCodeStub cons(int code) {
        return n -> bodyReq -> uri -> headers -> code;
    }

}
