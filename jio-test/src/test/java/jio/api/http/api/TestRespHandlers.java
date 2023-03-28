package jio.api.http.api;

import com.sun.net.httpserver.HttpServer;
import jio.IO;
import jio.http.client.MyHttpClient;
import jio.http.client.MyHttpClientBuilder;
import jio.http.server.HttpServerBuilder;
import jio.test.junit.JioDebugger;
import jio.test.junit.DebuggerDuration;
import jio.test.stub.httpserver.BodyStub;
import jio.test.stub.httpserver.GetStub;
import jio.test.stub.httpserver.HeadersStub;
import jio.test.stub.httpserver.StatusCodeStub;
import jsonvalues.JsObj;
import jsonvalues.JsStr;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@ExtendWith(JioDebugger.class)
@DebuggerDuration(millis = 1000)
public class TestRespHandlers {

    static int port;
    static MyHttpClient httpClient;

    @BeforeAll
    public static void prepare() {

        GetStub getStrReqHandler = GetStub.of(BodyStub.cons("foo"),
                                              StatusCodeStub.cons(200),
                                              HeadersStub.EMPTY
                                             );
        GetStub getJsonReqHandler = GetStub.of(BodyStub.cons(JsObj.of("a",
                                                                      JsStr.of("b")
                                                                     )
                                                                  .toString()),
                                               StatusCodeStub.cons(200),
                                               HeadersStub.EMPTY
                                              );
        HttpServerBuilder builder =
                new HttpServerBuilder().addContext("/get_str",
                                                   getStrReqHandler
                                                  )
                                       .addContext("/get_json",
                                                   getJsonReqHandler
                                                  );

        IO<HttpServer> server = builder.startAtRandom("localhost",
                                                      8000,
                                                      9000
                                                     );

        port = server.join()
                     .getAddress()
                     .getPort();

        httpClient = new MyHttpClientBuilder(HttpClient.newBuilder()
                                                       .build()).create();

    }

    @Test
    public void test_get_str() {

        String uri = String.format("http://localhost:%s/get_str",
                                   port
                                  );

        IO<HttpResponse<String>> val =
                httpClient.ofString().apply(HttpRequest.newBuilder()
                                                       .GET()
                                                       .uri(URI.create(uri))
                                           );


        HttpResponse<String> resp = val.get()
                                       .join();
        Assertions.assertEquals("foo",
                                resp.body()
                               );
        Assertions.assertEquals(200,
                                resp.statusCode()
                               );

    }


    @Test
    public void test_get_json() {

        String uri = String.format("http://localhost:%s/get_json",
                                   port
                                  );

        IO<HttpResponse<String>> val =
                httpClient.ofString()
                          .apply(HttpRequest.newBuilder()
                                            .GET()
                                            .uri(URI.create(uri))
                                );

        HttpResponse<String> resp = val.get()
                                       .join();
        Assertions.assertEquals(JsObj.of("a",
                                         JsStr.of("b")
                                        ),
                                JsObj.parse(resp.body())
                               );
        Assertions.assertEquals(200,
                                resp.statusCode()
                               );
    }


}
