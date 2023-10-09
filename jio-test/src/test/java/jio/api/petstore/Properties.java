package jio.api.petstore;

import fun.gen.Combinators;
import jio.BiLambda;
import jio.http.client.MyHttpClient;
import jio.http.client.MyHttpClientBuilder;
import jio.test.junit.Debugger;
import jio.test.pbt.*;
import jio.test.pbt.rest.CRDPropBuilder;
import jsonvalues.JsNull;
import jsonvalues.JsObj;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

@Disabled
public class Properties {

    @RegisterExtension
    static Debugger debugger = new Debugger(Duration.ofSeconds(2));

    static MyHttpClient client = new MyHttpClientBuilder(HttpClient.newBuilder().build()).create();

    static Predicate<HttpResponse<String>> is2XX =
            resp -> resp.statusCode() < 300
                    && Specs.apiResponseSpec.test(JsObj.parse(resp.body())).isEmpty();

    static Predicate<HttpResponse<String>> is400 =
            resp -> resp.statusCode() == 400
                    && Specs.apiResponseSpec.test(JsObj.parse(resp.body())).isEmpty();

    static Function<HttpResponse<String>, TestResult> assertResp(Predicate<HttpResponse<String>> predicate,
                                                                 String failureMessage
                                                                ) {
        return resp -> predicate.test(resp) ?
                TestSuccess.SUCCESS :
                TestFailure.reason(failureMessage + "." + "Response: " + resp.statusCode() + ", " + resp.body());
    }

    static BiLambda<JsObj, JsObj, HttpResponse<String>> post(String entity) {
        return (conf, body) -> client
                .ofString()
                .apply(HttpRequest.newBuilder()
                                  .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                                  .uri(URI.create("https://petstore.swagger.io/v2/" + entity))
                                  .header("Content-Type", "application/json")
                      );
    }


    static BiLambda<JsObj, String, HttpResponse<String>> get(String entity) {

        return (conf, id) -> client
                .ofString()
                .apply(HttpRequest.newBuilder()
                                  .GET()
                                  .uri(URI.create("https://petstore.swagger.io/v2/" + entity + "/" + id))
                      );
    }

    static BiLambda<JsObj, String, HttpResponse<String>> delete(String entity) {
        return (conf, id) -> client.ofString()
                                   .apply(HttpRequest.newBuilder()
                                                     .DELETE()
                                                     .uri(URI.create("https://petstore.swagger.io/v2/" + entity + "/" + id))
                                         );
    }

    @Test
    public void pet_missing_field_not_inserted() {
        Property<JsObj> invalidPetIsNotInserted =
                Property.ofLambda("post_pet_missing_req_key",
                                  Combinators.oneOf(Fields.REQ_PET_FIELDS)
                                             .then(reqKey -> Generators.petGen.map(json -> json.delete(reqKey))),
                                  (conf, body) -> post("pet").apply(conf, body)
                                                             .map(resp -> assertResp(is400, "4XX status code was expected").apply(resp))

                                 )
                        .withTimes(100);
    }

    @Test
    public void pet_null_field_not_inserted() {
        Property<JsObj> petWithNullIsNotInserted =
                Property.ofLambda("post_pet_with_null_value_in_req_key",
                                  Combinators.oneOf(Fields.REQ_PET_FIELDS)
                                             .then(reqKey -> Generators.petGen.map(json -> json.set(reqKey, JsNull.NULL))),
                                  (conf, body) -> post("pet").apply(conf, body)
                                                             .map(resp -> assertResp(is400, "4XX status code was expected").apply(resp))
                                 )
                        .withTimes(100);

    }

    @Test
    public void crud_pet_and_users_is_ok() {
        Property<JsObj> crudPetFlow =
                new CRDPropBuilder<>("pet_crud",
                                     Generators.petGen,
                                     post("pet"),
                                     get("pet"),
                                     delete("pet"))
                        .create()
                        .withTimes(2);

        Property<JsObj> userPetFlow =
                new CRDPropBuilder<>("user_crud",
                                     Generators.userGen,
                                     post("user"),
                                     get("user"),
                                     delete("user"))
                        .withGetIdFromReqBody(body -> body.getStr("username"))
                        .create()
                        .withTimes(2);

        var report = Group.of("petstore",
                              List.of(crudPetFlow, userPetFlow)
                             )
                          .par()
                          .result();

        report.assertAllSuccess();
    }

}
