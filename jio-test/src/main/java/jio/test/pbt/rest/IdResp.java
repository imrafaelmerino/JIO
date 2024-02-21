package jio.test.pbt.rest;

import java.net.http.HttpResponse;

record IdResp(String id,
              HttpResponse<String> resp) {

}
