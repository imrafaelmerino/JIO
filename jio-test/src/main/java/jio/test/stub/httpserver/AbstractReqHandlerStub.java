package jio.test.stub.httpserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

abstract class AbstractReqHandlerStub implements HttpHandler {

    static int counter = 0;

    private final Function<HttpExchange, Headers> headers;
    private final Function<HttpExchange, Integer> code;
    private final String method;
    private final Function<HttpExchange, String> body;

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
                    exchange.sendResponseHeaders(code.apply(exchange),
                                                 body.apply(exchange)
                                                     .getBytes(StandardCharsets.UTF_8)
                                                         .length
                                                );
                    outputStream.write(body.apply(exchange)
                                           .getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                }

            } catch (Exception e) {
                returnExceptionMessageError(exchange,
                                            e
                                           );
            }
        } else returnUnexpectedHttpMethodError(exchange,requestMethod);

    }

    private void returnExceptionMessageError(HttpExchange exchange,
                                             Exception e
                                            ) throws IOException {
        var outputStream = exchange.getResponseBody();
        var response = e.getMessage();
        exchange.sendResponseHeaders(500,
                                     response.getBytes(StandardCharsets.UTF_8).length
                                    );
        outputStream.write(response.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        outputStream.close();
    }

    private void returnUnexpectedHttpMethodError(HttpExchange exchange,String requestMethod) throws IOException {
        try (var outputStream = exchange.getResponseBody()) {
            var response = method + " method was expected, but "+requestMethod+" was received.";
            exchange.sendResponseHeaders(500,
                                         response.getBytes(StandardCharsets.UTF_8).length
                                        );
            outputStream.write(response.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }
    }

}
