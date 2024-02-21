package jio.http.client.oauth;

/**
 * This exception happens when the predicate to check when refreshing the oauth token, that is specified in
 * {@link ClientCredentialsBuilder}, is evaluated to true for a predefined number of times in a row, producing the
 * following loop:
 *
 * <pre>
 *
 * GET token http request
 * refreshToken = true
 *
 * GET token http request
 * refreshToken = true
 *
 * GET token http request
 * refreshToken = true
 *
 * AND so on
 *
 *
 *
 * It could happen in the following scenario:
 *
 * http client -> API GATEWAY -> BACKEND
 *
 * and the refreshTokenPredicates only checks the status code to see if the access token need to be refreshed:
 *
 * {@code Predicate<HttpResponse<?>> pred = resp -> resp.statusCode() == 401; }
 * </pre>
 * In this scenario you'll get this exception if the BACKEND returns the status code 401. The problem is that the
 * refreshTokenPredicates only checks the status code and cant distinguish a regular 401 from the backend from a 401
 * from the API gateway asking you to refresh the token. You need to be more specific and also take into account a
 * header or the body
 */
@SuppressWarnings("serial")
public final class RefreshTokenLoop extends Exception {

  RefreshTokenLoop(int n) {
    super(STR."The refresh token predicate has been evaluated to true for \{n} times in a row. It couldbe an error on its implementation because it's returning true to ask for a new access tokenwhen it shouldn't.");
  }
}
