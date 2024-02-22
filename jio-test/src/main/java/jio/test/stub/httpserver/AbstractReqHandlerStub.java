package jio.test.stub.httpserver;

import static java.util.Objects.requireNonNull;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * An abstract base class for implementing request handler stubs for HTTP server testing. This class allows you to
 * define custom behaviors for handling HTTP requests based on various parameters such as HTTP method, response headers,
 * response status code, and response body.
 */
abstract class AbstractReqHandlerStub implements HttpHandler {

  static int counter = 0;

  private final Function<HttpExchange, Headers> headers;
  private final Function<HttpExchange, Integer> code;
  private final String method;
  private final Function<HttpExchange, String> body;

  /**
   * Creates an instance of the abstract request handler stub with the specified behaviors.
   *
   * @param headers A function that provides response headers based on the HTTP exchange.
   * @param code    A function that provides the response status code based on the HTTP exchange.
   * @param body    A function that provides the response body based on the HTTP exchange.
   * @param method  The expected HTTP method for handling requests.
   */
  public AbstractReqHandlerStub(final Function<HttpExchange, Headers> headers,
                                final Function<HttpExchange, Integer> code,
                                final Function<HttpExchange, String> body,
                                final String method
                               ) {
    this.headers = requireNonNull(headers);
    this.code = requireNonNull(code);
    this.method = requireNonNull(method);
    this.body = requireNonNull(body);
  }

  /**
   * Handles an HTTP request by applying custom behaviors based on the expected HTTP method.
   *
   * @param exchange The HTTP exchange to handle.
   * @throws IOException If an I/O error occurs during request handling.
   */
  @Override
  public void handle(final HttpExchange exchange) throws IOException {
    counter += 1;
    String requestMethod = requireNonNull(exchange).getRequestMethod();
    if (requestMethod.equalsIgnoreCase(method)) {
      try {
        var headers = exchange.getResponseHeaders();
        var keySet = this.headers.apply(exchange)
                                 .keySet();
        for (final String key : keySet) {
          var values = this.headers.apply(exchange)
                                   .get(key);
          for (final String value : values) {
            headers.add(key,
                        value
                       );
          }
        }

        try (var outputStream = exchange.getResponseBody()) {
          byte[] bodyBytes = body.apply(exchange)
                                 .getBytes(StandardCharsets.UTF_8);
          exchange.sendResponseHeaders(code.apply(exchange),
                                       bodyBytes.length
                                      );
          outputStream.write(bodyBytes);
          outputStream.flush();
        }

      } catch (Exception e) {
        returnExceptionMessageError(exchange,
                                    e
                                   );
      }
    } else {
      returnUnexpectedHttpMethodError(exchange,
                                      requestMethod);
    }

  }

  private void returnExceptionMessageError(HttpExchange exchange,
                                           Exception e
                                          ) throws IOException {
    var outputStream = exchange.getResponseBody();
    var response = e.getMessage();
    byte[] bytesResponse = response.getBytes(StandardCharsets.UTF_8);
    exchange.sendResponseHeaders(500,
                                 bytesResponse.length
                                );
    outputStream.write(bytesResponse);
    outputStream.flush();
    outputStream.close();
  }

  private void returnUnexpectedHttpMethodError(HttpExchange exchange,
                                               String requestMethod) throws IOException {
    try (var outputStream = exchange.getResponseBody()) {
      var response = method + " method was expected, but " + requestMethod + " was received.";
      byte[] bytesResponse = response.getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(500,
                                   bytesResponse.length
                                  );
      outputStream.write(bytesResponse);
      outputStream.flush();
    }
  }

}
