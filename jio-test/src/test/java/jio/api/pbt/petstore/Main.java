package jio.api.pbt.petstore;

import fun.gen.Combinators;
import jio.BiLambda;
import jio.http.client.MyHttpClient;
import jio.http.client.MyHttpClientBuilder;
import jio.pbt.*;
import jsonvalues.JsNull;
import jsonvalues.JsObj;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class Main {

    static MyHttpClient client = new MyHttpClientBuilder(HttpClient.newBuilder().build()).create();

    static Predicate<HttpResponse<String>> is2XX =
            resp -> {

                return resp.statusCode() < 300
                        && Specs.apiResponseSpec.test(JsObj.parse(resp.body())).isEmpty();
            };

    static Predicate<HttpResponse<String>> is400 =
            resp -> {

                return resp.statusCode() == 400
                        && Specs.apiResponseSpec.test(JsObj.parse(resp.body())).isEmpty();
            };

    static Function<HttpResponse<String>, TestResult> assertResp(Predicate<HttpResponse<String>> predicate,
                                                                 String failureMessage) {
        return resp -> predicate.test(resp) ?
                TestSuccess.SUCCESS :
                TestFailure.reason(failureMessage + "." + "Response: " + resp.statusCode() + ", " + resp.body());
    }

    static BiLambda<JsObj, JsObj, HttpResponse<String>> post(String entity) {
        return (conf, body) -> client
                .ofString()
                .apply(HttpRequest
                        .newBuilder()
                        .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                        .uri(URI.create("https://petstore.swagger.io/v2/" + entity))
                        .header("Content-Type", "application/json")
                );
    }


    static BiLambda<JsObj, String, HttpResponse<String>> get(String entity) {

        return (conf, id) -> client
                .ofString()
                .apply(HttpRequest
                        .newBuilder()
                        .GET()
                        .uri(URI.create("https://petstore.swagger.io/v2/" + entity + "/" + id))
                );
    }

    static BiLambda<JsObj, String, HttpResponse<String>> delete(String entity) {

        return (conf, id) -> client
                .ofString()
                .apply(HttpRequest
                        .newBuilder()
                        .DELETE()
                        .uri(URI.create("https://petstore.swagger.io/v2/" + entity + "/" + id))
                );
    }

    public static void main(String[] args) {

        Property<JsObj> petIsInserted = Property.<JsObj>ofLambda(
                        "post_pet",
                        (conf, body) -> post("pet").apply(conf, body).map(resp -> assertResp(is400, "2XX status code was expected").apply(resp))

                )
                .withGen(Generators.petGen)
                .times(100);

        Property<JsObj> invalidPetIsNotInserted = Property.<JsObj>ofLambda(
                        "post_pet_missing_req_key",
                        (conf, body) -> post("pet").apply(conf, body).map(resp -> assertResp(is400, "4XX status code was expected").apply(resp))

                )
                .withGen(Combinators
                        .oneOf(Fields.REQ_PET_FIELDS)
                        .then(reqKey ->
                                Generators.petGen.map(
                                        json -> json.delete(reqKey)
                                )
                        )
                )
                .times(100);

        Property<JsObj> petWithNullIsNotInserted = Property.<JsObj>ofLambda(
                        "post_pet_with_null_value_in_req_key",
                        (conf, body) -> post("pet").apply(conf, body).map(resp -> assertResp(is400, "4XX status code was expected").apply(resp))
                )
                .withGen(Combinators
                        .oneOf(Fields.REQ_PET_FIELDS)
                        .then(reqKey ->
                                Generators.petGen.map(
                                        json -> json.set(reqKey, JsNull.NULL)
                                )
                        )
                )
                .times(100);


        Property<JsObj> crudPetFlow = new CRUDPropBuilder<>(
                "pet_crud",
                post("pet"),
                get("pet"),
                delete("pet")).create().withGen(Generators.petGen).repeatPar();

        //   System.out.println(crudPetFlow.withGen(Generators.petGen).times(20).repeatPar(2));

        Property<JsObj> userPetFlow = new CRUDPropBuilder<>(
                "user_crud",
                post("user"),
                get("user"),
                delete("user")
        )
                .withBodyPostGetId(body -> body.getStr("username"))
                .create()
                .withGen(Generators.userGen);

        Group.par("petstore",List.of(crudPetFlow,userPetFlow)).apply().forEach(System.out::println);



    }


}
