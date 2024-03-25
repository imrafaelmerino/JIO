package jio.http.client.oauth;

import jio.IO;
import jio.Lambda;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static java.util.Objects.requireNonNull;

/**
 * Represents a function that takes a http client and send a predefined request to get the access token to the server,
 * returning the http response which body is a JsObj. The predefined request is the following:
 *
 * <pre>
 *
 * POST https|http://host:port/uri
 * grant_type=client_credentials
 *
 * Accept: application/json
 * Authorization: Base64("${ClientId}:${ClientSecret}")
 * Content-Type: application/x-www-form-urlencoded
 *
 * </pre>
 *
 * @see ClientCredentialsBuilder .
 */
public final class AccessTokenRequest implements Lambda<OauthHttpClient, HttpResponse<String>> {

  private final URI uri;
  private final String authorizationHeader;

  private AccessTokenRequest(final String clientId,
                             final String clientSecret,
                             final URI uri
  ) {

    String credentials = requireNonNull(clientId) + ":" + requireNonNull(clientSecret);
    this.authorizationHeader = Base64.getEncoder()
                                     .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    this.uri = requireNonNull(uri);
  }

  /**
   * Factory method to create the function that takes a http client and send the following request to the server
   * <pre>
   *
   * POST scheme://host:port/path
   * grant_type=client_credentials
   *
   * Accept: application/json
   * Authorization: Base64("${ClientId}:${ClientSecret}")
   * Content-Type: application/x-www-form-urlencoded
   *
   *
   * </pre>
   *
   * @param clientId     the client id
   * @param clientSecret the client secret
   * @param uri          the uri
   * @return a lambda to make the access token request from the http client
   */
  public static AccessTokenRequest of(final String clientId,
                                      final String clientSecret,
                                      final URI uri
  ) {
    return new AccessTokenRequest(clientId,
                                  clientSecret,
                                  uri);
  }

  @Override
  public IO<HttpResponse<String>> apply(final OauthHttpClient client) {
    var body = "grant_type=client_credentials";
    return requireNonNull(client).ofString()
                                 .apply(HttpRequest.newBuilder()
                                                   .header("Accept",
                                                           "application/json"
                                                   )
                                                   .header("Authorization",
                                                           String.format("Basic %s",
                                                                         authorizationHeader
                                                           )
                                                   )
                                                   .header("Content-Type",
                                                           "application/x-www-form-urlencoded"
                                                   )
                                                   .uri(uri)
                                                   .POST(HttpRequest.BodyPublishers.ofString(body)));

  }

}
