package jio.http.server;

import static java.util.Objects.requireNonNull;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import jio.ExceptionFun;
import jio.IO;
import jio.Result;

/**
 * Builder to create {@link HttpServer http servers}. The start method of the server is wrapped into a {@link IO}. It
 * allows you to define some interesting methods like {@link #startAtRandom(int, int)} (int, int)}, which sets the
 * server to listen on the first available port it finds. An Executor must be established with
 * {@link #withExecutor(Executor)}, so that all HTTP requests are handled in tasks given to the executor. If no executor
 * is defined, then a default implementation is used, which uses the thread created by the start() method.
 * <p>
 * This server builder is particularly useful for testing purposes. For each HTTP request, an event is created and sent
 * to the Java Flight Recorder (JFR) system, allowing you to capture and analyze request details for debugging and
 * performance analysis. Event recording is enabled by default but can be disabled if needed.
 *
 * @see ServerReqEvent
 * @see HttpServer
 * @see HttpsServer
 */
public final class HttpServerBuilder {

  private final AtomicLong counter = new AtomicLong(0);
  private final Map<String, HttpHandler> handlers;
  private Executor executor = Executors.newVirtualThreadPerTaskExecutor();
  private int backlog = 0;
  private boolean recordEvents = true;
  private HttpsConfigurator httpsConfigurator;

  private HttpServerBuilder(Map<String, HttpHandler> handlers) {
    this.handlers = handlers;
  }

  private static String headersToString(Map<String, List<String>> headers) {
    return headers.entrySet()
                  .stream()
                  .map(e -> String.format("%s:%s",
                                          e.getKey(),
                                          e.getValue()
                                           .size() == 1 ? e.getValue()
                                                           .getFirst() : e.getValue()
                                         )
                      )
                  .collect(Collectors.joining(", "));
  }

  /**
   * Creates an instance of the HttpServerBuilder with the specified HTTP request handlers.
   *
   * @param handlers A map of HTTP request handlers, where the keys are the path prefixes and the values are the
   *                 handlers.
   * @return An instance of HttpServerBuilder.
   */
  public static HttpServerBuilder of(final Map<String, HttpHandler> handlers) {
    return new HttpServerBuilder(requireNonNull(handlers));
  }

  /**
   * Sets this server's Executor object. All HTTP requests are handled in tasks given to this executor.
   *
   * @param executor the executor
   * @return this builder
   */
  public HttpServerBuilder withExecutor(final Executor executor) {
    this.executor = requireNonNull(executor);
    return this;
  }

  /**
   * Sets an HttpsConfigurator for configuring SSL settings for the HTTP server. The HttpsConfigurator allows you to
   * specify SSL-related settings such as SSLContext, SSLParameters, and more for secure connections. This is useful for
   * setting up HTTPS for the HTTP server.
   *
   * @param configurator the HttpsConfigurator to configure SSL settings
   * @return this builder
   * @see HttpsConfigurator
   */
  public HttpServerBuilder withSSL(final HttpsConfigurator configurator) {
    this.httpsConfigurator = requireNonNull(configurator);
    return this;
  }

  /**
   * Sets the socket backlog, specifying the number of incoming connections that can be queued for acceptance.
   *
   * @param backlog the socket backlog
   * @return this builder
   */
  public HttpServerBuilder withBacklog(final int backlog) {
    this.backlog = backlog;
    return this;
  }

  /**
   * Disables the recording of Java Flight Recorder (JFR) events for HTTP requests handled by the server. By default,
   * JFR events are recorded. Use this method to disable recording if needed.
   *
   * @return This builder with JFR event recording disable.
   */
  public HttpServerBuilder withoutRecordedEvents() {
    this.recordEvents = false;
    return this;
  }

  /**
   * Try to create a HttpServer and wraps any outcome in a Result object.
   * Create a socket address from <strong>localhost</strong> and a port number from a given interval, starting the
   * server in a new background thread. The background thread inherits the priority, thread group, and context class
   * loader of the caller. A valid port output is between 0 and 65535. A port number of zero will let the system pick up
   * an ephemeral port in a bind operation.
   *
   * @param start the first port number that will be tried
   * @param end   the last port number that will be tried
   * @return a Result with a HttpServer in case of success
   */
  public Result<HttpServer> startAtRandom(final int start,
                                          final int end
                                         ) {
    return buildAtRandomRec("localhost",
                            start,
                            end)
        .compute();

  }

