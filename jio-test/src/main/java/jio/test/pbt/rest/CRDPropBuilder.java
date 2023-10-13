package jio.test.pbt.rest;

import fun.gen.Gen;
import fun.tuple.Pair;
import jio.BiLambda;
import jio.IO;
import jio.Lambda;
import jio.test.pbt.Property;
import jio.test.pbt.TestException;
import jio.test.pbt.TestFailure;
import jio.test.pbt.TestResult;
import jsonvalues.JsObj;

import java.net.http.HttpResponse;
import java.util.Objects;

/**
 * A builder class for creating property tests for RESTful APIs that support Create (POST), Read (GET), and Delete
 * (DELETE) operations.
 *
 * @param <O> The type of data generated to feed the property tests.
 */
public final class CRDPropBuilder<O> extends RestPropBuilder<O, CRDPropBuilder<O>> {

    /**
     * Creates a new instance of CRDPropBuilder for building property tests for CRUD operations.
     *
     * @param name     The name of the property test.
     * @param gen      A generator for producing input data of type O.
     * @param p_post   A function for making HTTP POST requests to create entities.
     * @param p_get    A function for making HTTP GET requests to retrieve entities by ID.
     * @param p_delete A function for making HTTP DELETE requests to delete entities by ID.
     */
    public CRDPropBuilder(String name,
                          Gen<O> gen,
                          Lambda<O, HttpResponse<String>> p_post,
                          Lambda<String, HttpResponse<String>> p_get,
                          Lambda<String, HttpResponse<String>> p_delete
                         ) {
        super(name, gen, p_post, p_get, p_delete);
    }

    /**
     * Creates a new instance of CRDPropBuilder for building property tests for CRUD operations.
     *
     * @param name     The name of the property test.
     * @param gen      A generator for producing input data of type O.
     * @param p_post   A bi-function for making HTTP POST requests to create entities, taking a configuration and input
     *                 data.
     * @param p_get    A bi-function for making HTTP GET requests to retrieve entities by ID, taking a configuration and
     *                 an ID.
     * @param p_delete A bi-function for making HTTP DELETE requests to delete entities by ID, taking a configuration
     *                 and an ID.
     */
    public CRDPropBuilder(String name,
                          Gen<O> gen,
                          BiLambda<JsObj, O, HttpResponse<String>> p_post,
                          BiLambda<JsObj, String, HttpResponse<String>> p_get,
                          BiLambda<JsObj, String, HttpResponse<String>> p_delete
                         ) {
        super(name, gen, p_post, p_get, p_delete);
    }

    /**
     * Creates a property test based on the configured CRUD operations.
     *
     * @return A property test for Create (POST), Read (GET), and Delete (DELETE) operations on a RESTful API.
     */
    @Override
    public Property<O> create() {
        BiLambda<JsObj, O, TestResult> lambda =
                (conf, body) -> {
                    return post.apply(conf, body).then(resp -> {
                                   TestResult apply = postAssert.apply(resp);
                                   if (Objects.requireNonNull(apply) instanceof TestException exc)
                                       return IO.fail(exc);
                                   if (apply instanceof TestFailure failure)
                                       return IO.fail(failure);
                                   return getId.apply(body, resp)
                                               .then(id -> get.apply(conf, id)
                                                              .map(r -> Pair.of(id, r)));

                               })
                               .then(pair -> {
                                   TestResult apply = getAssert.apply(pair.second());
                                   if (Objects.requireNonNull(apply) instanceof TestException exc)
                                       return IO.fail(exc);
                                   if (apply instanceof TestFailure failure)
                                       return IO.fail(failure);
                                   return delete.apply(conf, pair.first())
                                                .map(r -> Pair.of(pair.first(), r));

                               })
                               .then(pair -> {
                                   TestResult apply = deleteAssert.apply(pair.second());
                                   if (Objects.requireNonNull(apply) instanceof TestException exc)
                                       return IO.fail(exc);
                                   if (apply instanceof TestFailure failure)
                                       return IO.fail(failure);
                                   return get.apply(conf, pair.first());
                               })
                               .map(resp -> resp.statusCode() == 404 ?
                                       TestResult.SUCCESS :
                                       TestFailure.reason("Entity found after being deleted successfully.Status code received %d".formatted(resp.statusCode())));
                };

        return Property.ofLambda(name, gen, lambda);
    }
}