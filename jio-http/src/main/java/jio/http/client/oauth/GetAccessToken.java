package jio.http.client.oauth;

import jio.IO;
import jio.Lambda;
import jsonvalues.JsObj;
import jsonvalues.spec.JsParserException;
import jsonvalues.JsPath;

import java.net.http.HttpResponse;
import java.util.Objects;

/**
 * Lambda that takes the access token request response, parses into a JsObj and returns the access token located at the
 * field access_token. If the token is not found the lambda fails with the exception {@link AccessTokenNotFound} There
 * is no public constructors to create instances of this class. Use the singleton {@link #DEFAULT}
 *
 * @see ClientCredentialsBuilder
 */
public final class GetAccessToken implements Lambda<HttpResponse<String>, String> {

  /**
   * singleton of this class
   */
  public static final GetAccessToken DEFAULT = new GetAccessToken();
  private static final JsPath ACCESS_TOKEN_PATH = JsPath.empty()
                                                        .key("access_token");

  private GetAccessToken() {
  }

  @Override
  public IO<String> apply(final HttpResponse<String> resp) {
    var body = Objects.requireNonNull(resp)
                      .body();
    try {
      var json = JsObj.parse(body);
      var token = json.getStr(ACCESS_TOKEN_PATH);
      if (token == null || token.isBlank()) {
        return IO.fail(new AccessTokenNotFound(String.format("Response: %s. Expected a string located at the path: %s.",
                                                             body,
                                                             ACCESS_TOKEN_PATH
        )
        )
        );
      }
      return IO.succeed(token);
    } catch (JsParserException malformedJson) {
      return IO.fail(new AccessTokenNotFound("A JsObj body response was expected. Received: " + body));
    } catch (Exception e) {
      return IO.fail(new AccessTokenNotFound("Exception while reading access token from response.",
                                             e));
    }

  }
}
