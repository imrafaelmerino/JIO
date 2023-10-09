package jio.http.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import jio.IO;
import jio.http.client.MyHttpClientBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Builder to create {@link HttpServer http servers}. The start method of the server is wrapped into a {@link IO}. It
 * allows you to define some interesting methods like {@link #startAtRandom(int, int)}, which sets the server to listen
 * on the first available port it finds. An Executor must be established with {@link #setExecutor(Executor)}, so that
 * all HTTP requests are handled in tasks given to the executor. If no executor is defined, then a default
 * implementation is used, which uses the thread created by the start() method.
 * <p>
 * This server builder is particularly useful for testing purposes. For each HTTP request, an event is created and sent
 * to the Java Flight Recorder (JFR) system, allowing you to capture and analyze request details for debugging and
 * performance analysis. Event recording is enabled by default but can be disabled if needed.
 *
 * @see ServerReqEvent
 */
public class HttpServerBuilder {

    private final AtomicLong counter = new AtomicLong(0);
    private final Map<String, HttpHandler> handlers = new HashMap<>();
    private Executor executor;
    private int backlog = 0;

    private boolean recordEvents = true;

    private static String headersToString(Map<String, List<String>> headers) {
        return
                headers.entrySet()
                       .stream()
                       .map(e -> String.format("%s:%s",
                                               e.getKey(),
                                               e.getValue()
                                              )
                           )
                       .collect(Collectors.joining(", "));
    }

    /**
     * Sets this server's Executor object. All HTTP requests are handled in tasks given to this executor.
     *
     * @param executor the executor
     * @return this builder
     */
    public HttpServerBuilder setExecutor(final Executor executor) {
        this.executor = requireNonNull(executor);
        return this;
    }

    /**
     * Associates a URI path with an HTTP request handler. Once created, all requests received by the server for the
     * specified path will be handled by the given handler object.
     *
     * @param path    the root URI path to associate with the handler (the first character of the path must be '/')
     * @param handler the handler to invoke for incoming requests
     * @return this builder
     */
    public HttpServerBuilder addContext(final String path,
                                        final HttpHandler handler
                                       ) {
        this.handlers.put(requireNonNull(path),
                          requireNonNull(handler)
                         );
        return this;
    }

    /**
     * Sets the socket backlog, specifying the number of incoming connections that can be queued for acceptance.
     *
     * @param backlog the socket backlog
     * @return this builder
     */
    public HttpServerBuilder setBacklog(final int backlog) {
        this.backlog = backlog;
        return this;
    }

    /**
     * Disables the recording of Java Flight Recorder (JFR) events for HTTP requests handled by the server.
     * By default, JFR events are recorded. Use this method to disable recording if needed.
     *
     * @return This builder with JFR event recording disable.
     */
    public HttpServerBuilder disableRecordEvents(){
        this.recordEvents = false;
        return this;
    }

    /**
     * Returns an effect that when invoked will create a socket address from <strong>localhost</strong> and a port
     * number from a given interval, starting the server in a new background thread. The background thread inherits the
     * priority, thread group, and context class loader of the caller. A valid port value is between 0 and 65535. A port
     * number of zero will let the system pick up an ephemeral port in a bind operation.
     *
     * @param start the first port number that will be tried
     * @param end   the last port number that will be tried
     * @return an effect that deploys the HttpServer
     */
    public IO<HttpServer> startAtRandom(final int start,
                                        final int end
                                       ) {
        return startAtRandom("localhost",
                             start,
                             end
                            );
    }

    /**
     * Returns an effect that when invoked will create a socket address from a hostname and a port number from a given
     * interval, starting the server in a new background thread. The background thread inherits the priority, thread
     * group, and context class loader of the caller. A valid port value is between 0 and 65535. A port number of zero
     * will let the system pick up an ephemeral port in a bind operation.
     *
     * @param host  the host name
     * @param start the first port number that will be tried
     * @param end   the last port number that will be tried
     * @return an effect that deploys the HttpServer
     */
    public IO<HttpServer> startAtRandom(final String host,
                                        final int start,
                                        final int end
                                       ) {
        if (start <= 0) throw new IllegalArgumentException("start <= 0");
        if (start > end) throw new IllegalArgumentException("start greater than end");
        return startAtRandomRec(host,
                                start,
                                end
                               );
    }

    private IO<HttpServer> startAtRandomRec(final String host,
                                            final int start,
                                            final int end
                                           ) {
        if (start == end) throw new IllegalArgumentException("range of ports exhausted");
        return start(requireNonNull(host),
                     start
                    )
                .recoverWith(error -> startAtRandomRec(host, start + 1, end));
    }

    /**
     * Returns an effect that when invoked will create a socket address from a hostname and a port number, starting the
     * server in a new background thread. The background thread inherits the priority, thread group, and context class
     * loader of the caller. A valid port value is between 0 and 65535. A port number of zero will let the system pick
     * up an ephemeral port in a bind operation.
     *
     * @param host the host name
     * @param port the port number
     * @return an effect that deploys the HttpServer
     */
    public IO<HttpServer> start(final String host,
                                final int port
                               ) {
        if (port <= 0) throw new IllegalArgumentException("port <= 0");
        Objects.requireNonNull(host);

        return IO.effect(() -> {
            try {
                var server =
                        HttpServer.create(new InetSocketAddress(host, port), backlog);
                if (executor != null) server.setExecutor(executor);
                var keySet = handlers.keySet();
                for (final String key : keySet) {
                    server.createContext(key,
                                         exchange -> {
                                             if(recordEvents)jfrHandle(key, exchange);
                                             else handlers.get(key).handle(exchange);
                                         }
                                        );
                }
                server.start();
                return CompletableFuture.completedFuture(server);
            } catch (IOException e) {
                return CompletableFuture.failedFuture(e);
            }
        });
    }

    private void jfrHandle(String key, HttpExchange exchange) {
        ServerReqEvent event = new ServerReqEvent();
        event.reqCounter = counter.incrementAndGet();
        event.remoteHostAddress = exchange.getRemoteAddress().getHostName();
        event.remoteHostPort = exchange.getRemoteAddress().getPort();
        event.protocol = exchange.getProtocol();
        event.method = exchange.getRequestMethod();
        event.uri = exchange.getRequestURI().toString();
        event.reqHeaders = headersToString(exchange.getRequestHeaders());
        event.begin();
        try {
            handlers.get(key).handle(exchange);
            event.statusCode = exchange.getResponseCode();
            event.result = ServerReqEvent.RESULT.SUCCESS.name();
        } catch (IOException e) {
            event.exception = String.format("%s:%s",
                                            e.getClass().getName(),
                                            e.getMessage()
                                           );
            event.result = ServerReqEvent.RESULT.FAILURE.name();
        } finally {
            event.commit();
        }
    }

    /**
     * Returns an effect that when invoked will create a socket address from <strong>localhost</strong> and a port
     * number, starting the server in a new background thread. The background thread inherits the priority, thread
     * group, and context class loader of the caller. A valid port value is between 0 and 65535. A port number of zero
     * will let the system pick up an ephemeral port in a bind operation.
     *
     * @param port the port number
     * @return an effect that deploys the HttpServer
     */
    public IO<HttpServer> start(final int port) {
        return start("localhost",
                     port
                    );
    }


}
