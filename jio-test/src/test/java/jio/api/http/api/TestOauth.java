package jio.api.http.api;

import com.sun.net.httpserver.HttpServer;
import jio.IO;
import jio.http.client.MyHttpClientBuilder;
import jio.http.client.oauth.AccessTokenRequest;
import jio.http.client.oauth.ClientCredentialsHttpClient;
import jio.http.client.oauth.ClientCredentialsHttpClientBuilder;
import jio.http.client.oauth.GetAccessToken;
import jio.http.server.HttpServerBuilder;
import jio.test.junit.JioDebugger;
import jio.test.junit.DebuggerDuration;
import jio.test.stub.httpserver.GetStub;
import jio.test.stub.httpserver.PostStub;
import jio.test.stub.httpserver.StatusCodeStub;
import jsonvalues.JsObj;
import jsonvalues.JsStr;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@ExtendWith(JioDebugger.class)
@DebuggerDuration(millis = 1000)
public class TestOauth {


    IO<HttpServer> io = new HttpServerBuilder()
            .addContext("/token", PostStub.of(n -> body -> uri -> headers -> JsObj.of("access_token", JsStr.of(n + "")).toString(),
                                              StatusCodeStub.cons(200)
                                             )
                       )
            .addContext("/service", GetStub.of(n -> body -> uri -> headers -> n == 2 ? "" : n + "",
                                               n -> body -> uri -> headers -> n == 2 ? 401 : 200
                                              ))
            .start(7777);


    @Test
    public void test() {

        HttpServer server = io.join();

        System.out.println(server.getAddress().getPort());

        System.out.println(server.getAddress().getHostName());


        ClientCredentialsHttpClientBuilder builder =
                new ClientCredentialsHttpClientBuilder(new MyHttpClientBuilder(HttpClient.newHttpClient())
                                                               .create(),
                                                       new AccessTokenRequest("client_id",
                                                                              "client_secret",
                                                                              "localhost",
                                                                              7777,
                                                                              "token",
                                                                              false
                                                       ),
                                                       GetAccessToken.DEFAULT,
                                                       resp -> resp.statusCode() == 401

                );

        ClientCredentialsHttpClient client = builder.create();

        HttpResponse<String> resp = client.oauthOfString()
                                          .apply(HttpRequest.newBuilder()
                                                            .GET()
                                                            .uri(URI.create("http://localhost:7777/service"))
                                                ).join();


        System.out.println(resp.body());

    }

}
