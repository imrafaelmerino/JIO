package jio.http.client.oauth;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Function;
import java.util.function.Predicate;
import jio.IO;
import jio.Lambda;
import jio.http.client.HttpLambda;
import jio.http.client.JioHttpClient;
import jio.http.client.JioHttpClientBuilder;

/**
 * An HTTP client with support for OAuth Client Credentials Grant. This client allows you to make HTTP requests with
 * OAuth authentication using the Client Credentials Grant flow. It automatically handles access token expiration and
 * refreshes tokens when needed.
 */
final class ClientCredentialsClient implements OauthHttpClient {

  private static final int MAX_REFRESH_TOKEN_LOOP_SIZE = 3;
  private final JioHttpClient httpClient;
  private final Function<OauthHttpClient, IO<HttpResponse<String>>> accessTokenReq;
  private final String authorizationHeaderName;
  private final Function<String, String> authorizationHeaderValue;
  private final Lambda<HttpResponse<String>, String> getAccessToken;
  private final Predicate<HttpResponse<?>> refreshTokenPredicate;
  private final HttpLambda<String> oauthStringLambda;
  private final HttpLambda<byte[]> oauthBytesLambda;
  private final HttpLambda<String> ofStringLambda;
  private final HttpLambda<byte[]> ofBytesLambda;
  private final HttpLambda<Void> discardingLambda;
  private final HttpLambda<Void> oauthDiscardingLambda;
  private volatile String accessToken;

  ClientCredentialsClient(final JioHttpClientBuilder client,
                          final Function<OauthHttpClient, IO<HttpResponse<String>>> accessTokenReq,
                          final String authorizationHeaderName,
                          final Function<String, String> authorizationHeaderValue,
                          final Lambda<HttpResponse<String>, String> getAccessToken,
                          final Predicate<HttpResponse<?>> refreshTokenPredicate
  ) {
    this.httpClient = client.get();
    this.accessTokenReq = accessTokenReq;
    this.authorizationHeaderName = authorizationHeaderName;
    this.authorizationHeaderValue = authorizationHeaderValue;
    this.getAccessToken = getAccessToken;
    this.refreshTokenPredicate = refreshTokenPredicate;
    this.ofStringLambda = httpClient.ofString();
    this.ofBytesLambda = httpClient.ofBytes();
    this.discardingLambda = httpClient.discarding();
    this.oauthDiscardingLambda = builder -> oauthRequest(discardingLambda,
                                                         builder,
                                                         false,
                                                         0
    );
    this.oauthStringLambda = builder -> oauthRequest(ofStringLambda,
                                                     builder,
                                                     false,
                                                     0
    );
    this.oauthBytesLambda = builder -> oauthRequest(ofBytesLambda,
                                                    builder,
                                                    false,
                                                    0
    );
  }

  @Override
  public HttpLambda<String> oauthOfString() {
    return oauthStringLambda;
  }

  @Override
  public HttpLambda<byte[]> oauthOfBytes() {
    return oauthBytesLambda;
  }

  @Override
  public HttpLambda<Void> oauthDiscarding() {
    return oauthDiscardingLambda;
  }

  @Override
  public <T> HttpLambda<T> oauthBodyHandler(HttpResponse.BodyHandler<T> handler) {
    return builder -> oauthRequest(bodyHandler(handler),
                                   builder,
                                   false,
                                   0
    );
  }

  @Override
  public HttpLambda<String> ofString() {
    return ofStringLambda;
  }

  @Override
  public HttpLambda<byte[]> ofBytes() {
    return ofBytesLambda;
  }

  @Override
  public HttpLambda<Void> discarding() {
    return discardingLambda;
  }

  @Override
  public <T> HttpLambda<T> bodyHandler(HttpResponse.BodyHandler<T> handler) {
    return httpClient.bodyHandler(handler);
  }

  @Override
  public void shutdown() {
    httpClient.shutdown();
  }

  @Override
  public void shutdownNow() {
    httpClient.shutdownNow();
  }

  @Override
  public void close() {
    httpClient.close();
  }

  private <I> IO<HttpResponse<I>> oauthRequest(final HttpLambda<I> httpLambda,
                                               final HttpRequest.Builder builder,
                                               final boolean refreshToken,
                                               final int deep
  ) {
    if (deep == MAX_REFRESH_TOKEN_LOOP_SIZE) {
      return IO.fail(new RefreshTokenLoop(deep));
    }

    IO<String> getToken = (refreshToken || this.accessToken == null)
        ? accessTokenReq.apply(this)
                        .then(getAccessToken)
                        .peekSuccess(newToken -> this.accessToken = newToken)
        : IO.succeed(this.accessToken);

    return getToken.then(token -> httpLambda.apply(builder.setHeader(authorizationHeaderName,
                                                                     authorizationHeaderValue.apply(token)
    )
    )
                                            .then(resp -> refreshTokenPredicate.test(resp) ? oauthRequest(httpLambda,
                                                                                                          builder,
                                                                                                          true,
                                                                                                          deep + 1
                                            )
                                                : IO.succeed(resp)
                                            )
    );
  }

}