  /**
   * Try to create a HttpServer and wraps any outcome in a Result object.
   * Create a socket address from a hostname and a port number from a given interval, starting the server in a new
   * background thread. The background thread inherits the priority, thread group, and context class loader of the
   * caller. A valid port output is between 0 and 65535. A port number of zero will let the system pick up an ephemeral
   * port in a bind operation.
   *
   * @param host  the host name
   * @param start the first port number that will be tried
   * @param end   the last port number that will be tried
   * @return a Result with a HttpServer in case of success
   */
  public Result<HttpServer> startAtRandom(final String host,
                                          final int start,
                                          final int end
                                         ) {
    if (start <= 0) {
      throw new IllegalArgumentException("start <= 0");
    }
    if (start > end) {
      throw new IllegalArgumentException("start greater than end");
    }
    return buildAtRandomRec(host,
                            start,
                            end
                           ).compute();

  }

  private IO<HttpServer> buildAtRandomRec(final String host,
                                          final int start,
                                          final int end
                                         ) {
    if (start == end) {
      throw new IllegalArgumentException("range of ports exhausted");
    }
    return build(requireNonNull(host),
                 start
                ).recoverWith(_ -> buildAtRandomRec(host,
                                                    start + 1,
                                                    end));
  }

  /**
   * Returns an effect that when computed will try to create a socket address from a hostname and a port number,
   * starting the server in a new background thread. The background thread inherits the priority, thread group, and
   * context class
   * loader of the caller. A valid port output is between 0 and 65535. A port number of zero will let the system pick up
   * an ephemeral port in a bind operation.
   *
   * @param host the host name
   * @param port the port number
   * @return an effect that deploys the HttpServer
   */
  private IO<HttpServer> build(final String host,
                               final int port
                              ) {
    if (port <= 0) {
      throw new IllegalArgumentException("port <= 0");
    }
    Objects.requireNonNull(host);

    return IO.effect(() -> {
      try {
        HttpServer server;
        if (httpsConfigurator == null) {
          server = HttpServer.create(new InetSocketAddress(host,
                                                           port),
                                     backlog);
        } else {
          server = HttpsServer.create(new InetSocketAddress(host,
                                                            port),
                                      backlog);
          ((HttpsServer) server).setHttpsConfigurator(httpsConfigurator);
        }

        if (executor != null) {
          server.setExecutor(executor);
        }
        var keySet = handlers.keySet();
        for (final String key : keySet) {
          server.createContext(key,
                               exchange -> {
                                 if (recordEvents) {
                                   jfrHandle(key,
                                             exchange);
                                 } else {
                                   handlers.get(key)
                                           .handle(exchange);
                                 }
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

  private void jfrHandle(String key,
                         HttpExchange exchange
                        ) {
    ServerReqEvent event = new ServerReqEvent();
    event.reqCounter = counter.incrementAndGet();
    event.remoteHostAddress = exchange.getRemoteAddress()
                                      .getHostName();
    event.remoteHostPort = exchange.getRemoteAddress()
                                   .getPort();
    event.protocol = exchange.getProtocol();
    event.method = exchange.getRequestMethod();
    event.uri = exchange.getRequestURI()
                        .toString();
    event.reqHeaders = headersToString(exchange.getRequestHeaders());
    event.begin();
    try {
      handlers.get(key)
              .handle(exchange);
      event.statusCode = exchange.getResponseCode();
      event.result = ServerReqEvent.RESULT.SUCCESS.name();
    } catch (IOException e) {
      var cause = ExceptionFun.findUltimateCause(e);
      event.exception = String.format("%s:%s",
                                      cause.getClass()
                                           .getName(),
                                      cause.getMessage()
                                     );
      event.result = ServerReqEvent.RESULT.FAILURE.name();
    } finally {
      event.commit();
    }
  }

  /**
   * Try to create a HttpServer and wraps any outcome in a Result object. Creates a socket address from
   * <strong>localhost</strong> and a port number, starting the server in a new background
   * thread. The background thread inherits the priority, thread group, and context class loader of the caller. A valid
   * port output is between 0 and 65535. A port number of zero will let the system pick up an ephemeral port in a bind
   * operation.
   *
   * @param port the port number
   * @return a Result with a HttpServer in case of success
   */
  public Result<HttpServer> start(final int port) {
    return build("localhost",
                 port
                ).compute();

  }

  /**
   * Try to create a HttpServer and wraps any outcome in a Result object.
   * Creates a socket address from a host and a port number, starting the server in a new background thread. The
   * background thread inherits the priority, thread group, and context class loader of the caller. A valid port output
   * is between 0 and 65535. A port number of zero will let the system pick up an ephemeral port in a bind operation.
   *
   * @param host the host address
   * @param port the port number
   * @return a Result with a HttpServer in case of success
   */
  public Result<HttpServer> start(final String host,
                                  final int port) {
    return build(host,
                 port
                )
        .compute();

  }

}
