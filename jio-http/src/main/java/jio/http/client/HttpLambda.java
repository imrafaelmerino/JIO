package jio.http.client;


import jio.Lambda;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * lambda that takes a request builder and returns a http response
 *
 * @param <R> the response body type
 */
public interface HttpLambda<R> extends Lambda<HttpRequest.Builder, HttpResponse<R>> {
}
