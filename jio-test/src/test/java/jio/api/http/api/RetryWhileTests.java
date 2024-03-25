package jio.api.http.api;

import com.sun.net.httpserver.HttpServer;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import jio.http.client.JioHttpClient;
import jio.http.client.JioHttpClientBuilder;
import jio.http.server.HttpServerBuilder;
import jio.test.junit.Debugger;
import jio.test.stub.httpserver.GetStub;
import jio.test.stub.httpserver.HeadersStub;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.RegisterExtension;

public class RetryWhileTests {

  @RegisterExtension
  static Debugger debugger = Debugger.of(Duration.ofSeconds(2));

  static int port;
  static JioHttpClient httpClient;

  @BeforeAll
  public static void prepare()  {

    GetStub getStrReqHandler = GetStub.of(n -> bodyReq -> uri -> headers -> n <= 3 ? "not found" : "success",
                                          n -> bodyReq -> uri -> headers -> n <= 3 ? 404 : 200,
                                          HeadersStub.EMPTY
                                         );

    HttpServerBuilder builder = HttpServerBuilder.of(Map.of("/get_str",
                                                            getStrReqHandler
                                                           ));

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


}
