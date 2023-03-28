package jio.api.http.api;

import com.sun.net.httpserver.HttpServer;
import jio.IO;
import jio.http.HttpExceptions;
import jio.http.client.MyHttpClient;
import jio.http.client.MyHttpClientBuilder;
import jio.http.server.HttpServerBuilder;
import jio.test.junit.JioDebugger;
import jio.test.junit.DebuggerDuration;
import jio.test.stub.httpserver.BodyStub;
import jio.test.stub.httpserver.GetStub;
import jio.test.stub.httpserver.HeadersStub;
import jio.test.stub.httpserver.StatusCodeStub;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@ExtendWith(JioDebugger.class)
@DebuggerDuration(millis = 1000)
public class TestErrors {


    IO<HttpServer> server =
            new HttpServerBuilder().addContext("/foo",
                                               GetStub.of(BodyStub.consAfter("hi",
                                                                             Duration.of(2,
                                                                                         ChronoUnit.SECONDS
                                                                                        )
                                                                            ),
                                                          StatusCodeStub.cons(200),
                                                          HeadersStub.EMPTY
                                                         )
                                              )
                                   .startAtRandom("localhost",
                                                  8000,
                                                  9000
                                                 );

    @Test
    public void test_http_connect_timeout() {

        MyHttpClient client = new MyHttpClientBuilder(HttpClient.newBuilder()
                                                                .connectTimeout(
                                                                        Duration.of(1,
                                                                                    ChronoUnit.NANOS
                                                                                   )
                                                                               ).build()).create();

        boolean isConnectTimeout = client.ofString().apply(HttpRequest.newBuilder()
                                                                      .GET()
                                                                      .uri(URI.create("https://www.google.com")))
                                         .then(response -> IO.FALSE,
                                               failure -> IO.succeed(HttpExceptions.CONNECTION_TIMEOUT.test(failure)
                                                                    )
                                              )
                                         .join();
        Assertions.assertTrue(isConnectTimeout);
    }

    /**
     * you also receive the failure UNRESOLVED_ADDRESS_CAUSE_PRISM whe the router is off
     */
    @Test
    public void test_domain_doesnt_exists() {

        MyHttpClient client = new MyHttpClientBuilder(HttpClient.newHttpClient()).create();

        boolean isUnresolved = client.ofString().apply(HttpRequest.newBuilder()
                                                                  .GET()
                                                                  .uri(URI.create("https://www.google.foo")))
                                     .then(response -> IO.FALSE,
                                           failure -> IO.succeed(HttpExceptions.UNRESOLVED_SOCKET_ADDRESS
                                                                         .test(failure)
                                                                )
                                          )
                                     .join();

        Assertions.assertTrue(isUnresolved);

    }

    @Test
    public void test_http_timeout() {


        HttpServer join = server.join();

        MyHttpClient client = new MyHttpClientBuilder(HttpClient.newBuilder()
                                                                .connectTimeout(Duration.of(1,
                                                                                            ChronoUnit.NANOS
                                                                                           )
                                                                               )
                                                                .build()
        )
                .create();


        URI uri = URI.create("http://localhost:" + join.getAddress()
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
                      .join();

        Assertions.assertTrue(isTimeout);


    }


}
