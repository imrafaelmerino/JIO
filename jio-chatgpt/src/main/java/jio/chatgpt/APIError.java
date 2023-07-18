package jio.chatgpt;


import java.net.http.HttpResponse;

public final class APIError extends RuntimeException{

    public final HttpResponse<String> resp;

    public APIError(HttpResponse<String> resp){
        this.resp = resp;
    }

    @Override
    public String toString() {
        return String.format("status_code: %s, body: %s",resp.statusCode(),resp.body());
    }
}
