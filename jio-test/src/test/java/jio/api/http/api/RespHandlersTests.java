package jio.api.http.api;

import com.sun.net.httpserver.HttpServer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import jio.IO;
import jio.http.client.JioHttpClient;
import jio.http.client.JioHttpClientBuilder;
import jio.http.server.HttpServerBuilder;
import jio.test.junit.Debugger;
import jio.test.stub.httpserver.BodyStub;
import jio.test.stub.httpserver.GetStub;
import jio.test.stub.httpserver.HeadersStub;
import jio.test.stub.httpserver.StatusCodeStub;
import jsonvalues.JsObj;
import jsonvalues.JsStr;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class RespHandlersTests {

  @RegisterExtension
  static Debugger debugger = Debugger.of(Duration.ofSeconds(2));
  static int port;
  static JioHttpClient httpClient;

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
    HttpServerBuilder builder = HttpServerBuilder.of(Map.of("/get_str",
                                                            getStrReqHandler,
                                                            "/get_json",
                                                            getJsonReqHandler
                                                           )
                                                    );

    HttpServer server = builder.startAtRandom("localhost",
                                              8000,
                                              9000
                                             )
                               .getOutput();

    port = server.getAddress()
                 .getPort();

    httpClient = JioHttpClientBuilder.of(HttpClient.newBuilder())
                                     .get();

  }

  @Test
  public void test_get_str() throws Exception {

    String uri = String.format("http://localhost:%s/get_str",
                               port
                              );

    IO<HttpResponse<String>> val = httpClient.ofString()
                                             .apply(HttpRequest.newBuilder()
                                                               .GET()
                                                               .uri(URI.create(uri))
                                                   );

    HttpResponse<String> resp = val.compute()
                                   .getOutputOrThrow();
    Assertions.assertEquals("foo",
                            resp.body()
                           );
    Assertions.assertEquals(200,
                            resp.statusCode()
                           );

  }

  @Test
  public void test_get_json() throws Exception {

    String uri = String.format("http://localhost:%s/get_json",
                               port
                              );

    IO<HttpResponse<String>> val = httpClient.ofString()
                                             .apply(HttpRequest.newBuilder()
                                                               .GET()
                                                               .uri(URI.create(uri))
                                                   );

    HttpResponse<String> resp = val.compute()
                                   .getOutputOrThrow();
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
