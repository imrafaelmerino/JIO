package jio.test.pbt.rest;

import fun.gen.Gen;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import jio.BiLambda;
import jio.IO;
import jio.Lambda;
import jio.test.pbt.PropertyBuilder;
import jio.test.pbt.TestFailure;
import jio.test.pbt.TestResult;
import jsonvalues.JsObj;

/**
 * A builder class for creating property tests that cover CRUD (Create, Read, Update, Delete) operations on a RESTful
 * API endpoint.
 *
 * @param <GenReqBody> The type of data generated to feed the property tests.
 */
public final class CRUDPropBuilder<GenReqBody> extends RestPropBuilder<GenReqBody, CRUDPropBuilder<GenReqBody>>
    implements
    Supplier<PropertyBuilder<GenReqBody>> {

  private final BiLambda<JsObj, HttpResponse<String>, HttpResponse<String>> update;

  private Function<HttpResponse<String>, TestResult> updateAssert = DEFAULT_RESP_ASSERT;

  private CRUDPropBuilder(String name,
                          Gen<GenReqBody> gen,
                          BiLambda<JsObj, GenReqBody, HttpResponse<String>> p_post,
                          BiLambda<JsObj, String, HttpResponse<String>> p_get,
                          BiLambda<JsObj, HttpResponse<String>, HttpResponse<String>> p_update,
                          BiLambda<JsObj, String, HttpResponse<String>> p_delete
                         ) {
    super(name,
          gen,
          p_post,
          p_get,
          p_delete);
    this.update = Objects.requireNonNull(p_update);
  }

  /**
   * Creates a new instance of CRUDPropBuilder with the specified parameters.
   *
   * @param name     The name of the property test.
   * @param gen      The data generator that produces pseudorandom data for testing.
   * @param p_post   The lambda function representing the HTTP POST operation.
   * @param p_get    The lambda function representing the HTTP GET operation.
   * @param p_update The lambda function representing the HTTP UPDATE operation.
   * @param p_delete The lambda function representing the HTTP DELETE operation.
   * @param <O>      The type of data generated to feed the property tests.
   * @return a CRUDPropBuilder
   */
  public static <O> CRUDPropBuilder<O> of(final String name,
                                          final Gen<O> gen,
                                          final Lambda<O, HttpResponse<String>> p_post,
                                          final Lambda<String, HttpResponse<String>> p_get,
                                          final Lambda<HttpResponse<String>, HttpResponse<String>> p_update,
                                          final Lambda<String, HttpResponse<String>> p_delete
                                         ) {
    Objects.requireNonNull(p_post);
    Objects.requireNonNull(p_get);
    Objects.requireNonNull(p_update);
    Objects.requireNonNull(p_delete);
    return new CRUDPropBuilder<>(name,
                                 gen,
                                 (conf,
                                  body) -> Objects.requireNonNull(p_post)
                                                  .apply(body),
                                 (conf,
                                  id) -> Objects.requireNonNull(p_get)
                                                .apply(id),
                                 (conf,
                                  id) -> Objects.requireNonNull(p_update)
                                                .apply(id),
                                 (conf,
                                  id) -> Objects.requireNonNull(p_delete)
                                                .apply(id));
  }

  /**
   * Creates a new instance of CRUDPropBuilder with the specified parameters.
   *
   * @param name     The name of the property test.
   * @param gen      The data generator that produces pseudorandom data for testing.
   * @param p_post   The lambda function representing the HTTP POST operation.
   * @param p_get    The lambda function representing the HTTP GET operation.
   * @param p_update The lambda function representing the HTTP UPDATE operation.
   * @param p_delete The lambda function representing the HTTP DELETE operation.
   * @param <O>      The type of data generated to feed the property tests.
   * @return a CRUDPropBuilder
   */
  public static <O> CRUDPropBuilder<O> of(final String name,
                                          final Gen<O> gen,
                                          final BiLambda<JsObj, O, HttpResponse<String>> p_post,
                                          final BiLambda<JsObj, String, HttpResponse<String>> p_get,
                                          final BiLambda<JsObj, HttpResponse<String>, HttpResponse<String>> p_update,
                                          final BiLambda<JsObj, String, HttpResponse<String>> p_delete
                                         ) {
    return new CRUDPropBuilder<>(name,
                                 gen,
                                 p_post,
                                 p_get,
                                 p_update,
                                 p_delete);
  }

  /**
   * Creates a property test based on the configuration of this builder.
   *
   * @return A property test for the CRUD operations defined by this builder.
   */
  @Override
  public PropertyBuilder<GenReqBody> get() {
    BiLambda<JsObj, GenReqBody, TestResult> lambda = (conf,
                                                      body) -> post.apply(conf,
                                                                          body)
                                                                   .then(resp -> {
                                                                           TestResult result = postAssert.apply(resp);
                                                                           if (result instanceof TestFailure f) {
                                                                             return IO.fail(f);
                                                                           }
                                                                           return getId.apply(body,
                                                                                              resp);
                                                                         }
                                                                        )
                                                                   .then(id -> get.apply(conf,
                                                                                         id)
                                                                                  .then(assertResp(getAssert,
                                                                                                   id))
                                                                        )
                                                                   .then(idResp -> update.apply(conf,
                                                                                                idResp.resp())
                                                                                         .then(assertResp(updateAssert,
                                                                                                          idResp.id())))
                                                                   .then(idResp -> delete.apply(conf,
                                                                                                idResp.id())
                                                                                         .then(assertResp(deleteAssert,
                                                                                                          idResp.id()))
                                                                        )
                                                                   .then(idResp -> get.apply(conf,
                                                                                             idResp.id()))
                                                                   .map(resp -> resp.statusCode() == 404
                                                                                ? TestResult.SUCCESS
                                                                                : TestFailure.reason(
                                                                                    "Entity found after being deleted successfully. Status code received %d".formatted(
                                                                                        resp.statusCode())));

    return PropertyBuilder.ofLambda(name,
                                    gen,
                                    lambda);
  }

  /**
   * Sets the assertion function for the HTTP UPDATE operation.
   *
   * @param updateAssert The assertion function for the HTTP UPDATE operation.
   * @return This CRUDPropBuilder instance with the updated assertion function.
   */
  public CRUDPropBuilder<GenReqBody> withUpdateAssert(
      Function<HttpResponse<String>, TestResult> updateAssert) {
    this.updateAssert = Objects.requireNonNull(updateAssert);
    return this;
  }
}