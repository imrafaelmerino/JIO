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

/**
 * An abstract base class for building property tests for RESTful APIs. This class provides a flexible framework for
 * defining property tests for HTTP POST, GET, and DELETE operations on a RESTful API endpoint.
 *
 * @param <O> The type of data generated to feed the property tests.
 * @param <A> The concrete subclass type for fluent builder methods.
 */
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

    /**
     * Creates a new instance of the RestPropBuilder class with the specified parameters.
     *
     * @param name     The name of the property test.
     * @param gen      The data generator that produces pseudorandom data for testing.
     * @param p_post   The lambda function representing the HTTP POST operation.
     * @param p_get    The lambda function representing the HTTP GET operation.
     * @param p_delete The lambda function representing the HTTP DELETE operation.
     */
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

    /**
     * Creates a new instance of the RestPropBuilder class with the specified parameters.
     *
     * @param name     The name of the property test.
     * @param gen      The data generator that produces pseudorandom data for testing.
     * @param p_post   The lambda function representing the HTTP POST operation.
     * @param p_get    The lambda function representing the HTTP GET operation.
     * @param p_delete The lambda function representing the HTTP DELETE operation.
     */
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

    /**
     * Sets the assertion function for the HTTP POST operation.
     *
     * @param postAssert The assertion function for the HTTP POST operation.
     * @return This RestPropBuilder instance with the updated assertion function.
     */
    @SuppressWarnings("unchecked")
    public A withPostAssert(Function<HttpResponse<String>, TestResult> postAssert) {
        this.postAssert = Objects.requireNonNull(postAssert);
        return (A) this;
    }

    /**
     * Sets the assertion function for the HTTP GET operation.
     *
     * @param getAssert The assertion function for the HTTP GET operation.
     * @return This RestPropBuilder instance with the updated assertion function.
     */
    @SuppressWarnings("unchecked")
    public A withGetAssert(Function<HttpResponse<String>, TestResult> getAssert) {
        this.getAssert = Objects.requireNonNull(getAssert);
        return (A) this;
    }

    /**
     * Sets the assertion function for the HTTP DELETE operation.
     *
     * @param deleteAssert The assertion function for the HTTP DELETE operation.
     * @return This RestPropBuilder instance with the updated assertion function.
     */
    @SuppressWarnings("unchecked")
    public A withDeleteAssert(Function<HttpResponse<String>, TestResult> deleteAssert) {
        this.deleteAssert = Objects.requireNonNull(deleteAssert);
        return (A) this;
    }

    /**
     * Sets the function to extract an ID for subsequent HTTP requests. You can choose from two specific ways to extract
     * the ID:
     * <ul>
     *   <li>Use {@link #withGetIdFromReqBody(Function)} to extract the ID from the request body of type O.</li>
     *   <li>Use {@link #withGetIdFromRespPath(JsPath)} to extract the ID from the HTTP response using a specific path.</li>
     * </ul>
     *
     * @param getId The function to extract an ID for subsequent HTTP requests.
     * @return This RestPropBuilder instance with the updated ID extraction method.
     */
    @SuppressWarnings("unchecked")
    public A withGetId(BiFunction<O, HttpResponse<String>, IO<String>> getId) {
        this.getId = Objects.requireNonNull(getId);
        return (A) this;
    }

    /**
     * Sets the path to extract an ID from the HTTP response and use it in subsequent HTTP requests.
     *
     * @param path The path to extract an ID from the HTTP response.
     * @return This RestPropBuilder instance with the updated ID extraction path.
     */
    @SuppressWarnings("unchecked")
    public A withGetIdFromRespPath(final JsPath path) {
        this.getId = getIdFromPath.apply(Objects.requireNonNull(path));
        return (A) this;
    }

    /**
     * Sets the function to extract an ID from the request body of type O and use it in subsequent HTTP requests.
     *
     * @param p_getId The function to extract an ID from the request body of type O.
     * @return This RestPropBuilder instance with the updated ID extraction function.
     */
    @SuppressWarnings("unchecked")
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
