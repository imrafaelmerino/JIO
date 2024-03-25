package jio.chatgpt;


import java.net.http.HttpResponse;

/**
 * Custom exception class for representing errors that occur during API requests.
 */
public final class APIError extends RuntimeException {

    /**
     * The HTTP response associated with the error.
     */
    public final HttpResponse<String> resp;

    /**
     * Constructs an API error with the provided HTTP response.
     *
     * @param resp The HTTP response associated with the error.
     */
    public APIError(HttpResponse<String> resp) {
        this.resp = resp;
    }

    /**
     * Returns a string representation of the API error, including the HTTP status code and response body.
     *
     * @return A string representation of the API error.
     */
    @Override
    public String toString() {
        return String.format("status_code: %s, body: %s", resp.statusCode(), resp.body());
    }
}

