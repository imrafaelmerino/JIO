package jio.api.petstore;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import fun.gen.Combinators;
import fun.gen.Gen;
import fun.gen.IntGen;
import fun.tuple.Pair;
import jio.BiLambda;
import jio.IO;
import jio.ListExp;
import jio.RetryPolicies;
import jio.http.client.JioHttpClientBuilder;
import jio.http.client.oauth.AccessTokenRequest;
import jio.http.client.oauth.ClientCredsBuilder;
import jio.http.client.oauth.GetAccessToken;
import jio.http.client.oauth.OauthHttpClient;
import jio.http.server.HttpServerBuilder;
import jio.test.junit.Debugger;
import jio.test.pbt.*;
import jio.test.pbt.rest.CRDPropBuilder;
import jio.test.stub.httpserver.BodyStub;
import jio.test.stub.httpserver.GetStub;
import jio.test.stub.httpserver.PostStub;
import jio.test.stub.httpserver.StatusCodeStub;
import jsonvalues.JsNull;
import jsonvalues.JsObj;
import jsonvalues.gen.JsObjGen;
import jsonvalues.gen.JsStrGen;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static jio.http.client.HttpExceptions.CONNECTION_TIMEOUT;
import static jio.http.client.HttpExceptions.NETWORK_UNREACHABLE;

@Disabled
public class Properties {

    @RegisterExtension
    static Debugger debugger = Debugger.of(Duration.ofSeconds(2));

    static JioHttpClientBuilder myHttpClientBuilder =
            JioHttpClientBuilder.of(HttpClient.newBuilder()
                                              .connectTimeout(Duration.ofMillis(300)))
                                .withRetryPolicy(RetryPolicies.incrementalDelay(Duration.ofMillis(10))
                                                              .append(RetryPolicies.limitRetries(5)))
                                .withRetryPredicate(CONNECTION_TIMEOUT.or(NETWORK_UNREACHABLE));

    static HttpServer server =
            HttpServerBuilder.of(Map.of(
                                     "/token", PostStub.of(BodyStub.gen(JsObjGen.of("access_token", JsStrGen.alphanumeric(10, 10)).map(JsObj::toString)),
                                                           StatusCodeStub.cons(200))
                                     ,
                                     "/thanks", GetStub.of(BodyStub.cons("your welcome!"),
                                                           StatusCodeStub.gen(Combinators.freq(Pair.of(5, IntGen.arbitrary(200, 299)),
                                                                                               Pair.of(1, Gen.cons(401))))
                                                          )
                                       )
                                )
                             .startAtRandom(8000, 9000);

    private static final int port = server.getAddress().getPort();
    static OauthHttpClient oauthClient =
            ClientCredsBuilder.of(myHttpClientBuilder,
                                  AccessTokenRequest.of("client_id",
                                                        "client_secret",
                                                        URI.create("http://localhost:%d/token".formatted(port))),
                                  GetAccessToken.DEFAULT,
                                  resp -> resp.statusCode() == 401)
                              .get();
    static Predicate<HttpResponse<String>> is2XX =
            resp -> resp.statusCode() < 300
                    && Specs.apiResponseSpec.test(JsObj.parse(resp.body())).isEmpty();
    static Predicate<HttpResponse<String>> is400 =
            resp -> resp.statusCode() == 400
                    && Specs.apiResponseSpec.test(JsObj.parse(resp.body())).isEmpty();

    static {
        HttpHandler accessToken = PostStub.of(BodyStub.gen(JsObjGen.of("access_token", JsStrGen.alphanumeric(10, 10)).map(JsObj::toString)),
                                              StatusCodeStub.cons(200));
        HttpHandler handler = GetStub.of(BodyStub.cons("your welcome!"),
                                         StatusCodeStub.gen(Combinators.freq(Pair.of(5, IntGen.arbitrary(200, 299)),
                                                                             Pair.of(1, Gen.cons(401))))
                                        );

    }

    static Function<HttpResponse<String>, TestResult> assertResp(Predicate<HttpResponse<String>> predicate,
                                                                 String failureMessage
                                                                ) {
        return resp -> predicate.test(resp) ?
                TestSuccess.SUCCESS :
                TestFailure.reason(failureMessage + "." + "Response: " + resp.statusCode() + ", " + resp.body());
    }

    static BiLambda<JsObj, JsObj, HttpResponse<String>> post(String entity) {
        return (conf, body) -> oauthClient
                .ofString()
                .apply(HttpRequest.newBuilder()
                                  .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                                  .uri(URI.create("https://petstore.swagger.io/v2/" + entity))
                                  .header("Content-Type", "application/json")
                      );
    }


