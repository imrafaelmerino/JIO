package jio.http.client.oauth;

import java.util.Objects;

/**
 * Exception that represents that the oauth access token is not found.
 */
@SuppressWarnings("serial")
public final class AccessTokenNotFound extends Exception {

  AccessTokenNotFound(String message) {
    super(Objects.requireNonNull(message));
  }

  AccessTokenNotFound(String message,
                      Throwable cause
  ) {
    super(Objects.requireNonNull(message),
          Objects.requireNonNull(cause)
    );
  }
}
