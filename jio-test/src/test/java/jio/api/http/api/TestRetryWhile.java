package jio.api.http.api;

import com.sun.net.httpserver.HttpServer;
import jio.IO;
import jio.http.client.MyHttpClient;
import jio.http.client.MyHttpClientBuilder;
import jio.http.server.HttpServerBuilder;
import jio.test.junit.Debugger;
import jio.test.junit.DebugExp;
import jio.test.stub.httpserver.GetStub;
import jio.test.stub.httpserver.HeadersStub;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.http.HttpClient;

@ExtendWith(Debugger.class)
@DebugExp(duration = 1000)
public class TestRetryWhile {

    static int port;
    static MyHttpClient httpClient;

    @BeforeAll
    public static void prepare() {

        GetStub getStrReqHandler = GetStub.of(n -> bodyReq -> uri -> headers ->
                                                      n <= 3 ?
                                                              "not found" :
                                                              "success",
                                              n -> bodyReq -> uri -> headers ->
                                                      n <= 3 ?
                                                              404 :
                                                              200,
                                              HeadersStub.EMPTY
                                             );

        HttpServerBuilder builder =
                new HttpServerBuilder().addContext("/get_str",
                                                   getStrReqHandler
                                                  );

        IO<HttpServer> server = builder.startAtRandom("localhost",
                                                      8000,
                                                      9000
                                                     );

        port = server.result()
                     .getAddress()
                     .getPort();

        httpClient = new MyHttpClientBuilder(HttpClient.newBuilder()
                                                       .build()).create();

    }

}