    static BiLambda<JsObj, String, HttpResponse<String>> get(String entity) {

        return (conf, id) -> oauthClient
                .ofString()
                .apply(HttpRequest.newBuilder()
                                  .GET()
                                  .uri(URI.create("https://petstore.swagger.io/v2/" + entity + "/" + id))
                      );
    }

    static BiLambda<JsObj, String, HttpResponse<String>> delete(String entity) {
        return (conf, id) -> oauthClient.ofString()
                                        .apply(HttpRequest.newBuilder()
                                                          .DELETE()
                                                          .uri(URI.create("https://petstore.swagger.io/v2/" + entity + "/" + id))
                                              );
    }

    public static void main(String[] args) {
        BiFunction<String, String, HttpRequest.Builder> GET =
                (entity, id) -> HttpRequest.newBuilder()
                                           .GET()
                                           .uri(URI.create("https://petstore.swagger.io/v2/%s/%s".formatted(entity,
                                                                                                            id)));

        IO<HttpResponse<String>> getPet = oauthClient.ofString().apply(GET.apply("pet", "1"));
        IO<HttpResponse<String>> getOrder = oauthClient.ofString().apply(GET.apply("store/order", "1"));

        List<Integer> status = ListExp.par(getPet, getOrder)
                                      .map(responses -> responses.stream()
                                                                 .map(HttpResponse::statusCode)
                                                                 .toList()
                                          )
                                      .result();


    }

    @Test
    public void pet_missing_field_not_inserted() {
        Property<JsObj> unused =
                PropBuilder.ofLambda("post_pet_missing_req_key",
                                     Combinators.oneOf(Fields.REQ_PET_FIELDS)
                                                .then(reqKey -> Generators.petGen.map(json -> json.delete(reqKey))),
                                     (conf, body) -> post("pet").apply(conf, body)
                                                                .map(resp -> assertResp(is400, "4XX status code was expected").apply(resp))

                                    )
                           .withTimes(100)
                           .get();
    }

    @Test
    public void pet_null_field_not_inserted() {
        Property<JsObj> unused =
                PropBuilder.ofLambda("post_pet_with_null_value_in_req_key",
                                     Combinators.oneOf(Fields.REQ_PET_FIELDS)
                                                .then(reqKey -> Generators.petGen.map(json -> json.set(reqKey, JsNull.NULL))),
                                     (conf, body) -> post("pet").apply(conf, body)
                                                                .map(resp -> assertResp(is400, "4XX status code was expected").apply(resp))
                                    )
                           .withTimes(100)
                           .get();

    }

    @Test
    public void crud_pet_and_users_is_ok() {
        Property<JsObj> crudPetFlow =
                CRDPropBuilder.of("pet_crud",
                                  Generators.petGen,
                                  post("pet"),
                                  get("pet"),
                                  delete("pet"))
                              .get()
                              .get();

        Property<JsObj> userPetFlow =
                CRDPropBuilder.of("user_crud",
                                  Generators.userGen,
                                  post("user"),
                                  get("user"),
                                  delete("user"))
                              .withGetIdFromReqBody(body -> body.getStr("username"))
                              .get()
                              .get();

        var report = Group.of("petstore",
                              List.of(crudPetFlow, userPetFlow)
                             )
                          .par()
                          .result();

        report.assertAllSuccess();
    }

    @Test
    public void test() {
        BiFunction<String, String, HttpRequest.Builder> GET =
                (entity, id) -> HttpRequest.newBuilder()
                                           .GET()
                                           .uri(URI.create("https://petstore.swagger.io/v2/%s/%s".formatted(entity,
                                                                                                            id
                                                                                                           )
                                                          )
                                               );

        IO<HttpResponse<String>> getPet = oauthClient.ofString().apply(GET.apply("pet", "1"));
        IO<HttpResponse<String>> getOrder = oauthClient.ofString().apply(GET.apply("store/order", "1"));

        List<Integer> status = ListExp.par(getPet, getOrder)
                                      .map(responses -> responses.stream()
                                                                 .map(HttpResponse::statusCode)
                                                                 .toList()
                                          )
                                      .result();

        System.out.println(status);
    }

    @Test
    public void testOuth() {
        oauthClient.oauthOfString()
                   .apply(HttpRequest.newBuilder().GET().uri(URI.create("http://localhost:%s/thanks".formatted(port))))
                   .repeat(resp -> true, RetryPolicies.limitRetries(10))
                   .result();
    }

}
