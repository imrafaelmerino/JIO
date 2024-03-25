package jio.api.http.api;

import static jio.ExceptionFun.IS_CONNECTION_REFUSE;
import static jio.http.client.HttpExceptionFun.IS_CONNECTION_TIMEOUT;

import com.sun.net.httpserver.HttpServer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Map;
import jio.Result;
import jio.Result.Success;
import jio.RetryPolicies;
import jio.http.client.JioHttpClientBuilder;
import jio.http.client.oauth.AccessTokenRequest;
import jio.http.client.oauth.ClientCredentialsBuilder;
import jio.http.client.oauth.GetAccessToken;
import jio.http.client.oauth.OauthHttpClient;
import jio.http.server.HttpServerBuilder;
import jio.test.junit.Debugger;
import jio.test.stub.httpserver.BodyStub;
import jio.test.stub.httpserver.GetStub;
import jio.test.stub.httpserver.PostStub;
import jio.test.stub.httpserver.StatusCodeStub;
import jsonvalues.JsObj;
import jsonvalues.JsStr;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class TestExample {

  @RegisterExtension
  static Debugger debugger = Debugger.of(Duration.ofSeconds(2));

  int PORT = 7777;
  Result<HttpServer> server;

  {
    BodyStub getTokenBodyStub = serverReqCounter -> reqBody -> reqUri -> reqHeaders ->
        JsObj.of("access_token", JsStr.of(String.valueOf(serverReqCounter))).toString();

    StatusCodeStub getTokenStatusCodeStub = StatusCodeStub.cons(200);

    BodyStub handlerBodyStub = serverReqCounter -> body -> uri -> headers ->
        serverReqCounter == 2 ? "" : String.valueOf(serverReqCounter);

    StatusCodeStub handlerStatusCodeStub =
        serverReqCounter -> body -> uri -> headers -> serverReqCounter == 2 ? 401 : 200;

    server = HttpServerBuilder.of(Map.of("/token",
                                         PostStub.of(getTokenBodyStub,
                                                     getTokenStatusCodeStub
                                                    ),
                                         "/thanks",
                                         GetStub.of(handlerBodyStub,
                                                    handlerStatusCodeStub
                                                   )
                                        )
                                 )
                              .start(PORT);
  }

  JioHttpClientBuilder clientBuilder =
      JioHttpClientBuilder.of(HttpClient.newBuilder()
                                        .connectTimeout(Duration.ofMillis(300)))
                          .withRetryPolicy(RetryPolicies.incrementalDelay(Duration.ofMillis(10))
                                                        .append(RetryPolicies.limitRetries(5)))
                          .withRetryPredicate(IS_CONNECTION_TIMEOUT.or(IS_CONNECTION_REFUSE));

  URI tokenUri = URI.create("http://localhost:%s/token".formatted(7777));
  OauthHttpClient client =
      ClientCredentialsBuilder.of(clientBuilder,
                                  AccessTokenRequest.of("client_id",
                                                        "client_secret",
                                                        tokenUri
                                                       ),
                                  GetAccessToken.DEFAULT,
                                  //token in access_token key in a JSON
                                  resp -> resp.statusCode() == 401
                                  // if 401 go for a new token
                                 )
                              .get();

  @Test
  public void testOuth() {
    URI uri = URI.create("http://localhost:%s/thanks".formatted(PORT));
    Assertions.assertEquals(new Success<>("4"),
                            client.oauthOfString()
                                  .apply(HttpRequest.newBuilder()
                                                    .uri(uri))
                                  .compute()
                                  .map(resp -> resp.body())
                           );

  }

}
