package jio.http.client.oauth;

import jio.IO;
import jio.Lambda;
import jio.http.client.JioHttpClientBuilder;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Builder to create a http client with OAuth Client Credentials Grant support. The following options can be
 * customized:
 * <ul>
 * <li>The request sent to the server to get the access token</li>
 * <li>A function to read the access token from the server response</li>
 * <li>A predicate that takes the server response and returns true if the access token needs to be refreshed</li>
 * <li>The authorization header name</li>
 * <li>A function to create the authorization header output from the access token</li>
 * <li>A retry policy to make the access token request more resilient</li>
 * </ul>
 *
 * @see AccessTokenRequest
 * @see GetAccessToken
 * @see #withAuthorizationHeaderName(String)
 * @see #withAuthorizationHeaderValue(Function)
 */
public final class ClientCredentialsBuilder implements Supplier<OauthHttpClient> {

  private final Function<OauthHttpClient, IO<HttpResponse<String>>> accessTokenReq;
  private final Lambda<HttpResponse<String>, String> getAccessToken;
  private final Predicate<HttpResponse<?>> refreshTokenPredicate;
  private final JioHttpClientBuilder client;
  String authorizationHeaderName = "Authorization";
  Function<String, String> authorizationHeaderValue = token -> String.format("Bearer %s",
                                                                             token
  );

  private ClientCredentialsBuilder(final JioHttpClientBuilder builder,
                                   final Lambda<OauthHttpClient, HttpResponse<String>> accessTokenReq,
                                   final Lambda<HttpResponse<String>, String> getAccessToken,
                                   final Predicate<HttpResponse<?>> refreshTokenPredicate
  ) {
    this.client = builder;
    this.accessTokenReq = requireNonNull(accessTokenReq);
    this.getAccessToken = requireNonNull(getAccessToken);
    this.refreshTokenPredicate = requireNonNull(refreshTokenPredicate);
  }

  /**
   * Creates a http builder with oauth client credentials grand support from a regular http client, two lambdas to get
   * the access token, and a predicate to check if the access token needs to be refreshed. There is a predefined http
   * request to get the access token implemented in {@link AccessTokenRequest}. There is also a default function
   * implemented in {@link GetAccessToken} to read the access token from the access token request response.
   * <p>
   * The predicate passed in the <code>refreshTokenPredicate</code> parameter must be implemented carefully. For example
   * consider the following implementation:
   *
   * <pre>
   * {@code
   *
   *     Predicate<HttpResponse<?>> refreshTokenPredicate = resp -> resp.statusCode() == 401;
   * }
   * </pre>
   * <p>
   * This implementation considering only the http status code may be wrong if there is an api gateway and a backend
   * service that both return 401 codes for different circumstances. In this case you need another condition to make
   * sure the 401 response is from the api gateway indicating you need to refresh the token
   *
   * @param builder               the regular {@link HttpClient http client}
   * @param accessTokenReq        lambda that takes the regular http client and sends a http request to the server,
   *                              returning the response.
   * @param getAccessToken        lambda that takes the server response and returns the oauth token
   * @param refreshTokenPredicate predicate that checks the response to see if the access token need to be refreshed
   * @return a ClientCredsBuilder
   * @see AccessTokenRequest
   * @see GetAccessToken
   */
  public static ClientCredentialsBuilder of(final JioHttpClientBuilder builder,
                                            final Lambda<OauthHttpClient, HttpResponse<String>> accessTokenReq,
                                            final Lambda<HttpResponse<String>, String> getAccessToken,
                                            final Predicate<HttpResponse<?>> refreshTokenPredicate
  ) {
    return new ClientCredentialsBuilder(builder,
                                        accessTokenReq,
                                        getAccessToken,
                                        refreshTokenPredicate);
  }

  /**
   * Creates a new http client
   *
   * @return a ClientCredentialsHttpClient
   */
  @Override
  public OauthHttpClient get() {
    return new ClientCredentialsClient(client,
                                       accessTokenReq,
                                       authorizationHeaderName,
                                       authorizationHeaderValue,
                                       getAccessToken,
                                       refreshTokenPredicate
    );
  }

  /**
   * Option to define the name of the authorization header, which by default is Authorization
   *
   * @param authorizationHeaderName the name of the authorization header
   * @return this builder
   */
  public ClientCredentialsBuilder withAuthorizationHeaderName(final String authorizationHeaderName) {
    this.authorizationHeaderName = requireNonNull(authorizationHeaderName);
    return this;
  }

  /**
   * Option to define how to build the authorization header output from the access token, which by default is "Bearer
   * ${Access Token}"
   *
   * @param fn function that takes the access token and returns the authorization header output
   * @return this builder
   */
  public ClientCredentialsBuilder withAuthorizationHeaderValue(final Function<String, String> fn) {
    this.authorizationHeaderValue = requireNonNull(fn);
    return this;
  }

}