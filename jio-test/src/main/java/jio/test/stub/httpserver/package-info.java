/**
 * This package provides a set of classes and interfaces to create stubs for HTTP server request handlers. These stubs
 * allow you to simulate HTTP server responses for testing and mocking purposes. These stubs are particularly useful
 * when testing client applications that make HTTP requests to external services.
 *
 * <p>The main classes and interfaces in this package include:
 *
 * <ul>
 * <li>{@link jio.test.stub.httpserver.HttpRespStub}: Represents a stub that stands in for an HTTP response.
 * </li>
 * <li>{@link jio.test.stub.httpserver.BodyStub}: Stub that stands in for the body response of an HTTP request.
 * </li>
 * <li>{@link jio.test.stub.httpserver.HeadersStub}: Stub that stands in for the response headers of an HTTP request.
 * </li>
 * <li>{@link jio.test.stub.httpserver.StatusCodeStub}: Stub that stands in for the status code of the response of
 * an HTTP request.
 * </li>
 * <li>Concrete request handler stubs for various HTTP methods, such as {@link jio.test.stub.httpserver.GetStub},
 * {@link jio.test.stub.httpserver.PostStub}, {@link jio.test.stub.httpserver.PutStub}, {@link
 * jio.test.stub.httpserver.PatchStub}, and {@link jio.test.stub.httpserver.DeleteStub}.
 * </li>
 * </ul>
 *
 * <p><strong>Example HTTP Server Creation:</strong></p>
 *
 * <p>Here's an example of how to create an HTTP server using the {@link jio.http.server.HttpServerBuilder} and
 * integrate it with request handler stubs:</p>
 *
 * <pre>{@code
 * import com.sun.net.httpserver.HttpServer;
 * import jio.test.stub.httpserver.BodyStub;
 * import jio.test.stub.httpserver.GetStub;
 * import jio.test.stub.httpserver.HeadersStub;
 * import jio.test.stub.httpserver.StatusCodeStub;
 *
 * // Define request handler stubs for GET requests
 * GetStub getStrReqHandler = GetStub.of(BodyStub.gen(StrGen.alphabetic()),
 *                                       StatusCodeStub.cons(200),
 *                                       HeadersStub.EMPTY
 *                                      );
 * GetStub getJsonReqHandler = GetStub.of(BodyStub.cons("{\"message\":\"Hello, World!\"}"),
 * StatusCodeStub.cons(200),
 * HeadersStub.EMPTY
 * );
 *
 * // Build an HTTP server with the defined stubs
 * HttpServerBuilder builder =
 * new HttpServerBuilder().addContext("/get_str", getStrReqHandler)
 * .addContext("/get_json", getJsonReqHandler);
 *
 * // Start the HTTP server
 * HttpServer server = builder.startAtRandom("localhost", 8000, 9000);
 * }</pre>
 *
 * <p>In this example, request handler stubs are defined for GET requests using
 * {@link jio.test.stub.httpserver.GetStub}.
 * An HTTP server is created using the {@link jio.http.server.HttpServerBuilder}, and the stubs are associated with
 * specific contexts.</p>
 *
 * <p>You can then use this HTTP server to simulate responses for testing purposes.</p>
 */
package jio.test.stub.httpserver;
