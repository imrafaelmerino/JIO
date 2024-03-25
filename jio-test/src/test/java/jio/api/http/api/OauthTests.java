package jio.api.http.api;

import com.sun.net.httpserver.HttpServer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import jio.Result;
import jio.http.client.JioHttpClientBuilder;
import jio.http.client.oauth.AccessTokenRequest;
import jio.http.client.oauth.ClientCredentialsBuilder;
import jio.http.client.oauth.GetAccessToken;
import jio.http.client.oauth.OauthHttpClient;
import jio.http.server.HttpServerBuilder;
import jio.test.junit.Debugger;
import jio.test.stub.httpserver.GetStub;
import jio.test.stub.httpserver.PostStub;
import jio.test.stub.httpserver.StatusCodeStub;
import jsonvalues.JsObj;
import jsonvalues.JsStr;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class OauthTests {

  @RegisterExtension
  static Debugger debugger = Debugger.of(Duration.ofSeconds(2));

  Result<HttpServer> unused = HttpServerBuilder.of(Map.of("/token",
                                                          PostStub.of(n -> body -> uri -> headers -> JsObj.of("access_token",
                                                                                                              JsStr.of(String.valueOf(n))
                                                                                                             )
                                                                                                          .toString(),
                                                                      StatusCodeStub.cons(200)
                                                                     ),
                                                          "/service",
                                                          GetStub.of(n -> body -> uri -> headers -> n == 2 ? "" : String
                                                                         .valueOf(n),
                                                                     n -> body -> uri -> headers -> n == 2 ? 401 : 200
                                                                    )
                                                         )
                                                  )
                                               .start(7777);

  public OauthTests() {
  }

  @Test
  public void test() {

    ClientCredentialsBuilder builder =
        ClientCredentialsBuilder.of(JioHttpClientBuilder.of(HttpClient.newBuilder()),
                                    AccessTokenRequest.of("client_id",
                                                          "client_secret",
                                                          URI.create("http://localhost:7777/token")
                                                         ),
                                    GetAccessToken.DEFAULT,
                                    resp -> resp.statusCode() == 401

                                   );

    OauthHttpClient client = builder.get();

    Result<HttpResponse<String>> unused = client.oauthOfString()
                                                .apply(HttpRequest.newBuilder()
                                                                  .GET()
                                                                  .uri(URI.create("http://localhost:7777/service"))
                                                      )
                                                .compute();

  }

}
