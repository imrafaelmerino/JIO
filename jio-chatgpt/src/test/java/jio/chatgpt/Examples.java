package jio.chatgpt;

import jio.IO;
import jio.http.client.JioHttpClientBuilder;
import jsonvalues.JsObj;

import java.io.IOException;
import java.net.http.HttpClient;

public class Examples {


    public static void main(String[] args) throws IOException {

        try {

            String secret = "";

            JioHttpClientBuilder builder =
                    JioHttpClientBuilder.of(HttpClient.newBuilder());

            ChatGPTServices services =
                    ChatGPTServices.of(ConfBuilder.of(secret),
                                       builder);

            IO<JsObj> files = services.file.list.get();

            System.out.println(files.result());


            // Response
            // {"filename":"json-values-training.jsonl","created_at":1683901794,"bytes":1663,"status":"uploaded","status_details":null,"id":"file-W7zyps93AjwtqWkjWV2hxeWX","purpose":"fine-tune","object":"file"}
        } catch (Exception e) {
            System.out.println(e.getCause());
        }


    }

}

