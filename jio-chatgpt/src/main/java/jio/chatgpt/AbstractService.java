package jio.chatgpt;

import jio.IO;
import jio.http.client.MyHttpClient;
import jsonvalues.JsObj;


import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

class AbstractService {

    final byte[] authHeader;
    final MyHttpClient client;
    final URI uri;

    AbstractService(MyHttpClient client, ConfBuilder confBuilder, String resource) {
        APIConf conf = confBuilder.build();
        this.authHeader = conf.authHeader;
        this.client = client;
        this.uri = URI.create(String.format("https://%s/%s/%s", conf.host, conf.version, resource));

    }

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
