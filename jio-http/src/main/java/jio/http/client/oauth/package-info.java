/**
 * The {@code jio.http.client.oauth} package extends the functionality of the {@code jio.http.client} package to include
 * OAuth authentication support. It provides classes and interfaces for creating HTTP clients that can handle OAuth 2.0
 * Client Credentials Grant flow. Key classes and interfaces in this package include:
 * <ul>
 * <li>{@link jio.http.client.oauth.ClientCredentialsClient}: A specialized HTTP client that handles OAuth
 * authentication using the Client Credentials Grant flow. It automatically manages access tokens, refreshing them when
 * necessary.</li>
 * <li>{@link jio.http.client.oauth.ClientCredentialsBuilder}: A builder class for creating instances of
 * {@code ClientCredentialsHttpClient} with customizable settings for access token requests, token parsing, and token
 * refresh conditions.</li>
 * <li>{@link jio.http.client.oauth.AccessTokenRequest}: A function that sends predefined requests to obtain access
 * tokens from an OAuth server. It includes options for specifying client credentials, host, port, URI, and
 * protocol.</li>
 * <li>{@link jio.http.client.oauth.GetAccessToken}: A lambda function that parses access tokens from HTTP responses and
 * is used internally by the {@code ClientCredentialsHttpClient}.</li>
 * </ul>
 * This package enables developers to create HTTP clients that are capable of handling OAuth authentication seamlessly,
 * ensuring that access tokens are automatically managed and refreshed as needed.
 */

package jio.http.client.oauth;