package jio.chatgpt;

import jio.IO;
import jio.http.client.MyHttpClient;
import jio.http.client.MyHttpClientBuilder;
import jsonvalues.JsObj;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;

public class Examples {


    public static void main(String[] args) throws IOException {

        try {

            String secret = "";

            MyHttpClient client =
                    new MyHttpClientBuilder(HttpClient.newHttpClient())
                            .create();

            Services services = new Services(new ConfBuilder(secret.getBytes(StandardCharsets.UTF_8)), client);

            IO<JsObj> files = services.fileService.list();

            System.out.println(files.join());



            // Response
            // {"filename":"json-values-training.jsonl","created_at":1683901794,"bytes":1663,"status":"uploaded","status_details":null,"id":"file-W7zyps93AjwtqWkjWV2hxeWX","purpose":"fine-tune","object":"file"}
        } catch (Exception e) {
            System.out.println(e.getCause());
        }


    }

}

