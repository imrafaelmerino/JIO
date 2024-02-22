package jio.test.stub.httpserver;

import fun.gen.Gen;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Stub that stands in for the status code of the response of an HTTP request.
 */
public non-sealed interface StatusCodeStub extends HttpRespStub<Integer> {

  /**
   * Creates a status code stub where each call returns the value generated by the given generator.
   *
   * @param gen The generator for generating status code values.
   * @return A status code stub that returns the generated status code.
   * @throws NullPointerException if the provided generator is null.
   */
  static StatusCodeStub gen(final Gen<Integer> gen) {
    Supplier<Integer> supplier = Objects.requireNonNull(gen)
                                        .sample();
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
