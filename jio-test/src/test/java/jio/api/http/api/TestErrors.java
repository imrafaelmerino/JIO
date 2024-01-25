package jio.api.http.api;

import com.sun.net.httpserver.HttpServer;
import jio.ExceptionFun;
import jio.IO;
import jio.http.client.HttpExceptionFun;
import jio.http.client.JioHttpClient;
import jio.http.client.JioHttpClientBuilder;
import jio.http.server.HttpServerBuilder;
import jio.test.junit.Debugger;
import jio.test.stub.httpserver.BodyStub;
import jio.test.stub.httpserver.GetStub;
import jio.test.stub.httpserver.HeadersStub;
import jio.test.stub.httpserver.StatusCodeStub;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;


public class TestErrors {

  @RegisterExtension
  static Debugger debugger = Debugger.of(Duration.ofSeconds(2));
  HttpServer server = HttpServerBuilder.of(Map.of("/foo",
                                                  GetStub.of(BodyStub.consAfter("hi",
                                                                                Duration.ofSeconds(2)),
                                                             StatusCodeStub.cons(200),
                                                             HeadersStub.EMPTY)))
                                       .startAtRandom("localhost",
                                                      8000,
                                                      9000);


  @Test
  public void test_http_connect_timeout() {

    JioHttpClient client = JioHttpClientBuilder.of(HttpClient.newBuilder()
                                                             .connectTimeout(Duration.ofNanos(1)))
                                               .get();

    boolean isConnectTimeout = client.ofString()
                                     .apply(HttpRequest.newBuilder()
                                                       .GET()
                                                       .uri(URI.create("https://www.google.com")))
                                     .then(response -> IO.FALSE,
                                           failure -> IO.succeed(HttpExceptionFun.HAS_CONNECTION_TIMEOUT.test(failure)))
                                     .join();
    Assertions.assertTrue(isConnectTimeout);
  }

  /**
   * you also receive the failure UNRESOLVED_ADDRESS_CAUSE_PRISM whe the router is off
   */
  @Test
  public void test_domain_doesnt_exists() {

    JioHttpClient client = JioHttpClientBuilder.of(HttpClient.newBuilder())
                                               .get();

    boolean isUnresolved = client.ofString()
                                 .apply(HttpRequest.newBuilder()
                                                   .GET()
                                                   .uri(URI.create("https://www.google.foo")))
                                 .then(response -> IO.FALSE,
                                       failure -> IO.succeed(ExceptionFun.HAS_CONNECT_EXCEPTION.test(failure)))
                                 .join();

    Assertions.assertTrue(isUnresolved);

  }


  @Test
  public void test_http_timeout() {

    JioHttpClient client = JioHttpClientBuilder.of(HttpClient.newBuilder()
                                                             .connectTimeout(Duration.of(1,
                                                                                         ChronoUnit.NANOS)))
                                               .get();

    URI uri = URI.create("http://localhost:%s/foo".formatted(server.getAddress()
                                                                   .getPort()));
    boolean isTimeout = client.ofString()
                              .apply(HttpRequest.newBuilder()
                                                .GET()
                                                .uri(uri))
                              .then(response -> IO.FALSE,
                                    failure -> IO.succeed(HttpExceptionFun.HAS_CONNECTION_TIMEOUT.test(failure)))
                              .join();

    Assertions.assertTrue(isTimeout);

  }

  @Test
  public void testRequestTimeout() {
    GetStub getStrReqHandler = GetStub.of(BodyStub.consAfter("",
                                                             Duration.ofSeconds(1)),
                                          n -> bodyReq -> uri -> headers -> 200,
                                          HeadersStub.EMPTY);

    HttpServerBuilder builder = HttpServerBuilder.of(Map.of("/foo",
                                                            getStrReqHandler));

    HttpServer server = builder.startAtRandom("localhost",
                                              8000,
                                              9000);

    JioHttpClient client = JioHttpClientBuilder.of(HttpClient.newBuilder())
                                               .get();

    URI uri = URI.create("http://localhost:" + server.getAddress()
                                                     .getPort() + "/foo");
    boolean isRequestTimeout = client.ofString()
                                     .apply(HttpRequest.newBuilder()
                                                       .GET()
                                                       .uri(uri)
                                                       .timeout(Duration.ofMillis(500)))
                                     .then(response -> IO.FALSE,
                                           failure -> IO.succeed(HttpExceptionFun.HAS_REQUEST_TIMEOUT.test(failure)))
                                     .join();

    Assertions.assertTrue(isRequestTimeout);

  }

}
