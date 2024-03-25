package jio.http.client;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import jio.Lambda;

/**
 * Represents a lambda function that takes an HTTP request builder and returns an HTTP response.
 *
 * @param <RespBody> The type of the response body.
 */
public interface HttpLambda<RespBody> extends Lambda<HttpRequest.Builder, HttpResponse<RespBody>> {

}
