package jio.test.stub.httpserver;

import fun.gen.Gen;

import java.util.function.Supplier;

/**
 * Stub that stands in for the status code of the response of an HTTP request.
 */
public interface StatusCodeStub extends HttpRespStub<Integer> {

    static StatusCodeStub cons(Gen<Integer> gen){
        Supplier<Integer> supplier = gen.sample();
        return n -> bodyReq -> uri -> headers -> supplier.get();
    }

    /**
     * Creates a status code stub that always returns the specified status code.
     *
     * @param code The status code to return.
     * @return A status code stub.
     */
    static StatusCodeStub cons(int code) {
        return n -> bodyReq -> uri -> headers -> code;
    }

}
