package jio.http.client.oauth;

import jio.IO;
import jio.Lambda;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Represents a function that takes a http client and send a predefined request to get
 * the access token to the server, returning the http response which body is a JsObj. The predefined request is
 * the following:
 *
 * <pre>
 *
 *     POST https|http://host:port/uri
 *     grant_type=client_credentials
 *
 *     Accept: application/json
 *     Authorization: Base64("${ClientId}:${ClientSecret}")
 *     Content-Type: application/x-www-form-urlencoded
 *
 * </pre>
 * <p>
 * There are two constructors.
 * <ul>
 * <li>{@link #AccessTokenRequest(String, String, String, int)} to specify the host, port, client id and client secret.
 * In this case the uri is /token and the protocol is https.</li>
 * <li>{@link #AccessTokenRequest(String, String, String, int, String, boolean)} to specify the host, port, client id
 * , client secret, uri and the protocol (http or https)
 * </li>
 * </ul>
 *
 * @see ClientCredentialsHttpClientBuilder
 * .
 */
public final class AccessTokenRequest implements Lambda<MyOauthHttpClient, HttpResponse<String>> {

    private static final String DEFAULT_URI = "token";
    private final String uri;
    private final String authorizationHeader;
    private final String host;
    private final Integer port;
    private final Boolean ssl;

    /**
     * Constructor to create the function that takes a http client and send the following request to
     * the server
     * <pre>
     *
     *     POST https|http://host:port/uri
     *     grant_type=client_credentials
     *
     *     Accept: application/json
     *     Authorization: Base64("${ClientId}:${ClientSecret}")
     *     Content-Type: application/x-www-form-urlencoded
     *
     * </pre>
     *
     * @param clientId     the client id
     * @param clientSecret the client secret
     * @param host         the host server
     * @param port         the port the server is listening on
     * @param uri          the uri
     * @param ssl          if true the protocols is https, otherwise http
     */
    public AccessTokenRequest(final String clientId,
                              final String clientSecret,
                              final String host,
                              final int port,
                              final String uri,
                              final boolean ssl
                             ) {
        String credentials = Objects.requireNonNull(clientId) + ":" + Objects.requireNonNull(clientSecret);
        this.authorizationHeader = Base64.getEncoder()
                                         .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        this.host = requireNonNull(host);
        this.uri = requireNonNull(uri);
        this.port = port;
        this.ssl = ssl;
    }

    /**
     * Constructor to create the function that takes a http client and send the following request to
     * the server
     * <pre>
     *
     *     POST https://host:port/token
     *     grant_type=client_credentials
     *
     *     Accept: application/json
     *     Authorization: Base64("${ClientId}:${ClientSecret}")
     *     Content-Type: application/x-www-form-urlencoded
     *
     * </pre>
     *
     * @param clientId     the client id
     * @param clientSecret the client secret
     * @param host         the host server
     * @param port         the port the server is listening on
     */
    public AccessTokenRequest(final String clientId,
                              final String clientSecret,
                              final String host,
                              final int port
                             ) {
        this.uri = DEFAULT_URI;
        String credentials = Objects.requireNonNull(clientId) + ":" + Objects.requireNonNull(clientSecret);
        this.authorizationHeader = Base64.getEncoder()
                                         .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        this.port = port;
        this.host = host;
        this.ssl = true;
    }


    @Override
    public IO<HttpResponse<String>> apply(final MyOauthHttpClient client) {
        var body = "grant_type=client_credentials";
        return Objects.requireNonNull(client).ofString()
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
                                        .uri(URI.create(String.format("%s://%s:%s/%s",
                                                                      ssl ? "https" : "http",
                                                                      host,
                                                                      port,
                                                                      uri
                                                                     )
                                                       )
                                            )
                                        .POST(HttpRequest.BodyPublishers.ofString(body)));

    }


}
