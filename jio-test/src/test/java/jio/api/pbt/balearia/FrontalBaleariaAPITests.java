package jio.api.pbt.balearia;

import fun.gen.Combinators;
import jio.Lambda;
import jio.http.client.MyHttpClient;
import jio.http.client.MyHttpClientBuilder;
import jio.pbt.*;
import jsonvalues.JsNull;
import jsonvalues.JsObj;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Function;

public class FrontalBaleariaAPITests {
    static MyHttpClient client;

    private static Lambda<JsObj, TestResult> req(Function<HttpResponse<String>, TestResult> fn) {

        return body -> client
                .ofString()
                .apply(HttpRequest
                        .newBuilder()
                        .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                        .uri(URI.create("https://dev.api.balearia.com/master-data/ships"))
                        .header("x-apikey", "zvNSOgxhJ7JC1E3dsNFr87QdsKmaVCg1oALf2nsikLqAbDCC")
                        .header("Content-Type", "application/json")
                ).map(fn);
    }

    @BeforeAll
    public static void init() {


        client = new MyHttpClientBuilder(HttpClient.newBuilder().build()).create();


    }

    @Test
    public void POST_Ship_Without_All_Required_Fields_Returns_400() {

        var invalidShipGen = Combinators
                .oneOf(Fields.REQ_SHIPS_FIELDS)
                .then(field -> Generators.VALID_SHIPS_GENERATOR.map(json -> json.delete(field)));

        Report report = Property.ofLambda(
                        "required_field_missing",
                        req(resp -> resp.statusCode() == 400 ?
                                TestSuccess.SUCCESS :
                                TestFailure.reason(resp.statusCode() + ": " + resp.body())
                        )
                )
                .withGen(invalidShipGen)
                .times(10)
                .repeatPar(1);


        System.out.println(report);
    }

    @Test
    public void POST_Ship_With_Null_In_Required_Field_Returns_400() {

        var invalidShipGen = Combinators.oneOf(Fields.REQ_SHIPS_FIELDS)
                .then(field -> Generators.VALID_SHIPS_GENERATOR.map(json -> json.set(field, JsNull.NULL)));

        Report report = Property.ofLambda(
                        "required_field_null",
                        req(resp -> resp.statusCode() == 400 ?
                                TestSuccess.SUCCESS :
                                TestFailure.reason(resp.statusCode() + ": " + resp.body())
                        )
                )
                .withGen(invalidShipGen)
                .times(10)
                .repeatPar(1);


        System.out.println(report);
    }
}
