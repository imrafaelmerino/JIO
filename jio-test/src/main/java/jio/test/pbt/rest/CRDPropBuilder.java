package jio.test.pbt.rest;

import fun.gen.Gen;
import jio.BiLambda;
import jio.IO;
import jio.Lambda;
import jio.test.pbt.PropBuilder;
import jio.test.pbt.TestFailure;
import jio.test.pbt.TestResult;
import jio.test.pbt.TestSuccess;
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


    private CRDPropBuilder(String name,
                           Gen<O> gen,
                           BiLambda<JsObj, O, HttpResponse<String>> p_post,
                           BiLambda<JsObj, String, HttpResponse<String>> p_get,
                           BiLambda<JsObj, String, HttpResponse<String>> p_delete
                          ) {
        super(name, gen, p_post, p_get, p_delete);
    }

    /**
     * Creates a new instance of CRDPropBuilder for building property tests for CRUD operations.
     *
     * @param name     The name of the property test.
     * @param gen      A generator for producing input data of type O.
     * @param p_post   A function for making HTTP POST requests to create entities.
     * @param p_get    A function for making HTTP GET requests to retrieve entities by ID.
     * @param p_delete A function for making HTTP DELETE requests to delete entities by ID.
     */
    public static <O> CRDPropBuilder<O> of(final String name,
                                           final Gen<O> gen,
                                           final Lambda<O, HttpResponse<String>> p_post,
                                           final Lambda<String, HttpResponse<String>> p_get,
                                           final Lambda<String, HttpResponse<String>> p_delete
                                          ) {
        Objects.requireNonNull(p_post);
        Objects.requireNonNull(p_get);
        Objects.requireNonNull(p_delete);
        return new CRDPropBuilder<>(name,
                                    gen,
                                    (conf, body) -> Objects.requireNonNull(p_post).apply(body),
                                    (conf, id) -> Objects.requireNonNull(p_get).apply(id),
                                    (conf, id) -> Objects.requireNonNull(p_delete).apply(id));
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
    public static <O> CRDPropBuilder<O> of(final String name,
                                           final Gen<O> gen,
                                           final BiLambda<JsObj, O, HttpResponse<String>> p_post,
                                           final BiLambda<JsObj, String, HttpResponse<String>> p_get,
                                           final BiLambda<JsObj, String, HttpResponse<String>> p_delete
                                          ) {
        return new CRDPropBuilder<>(name, gen, p_post, p_get, p_delete);
    }

    /**
     * Creates a property test based on the configured CRUD operations.
     *
     * @return A property test for Create (POST), Read (GET), and Delete (DELETE) operations on a RESTful API.
     */
    @Override
    public PropBuilder<O> buildPropBuilder() {
        BiLambda<JsObj, O, TestResult> lambda =
                (conf, body) -> post.apply(conf, body)
                                    .then(resp ->
                                                  switch (postAssert.apply(resp)) {
                                                      case TestSuccess $ -> getId.apply(body, resp);
                                                      case TestFailure f -> IO.fail(f);
                                                  }
                                         )
                                    .then(id -> get.apply(conf, id)
                                                   .then(assertResp(getAssert, id))

                                         )

                                    .then(idResp -> delete.apply(conf, idResp.id())
                                                          .then(assertResp(deleteAssert,
                                                                           idResp.id()))
                                         )

                                    .then(idResp -> get.apply(conf,
                                                              idResp.id()))

                                    .map(resp -> resp.statusCode() == 404 ?
                                            TestResult.SUCCESS :
                                            TestFailure.reason("Entity found after being deleted successfully. Status code received %d".formatted(resp.statusCode())));

        return PropBuilder.ofLambda(name, gen, lambda);
    }


}