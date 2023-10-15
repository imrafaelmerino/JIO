    package jio.http.client;


    import jio.Lambda;

    import java.net.http.HttpRequest;
    import java.net.http.HttpResponse;

    /**
     * Represents a lambda function that takes an HTTP request builder and returns an HTTP response.
     *
     * @param <R> The type of the response body.
     */
    public interface HttpLambda<R> extends Lambda<HttpRequest.Builder, HttpResponse<R>> {
    }
