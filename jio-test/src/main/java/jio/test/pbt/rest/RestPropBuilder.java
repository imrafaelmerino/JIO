package jio.test.pbt.rest;

import fun.gen.Gen;
import jio.BiLambda;
import jio.IO;
import jio.Lambda;
import jio.test.pbt.Property;
import jio.test.pbt.TestFailure;
import jio.test.pbt.TestResult;
import jsonvalues.*;

import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

abstract class RestPropBuilder<O, A extends RestPropBuilder<O, A>> {

    final static Function<HttpResponse<String>, TestResult> respAssert =
            resp -> resp.statusCode() < 300 ?
                    TestResult.SUCCESS :
                    TestFailure.reason("Expected status code < 300, but got a " + resp.statusCode());
    final String name;
    final BiLambda<JsObj, O, HttpResponse<String>> post;
    final BiLambda<JsObj, String, HttpResponse<String>> get;
    final BiLambda<JsObj, String, HttpResponse<String>> delete;
    final Gen<O> gen;
    private final Function<JsPath, BiFunction<O, HttpResponse<String>, IO<String>>> getIdFromPath =
            path -> (body, resp) -> {
                try {
                    JsObj respBody = JsObj.parse(resp.body());
                    JsValue id = respBody.get(path);
                    return id == JsNothing.NOTHING ?
                            IO.fail(TestFailure.reason(path + " not found in the following json: " + resp.body())) :
                            IO.succeed(id.toString());
                } catch (JsParserException e) {
                    return IO.fail(TestFailure.reason("resp body is not a Json well-formed: " + resp.body()));
                }
            };
    Function<HttpResponse<String>, TestResult> postAssert = respAssert;
    Function<HttpResponse<String>, TestResult> getAssert = respAssert;
    Function<HttpResponse<String>, TestResult> deleteAssert = respAssert;
    BiFunction<O, HttpResponse<String>, IO<String>> getId;
    ;

    public RestPropBuilder(String name,
                           Gen<O> gen,
                           Lambda<O, HttpResponse<String>> p_post,
                           Lambda<String, HttpResponse<String>> p_get,
                           Lambda<String, HttpResponse<String>> p_delete
                          ) {
        this(name,
             gen,
             (conf, body) -> Objects.requireNonNull(p_post).apply(body),
             (conf, id) -> Objects.requireNonNull(p_get).apply(id),
             (conf, id) -> Objects.requireNonNull(p_delete).apply(id));

    }

    public RestPropBuilder(String name,
                           Gen<O> gen,
                           BiLambda<JsObj, O, HttpResponse<String>> p_post,
                           BiLambda<JsObj, String, HttpResponse<String>> p_get,
                           BiLambda<JsObj, String, HttpResponse<String>> p_delete
                          ) {
        this.post = Objects.requireNonNull(p_post);
        this.get = Objects.requireNonNull(p_get);
        this.delete = Objects.requireNonNull(p_delete);
        this.name = Objects.requireNonNull(name);
        this.gen = Objects.requireNonNull(gen);
        this.getId = getIdFromPath.apply(JsPath.fromKey("id"));
    }

    public A withPostAssert(Function<HttpResponse<String>, TestResult> postAssert) {
        this.postAssert = Objects.requireNonNull(postAssert);
        return (A) this;
    }

    public A withGetAssert(Function<HttpResponse<String>, TestResult> getAssert) {
        this.getAssert = Objects.requireNonNull(getAssert);
        return (A) this;
    }

    public A withDeleteAssert(Function<HttpResponse<String>, TestResult> deleteAssert) {
        this.deleteAssert = Objects.requireNonNull(deleteAssert);
        return (A) this;
    }

    public A withGetId(BiFunction<O, HttpResponse<String>, IO<String>> getId) {
        this.getId = Objects.requireNonNull(getId);
        return (A) this;
    }


    public A withGetIdFromRespPath(final JsPath path) {
        this.getId = getIdFromPath.apply(Objects.requireNonNull(path));
        return (A) this;
    }

    public A withGetIdFromReqBody(final Function<O, String> p_getId) {
        Objects.requireNonNull(p_getId);
        this.getId = (body, resp) -> {
            String id = p_getId.apply(body);
            return id == null || id.isBlank() || id.isEmpty() ?
                    IO.fail(TestFailure.reason("id not found")) :
                    IO.succeed(id);
        };
        return (A) this;
    }

    public abstract Property<O> create();
}
