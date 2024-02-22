package jio.test.pbt.rest;

import static java.util.Objects.requireNonNull;

import fun.gen.Gen;
import java.net.http.HttpResponse;
import java.util.function.BiFunction;
import java.util.function.Function;
import jio.BiLambda;
import jio.IO;
import jio.Lambda;
import jio.test.pbt.Property;
import jio.test.pbt.PropertyBuilder;
import jio.test.pbt.TestFailure;
import jio.test.pbt.TestResult;
import jsonvalues.JsNothing;
import jsonvalues.JsObj;
import jsonvalues.JsPath;
import jsonvalues.JsValue;
import jsonvalues.spec.JsParserException;

/**
 * An abstract base class for building property tests for RESTful APIs. This class provides a flexible framework for
 * defining property tests for HTTP POST, GET, and DELETE operations on a RESTful API endpoint.
 *
 * @param <GenReqBody>  The type of data generated to feed the property tests.
 * @param <PropBuilder> The concrete subclass type for fluent builder methods.
 */
abstract class RestPropBuilder<GenReqBody, PropBuilder extends RestPropBuilder<GenReqBody, PropBuilder>> {

  @SuppressWarnings("UnnecessaryLambda")
  final static Function<HttpResponse<String>, TestResult> DEFAULT_RESP_ASSERT = resp -> resp.statusCode() < 300
                                                                                        ? TestResult.SUCCESS
                                                                                        : TestFailure.reason(
                                                                                            "Expected status code < 300, but got a "
                                                                                            + resp.statusCode());
  final String name;
  final BiLambda<JsObj, GenReqBody, HttpResponse<String>> post;
  final BiLambda<JsObj, String, HttpResponse<String>> get;
  final BiLambda<JsObj, String, HttpResponse<String>> delete;
  final Gen<GenReqBody> gen;
  @SuppressWarnings("UnnecessaryLambda")
  private final Function<JsPath, BiFunction<GenReqBody, HttpResponse<String>, IO<String>>> getIdFromPath = path -> (body,
                                                                                                                    resp) -> {
    try {
      JsObj respBody = JsObj.parse(resp.body());
      JsValue id = respBody.get(path);
      return id == JsNothing.NOTHING ? IO.fail(TestFailure.reason(path + " not found in the following json: " + resp
          .body()))
                                     : IO.succeed(id.toString());
    } catch (JsParserException e) {
      return IO.fail(TestFailure.reason("resp body is not a Json well-formed: " + resp.body()));
    }
  };
  Function<HttpResponse<String>, TestResult> postAssert = DEFAULT_RESP_ASSERT;
  Function<HttpResponse<String>, TestResult> getAssert = DEFAULT_RESP_ASSERT;
  Function<HttpResponse<String>, TestResult> deleteAssert = DEFAULT_RESP_ASSERT;
  BiFunction<GenReqBody, HttpResponse<String>, IO<String>> getId;

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
                         Gen<GenReqBody> gen,
                         BiLambda<JsObj, GenReqBody, HttpResponse<String>> p_post,
                         BiLambda<JsObj, String, HttpResponse<String>> p_get,
                         BiLambda<JsObj, String, HttpResponse<String>> p_delete
                        ) {
    this.post = requireNonNull(p_post);
    this.get = requireNonNull(p_get);
    this.delete = requireNonNull(p_delete);
    this.name = requireNonNull(name);
    this.gen = requireNonNull(gen);
    this.getId = getIdFromPath.apply(JsPath.fromKey("id"));
  }

  /**
   * Sets the assertion function for the HTTP POST operation.
   *
   * @param postAssert The assertion function for the HTTP POST operation.
   * @return This RestPropBuilder instance with the updated assertion function.
   */
  @SuppressWarnings("unchecked")
  public PropBuilder withPostAssert(final Function<HttpResponse<String>, TestResult> postAssert) {
    this.postAssert = requireNonNull(postAssert);
    return (PropBuilder) this;
  }

  /**
   * Sets the assertion function for the HTTP GET operation.
   *
   * @param getAssert The assertion function for the HTTP GET operation.
   * @return This RestPropBuilder instance with the updated assertion function.
   */
  @SuppressWarnings("unchecked")
  public PropBuilder withGetAssert(final Function<HttpResponse<String>, TestResult> getAssert) {
    this.getAssert = requireNonNull(getAssert);
    return (PropBuilder) this;
  }

  /**
   * Sets the assertion function for the HTTP DELETE operation.
   *
   * @param deleteAssert The assertion function for the HTTP DELETE operation.
   * @return This RestPropBuilder instance with the updated assertion function.
   */
  @SuppressWarnings("unchecked")
  public PropBuilder withDeleteAssert(final Function<HttpResponse<String>, TestResult> deleteAssert) {
    this.deleteAssert = requireNonNull(deleteAssert);
    return (PropBuilder) this;
  }

  /**
   * Sets the function to extract an ID for subsequent HTTP requests. You can choose from two specific ways to extract
   * the ID:
   * <ul>
   * <li>Use {@link #withGetIdFromReqBody(Function)} to extract the ID from the request body of type O.</li>
   * <li>Use {@link #withGetIdFromJSONRespPath(JsPath)} to extract the ID from the HTTP response using a specific
   * path.</li>
   * </ul>
   *
   * @param getId The function to extract an ID for subsequent HTTP requests.
   * @return This RestPropBuilder instance with the updated ID extraction method.
   */
  @SuppressWarnings("unchecked")
  public PropBuilder withGetId(BiFunction<GenReqBody, HttpResponse<String>, IO<String>> getId) {
    this.getId = requireNonNull(getId);
    return (PropBuilder) this;
  }

  /**
   * Sets the path to extract an ID from the JSON response and use it in subsequent HTTP requests.
   *
   * @param path The path to extract an ID from the HTTP response.
   * @return This RestPropBuilder instance with the updated ID extraction path.
   */
  @SuppressWarnings("unchecked")
  public PropBuilder withGetIdFromJSONRespPath(final JsPath path) {
    this.getId = getIdFromPath.apply(requireNonNull(path));
    return (PropBuilder) this;
  }

  /**
   * Sets the function to extract an ID from the request body of type O and use it in subsequent HTTP requests.
   *
   * @param p_getId The function to extract an ID from the request body of type O.
   * @return This RestPropBuilder instance with the updated ID extraction function.
   */
  @SuppressWarnings("unchecked")
  public PropBuilder withGetIdFromReqBody(final Function<GenReqBody, String> p_getId) {
    requireNonNull(p_getId);
    this.getId = (body,
                  resp) -> {
      String id = p_getId.apply(body);
      return id == null || id.isBlank() || id.isEmpty() ? IO.fail(TestFailure.reason("id not found")) : IO.succeed(id);
    };
    return (PropBuilder) this;
  }

  Lambda<HttpResponse<String>, IdResp> assertResp(
      Function<HttpResponse<String>, TestResult> assertResp,
      String id
                                                 ) {
    return resp -> {
      TestResult result = assertResp.apply(resp);
      if (result instanceof TestFailure f) {
        return IO.fail(f);
      }
      return IO.succeed(new IdResp(id,
                                   resp));
    };
  }

  /**
   * returns a property builder. If no further customization is needed, you can use {@link #build()}
   *
   * @return a property builder
   */
  public abstract PropertyBuilder<GenReqBody> get();

  /**
   * Build a property with the default parameters. For further customization (times of generations, description,
   * executor etc), use {@link #get()} that returns a property builder
   *
   * @return a property
   */
  public Property<GenReqBody> build() {
    return get().get();
  }

}
