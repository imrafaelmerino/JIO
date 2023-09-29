package jio.chatgpt;

import jio.IO;
import jio.http.client.MyHttpClient;
import jsonvalues.JsObj;


import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
/**
 * An abstract base class for GPT API services providing common HTTP request methods.
 */
class AbstractService {

    final byte[] authHeader;
    final MyHttpClient client;
    final URI uri;
    /**
     * Constructs an AbstractService with the provided HTTP client, configuration builder, and resource path.
     *
     * @param client      The HTTP client used for making API requests.
     * @param confBuilder The configuration builder for API settings.
     * @param resource    The resource path specific to the GPT API service.
     */
    AbstractService(MyHttpClient client, ConfBuilder confBuilder, String resource) {
        APIConf conf = confBuilder.build();
        this.authHeader = conf.authHeader;
        this.client = client;
        this.uri = URI.create(String.format("https://%s/%s/%s", conf.host, conf.version, resource));

    }
    /**
     * Handles the response from an API request and parses it as JSON.
     * If the response status code is below 300, it succeeds with the parsed JSON; otherwise, it fails with an APIError.
     *
     * @param resp The HTTP response received from the API request.
     * @return An IO monad that represents the parsed JSON result or an APIError in case of failure.
     */
    static IO<JsObj> errorHandler(HttpResponse<String> resp) {
        if (resp.statusCode() < 300) return IO.succeed(JsObj.parse(resp.body()));
        return IO.fail(new APIError(resp));
    }

    IO<JsObj> post(URI uri, JsObj body) {
        return post(uri, body.toString(), "application/json");
    }

    IO<JsObj> post(URI uri, String body, String contentType) {
        return client.ofString()
                     .apply(HttpRequest.newBuilder()
                                       .uri(uri)
                                       .header("Content-Type", contentType)
                                       .header("Authorization", "Bearer " + new String(authHeader, StandardCharsets.UTF_8))
                                       .POST(HttpRequest.BodyPublishers.ofString(body))
                           )
                     .then(AbstractService::errorHandler);
    }

    IO<JsObj> delete(URI uri) {
        return client.ofString()
                     .apply(HttpRequest.newBuilder()
                                       .uri(uri)
                                       .header("Authorization", "Bearer " + new String(authHeader, StandardCharsets.UTF_8))
                                       .DELETE()
                           )
                     .then(AbstractService::errorHandler);
    }

    IO<JsObj> post(URI uri) {
        return client.ofString()
                     .apply(HttpRequest.newBuilder()
                                       .uri(uri)
                                       .header("Content-Type", "application/json")
                                       .header("Authorization", "Bearer " + new String(authHeader, StandardCharsets.UTF_8))
                                       .POST(HttpRequest.BodyPublishers.noBody())
                           )
                     .then(AbstractService::errorHandler);
    }

    IO<JsObj> get(URI uri) {
        return client.ofString()
                     .apply(HttpRequest.newBuilder()
                                       .uri(uri)
                                       .header("Authorization", "Bearer " + new String(authHeader, StandardCharsets.UTF_8))
                                       .GET()
                           )
                     .then(AbstractService::errorHandler);
    }
}
