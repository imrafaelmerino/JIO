package jio.api.http.api;

import com.sun.net.httpserver.HttpServer;
import jio.IO;
import jio.http.client.HttpExceptions;
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
    HttpServer server =
            HttpServerBuilder.of(Map.of("/foo",
                                        GetStub.of(BodyStub.consAfter("hi",
                                                                      Duration.of(2,
                                                                                  ChronoUnit.SECONDS
                                                                                 )
                                                                     ),
                                                   StatusCodeStub.cons(200),
                                                   HeadersStub.EMPTY
                                                  )
                                       ))
                             .startAtRandom("localhost",
                                            8000,
                                            9000
                                           );


    @Test
    public void test_http_connect_timeout() {

        JioHttpClient client =
                JioHttpClientBuilder.of(HttpClient.newBuilder()
                                                  .connectTimeout(
                                                          Duration.of(1,
                                                                      ChronoUnit.NANOS
                                                                     )
                                                                 )).build();

        boolean isConnectTimeout =
                client.ofString().apply(HttpRequest.newBuilder()
                                                   .GET()
                                                   .uri(URI.create("https://www.google.com")))
                      .then(response -> IO.FALSE,
                            failure -> IO.succeed(HttpExceptions.CONNECTION_TIMEOUT.test(failure)
                                                 )
                           )
                      .result();
        Assertions.assertTrue(isConnectTimeout);
    }

    /**
     * you also receive the failure UNRESOLVED_ADDRESS_CAUSE_PRISM whe the router is off
     */
    @Test
    public void test_domain_doesnt_exists() {

        JioHttpClient client =
                JioHttpClientBuilder.of(HttpClient.newBuilder()).build();

        boolean isUnresolved =
                client.ofString().apply(HttpRequest.newBuilder()
                                                   .GET()
                                                   .uri(URI.create("https://www.google.foo")))
                      .then(response -> IO.FALSE,
                            failure -> IO.succeed(HttpExceptions.UNRESOLVED_SOCKET_ADDRESS
                                                          .test(failure)
                                                 )
                           )
                      .result();

        Assertions.assertTrue(isUnresolved);

    }

    @Test
    public void test_http_timeout() {


        JioHttpClient client =
                JioHttpClientBuilder.of(HttpClient.newBuilder()
                                                  .connectTimeout(Duration.of(1,
                                                                              ChronoUnit.NANOS
                                                                             )
                                                                 )
                                       )
                                    .build();


        URI uri = URI.create("http://localhost:" + server.getAddress()
                                                         .getPort() + "/foo");
        boolean isTimeout =
                client.ofString().apply(HttpRequest.newBuilder()
                                                   .GET()
                                                   .uri(uri)
                                       )
                      .then(response -> IO.FALSE,
                            failure -> IO.succeed(HttpExceptions.CONNECTION_TIMEOUT
                                                          .test(failure)
                                                 )
                           )
                      .result();

        Assertions.assertTrue(isTimeout);

    }

}
