package jio.http.client.oauth;

import jio.IO;
import jio.Lambda;
import jio.http.client.HttpLambda;
import jio.http.client.MyHttpClient;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Http client with OAuth Client Credentials Grant support.
 */
public final class ClientCredentialsHttpClient implements MyOauthHttpClient {
    private static final int MAX_REFRESH_TOKEN_LOOP_SIZE = 3;
    private volatile String accessToken;
    private final MyHttpClient httpClient;
    private final Function<MyOauthHttpClient, IO<HttpResponse<String>>> accessTokenReq;
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

    ClientCredentialsHttpClient(final MyHttpClient client,
                                final Function<MyOauthHttpClient, IO<HttpResponse<String>>> accessTokenReq,
                                final String authorizationHeaderName,
                                final Function<String, String> authorizationHeaderValue,
                                final Lambda<HttpResponse<String>, String> getAccessToken,
                                final Predicate<HttpResponse<?>> refreshTokenPredicate
                               ) {
        this.httpClient = client;
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


    private <I> IO<HttpResponse<I>> oauthRequest(final HttpLambda<I> httpLambda,
                                                 final HttpRequest.Builder builder,
                                                 final boolean refreshToken,
                                                 final int deep
                                                ) {
        if (deep == MAX_REFRESH_TOKEN_LOOP_SIZE) return IO.fromFailure(new RefreshTokenLoop(deep));

        IO<String> getToken = (refreshToken || this.accessToken == null) ?
                accessTokenReq.apply(this)
                              .then(getAccessToken)
                              .peekSuccess(newToken -> this.accessToken = newToken) :
                IO.fromValue(this.accessToken);


        return getToken.then(token ->
                                     httpLambda.apply(builder.setHeader(authorizationHeaderName,
                                                                        authorizationHeaderValue.apply(token)
                                                                       )
                                                     )
                                               .then(resp ->
                                                             refreshTokenPredicate.test(resp) ?
                                                                     oauthRequest(httpLambda,
                                                                                  builder,
                                                                                  true,
                                                                                  deep + 1
                                                                                 ) :
                                                                     IO.fromValue(resp)
                                                    )
                            );
    }

}
