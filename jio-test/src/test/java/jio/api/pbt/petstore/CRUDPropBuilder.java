package jio.api.pbt.petstore;

import fun.tuple.Pair;
import jio.BiLambda;
import jio.IO;
import jio.pbt.*;
import jsonvalues.*;

import java.net.http.HttpResponse;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class CRUDPropBuilder<O> {

    private final BiLambda<JsObj, O, HttpResponse<String>> post;
    private final BiLambda<JsObj, String, HttpResponse<String>> get;
    private final BiLambda<JsObj, String, HttpResponse<String>> delete;
    private final String name;
    private final Function<HttpResponse<String>, TestResult> postAssert =
            resp -> resp.statusCode() < 300 ?
                    TestResult.SUCCESS :
                    TestFailure.reason("Expected status code < 300, but got a " + resp.statusCode());
    private final Function<HttpResponse<String>, TestResult> getAssert =
            resp -> resp.statusCode() < 300 ?
                    TestResult.SUCCESS :
                    TestFailure.reason("Expected status code < 300, but got a " + resp.statusCode());
    private final Function<HttpResponse<String>, TestResult> deleteAssert =
            resp -> resp.statusCode() < 300 ?
                    TestResult.SUCCESS :
                    TestFailure.reason("Expected status code < 300, but got a " + resp.statusCode());

    private Function<HttpResponse<String>, String> logger = resp ->
            String.format(
                    "%s %s -> %s %s",
                    resp.request().method(),
                    resp.request().uri(),
                    resp.statusCode(),
                    (resp.body() == null || resp.body().isEmpty() ? "Empty body!" : resp.body())
            );

    private Consumer<String> appender = System.out::println;



    private boolean isDebug = true;
    private BiFunction<O, HttpResponse<String>, IO<String>> getId;

    public CRUDPropBuilder(String name,
                           BiLambda<JsObj, O, HttpResponse<String>> post,
                           BiLambda<JsObj, String, HttpResponse<String>> get,
                           BiLambda<JsObj, String, HttpResponse<String>> delete
    ) {
        this.post = post;
        this.get = get;
        this.delete = delete;
        this.name = name;
    }
    public CRUDPropBuilder<O> withAppender(Consumer<String> appender) {
        this.appender = appender;
        return this;
    }

    public CRUDPropBuilder<O> withLogger(Function<HttpResponse<String>, String> logger) {
        this.logger = logger;
        return this;
    }

    public CRUDPropBuilder<O> enableDebug(){
        this.isDebug = true;
        return this;
    }

    public CRUDPropBuilder<O> disableDebug(){
        this.isDebug = false;
        return this;
    }

    public CRUDPropBuilder<O> withPostRespIdPath(final JsPath path) {
        this.getId = (body, resp) -> {
            try {
                JsObj respBody = JsObj.parse(resp.body());
                JsValue id = respBody.get(path);
                return id == JsNothing.NOTHING ?
                        IO.failure(TestFailure.reason(name + " not found in the following json: " + resp.body())) :
                        IO.value(id.toString());
            } catch (JsParserException e) {
                return IO.failure(TestFailure.reason("resp body is not a Json well-formed: " + resp.body()));
            }
        };
        return this;
    }

    public CRUDPropBuilder<O> withBodyPostGetId(final Function<O, String> getId) {
        this.getId = (body, resp) -> {
            String id = getId.apply(body);
            return id == null || id.isBlank() || id.isEmpty() ?
                    IO.failure(TestFailure.reason("id not found")) :
                    IO.value(id);
        };
        return this;
    }


    @SuppressWarnings("ReturnValueIgnored")
    public Property<O> create() {
        BiLambda<JsObj, O, TestResult> lambda = (conf, body) ->
                post.apply(conf, body)
                        .peekSuccess(resp -> {
                            if (isDebug) appender.accept(logger.apply(resp));
                        })
                        .then(resp -> switch (postAssert.apply(resp)) {
                            case TestException exc -> IO.failure(exc);
                            case TestFailure failure -> IO.failure(failure);
                            case TestSuccess $ ->
                                    getId.apply(body, resp).then(id -> get.apply(conf, id).map(r -> Pair.of(id, r)));


                        })
                        .peekSuccess(pair -> {
                            if (isDebug) appender.accept(logger.apply(pair.second()));
                        }).

                        then(pair -> switch (getAssert.apply(pair.second())) {

                            case TestException exc -> IO.failure(exc);
                            case TestFailure failure -> IO.failure(failure);
                            case TestSuccess $ -> delete.apply(conf, pair.first()).map(r -> Pair.of(pair.first(), r));
                        }).peekSuccess(pair -> {
                            if (isDebug) appender.accept(logger.apply(pair.second()));
                        })
                        .then(pair -> switch (deleteAssert.apply(pair.second())) {
                            case TestException exc -> IO.failure(exc);
                            case TestFailure failure -> IO.failure(failure);
                            case TestSuccess $ -> get.apply(conf, pair.first());
                        })
                        .peekSuccess(resp -> {
                            if (isDebug) appender.accept(logger.apply(resp));
                        })
                        .map(resp -> resp.statusCode() == 404 ? TestResult.SUCCESS : TestFailure.reason("Entity found after being deleted successfully.Status code received " + resp.statusCode()));

        return Property.ofLambda(name, lambda);
    }
}