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

/**
 * A builder class for creating property tests that cover CRUD (Create, Read, Update, Delete) operations on a RESTful API endpoint.
 *
 * @param <O> The type of data generated to feed the property tests.
 */
public final class CRUDPropBuilder<O> extends RestPropBuilder<O, CRUDPropBuilder<O>> {


    private final BiLambda<JsObj, HttpResponse<String>, HttpResponse<String>> update;

    private Function<HttpResponse<String>, TestResult> updateAssert = respAssert;
    /**
     * Creates a new instance of CRUDPropBuilder with the specified parameters.
     *
     * @param name    The name of the property test.
     * @param gen     The data generator that produces pseudorandom data for testing.
     * @param p_post  The lambda function representing the HTTP POST operation.
     * @param p_get   The lambda function representing the HTTP GET operation.
     * @param p_update The lambda function representing the HTTP UPDATE operation.
     * @param p_delete The lambda function representing the HTTP DELETE operation.
     */
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
    /**
     * Creates a new instance of CRUDPropBuilder with the specified parameters.
     *
     * @param name    The name of the property test.
     * @param gen     The data generator that produces pseudorandom data for testing.
     * @param p_post  The lambda function representing the HTTP POST operation.
     * @param p_get   The lambda function representing the HTTP GET operation.
     * @param p_update The lambda function representing the HTTP UPDATE operation.
     * @param p_delete The lambda function representing the HTTP DELETE operation.
     */

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

    /**
     * Creates a property test based on the configuration of this builder.
     *
     * @return A property test for the CRUD operations defined by this builder.
     */
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

    /**
     * Sets the assertion function for the HTTP UPDATE operation.
     *
     * @param updateAssert The assertion function for the HTTP UPDATE operation.
     * @return This CRUDPropBuilder instance with the updated assertion function.
     */
    public CRUDPropBuilder<O> withUpdateAssert(Function<HttpResponse<String>, TestResult> updateAssert) {
        this.updateAssert = Objects.requireNonNull(updateAssert);
        return this;
    }
}