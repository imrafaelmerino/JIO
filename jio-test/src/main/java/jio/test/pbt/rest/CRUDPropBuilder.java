package jio.test.pbt.rest;

import fun.gen.Gen;
import fun.tuple.Pair;
import jio.BiLambda;
import jio.IO;
import jio.Lambda;
import jio.test.pbt.*;
import jsonvalues.*;

import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.function.Function;

public final class CRUDPropBuilder<O> extends RestPropBuilder<O, CRUDPropBuilder<O>> {


    private final BiLambda<JsObj, HttpResponse<String>, HttpResponse<String>> update;

    private Function<HttpResponse<String>, TestResult> updateAssert = respAssert;

    public CRUDPropBuilder(String name,
                           Gen<O> gen,
                           Lambda<O, HttpResponse<String>> p_post,
                           Lambda<String, HttpResponse<String>> p_get,
                           Lambda<HttpResponse<String>, HttpResponse<String>> p_update,
                           Lambda<String, HttpResponse<String>> p_delete
                          ) {
        super(name, gen, p_post, p_get, p_delete);
        this.update = (conf, res) -> p_update.apply(res);
    }


    public CRUDPropBuilder(String name,
                           Gen<O> gen,
                           BiLambda<JsObj, O, HttpResponse<String>> p_post,
                           BiLambda<JsObj, String, HttpResponse<String>> p_get,
                           BiLambda<JsObj, HttpResponse<String>, HttpResponse<String>> p_update,
                           BiLambda<JsObj, String, HttpResponse<String>> p_delete
                          ) {
        super(name, gen, p_post, p_get, p_delete);
        this.update = p_update;
    }


    public Property<O> create() {
        BiLambda<JsObj, O, TestResult> lambda =
                (conf, body) -> post.apply(conf, body)
                                    .then(resp -> switch (postAssert.apply(resp)) {
                                        case TestException exc -> IO.fail(exc);
                                        case TestFailure failure -> IO.fail(failure);
                                        case TestSuccess $ -> getId.apply(body, resp)
                                                                   .then(id -> get.apply(conf, id)
                                                                                  .map(r -> Pair.of(id, r)));
                                    })
                                    .then(pair -> switch (getAssert.apply(pair.second())) {
                                        case TestException exc -> IO.fail(exc);
                                        case TestFailure failure -> IO.fail(failure);
                                        case TestSuccess $ -> update.apply(conf, pair.second())
                                                                    .map(r -> Pair.of(pair.first(), r));
                                    })
                                    .then(pair -> switch (updateAssert.apply(pair.second())) {
                                        case TestException exc -> IO.fail(exc);
                                        case TestFailure failure -> IO.fail(failure);
                                        case TestSuccess $ -> delete.apply(conf, pair.first())
                                                                    .map(r -> Pair.of(pair.first(), r));
                                    })
                                    .then(pair -> switch (deleteAssert.apply(pair.second())) {
                                        case TestException exc -> IO.fail(exc);
                                        case TestFailure failure -> IO.fail(failure);
                                        case TestSuccess $ -> get.apply(conf, pair.first());
                                    })
                                    .map(resp -> resp.statusCode() == 404 ?
                                            TestResult.SUCCESS :
                                            TestFailure.reason("Entity found after being deleted successfully.Status code received " + resp.statusCode()));

        return Property.ofLambda(name, gen, lambda);
    }


    public CRUDPropBuilder<O> withUpdateAssert(Function<HttpResponse<String>, TestResult> updateAssert) {
        this.updateAssert = Objects.requireNonNull(updateAssert);
        return this;
    }
}