package jio.chatgpt;

import jio.http.client.MyHttpClientBuilder;

import java.io.IOException;
import java.net.http.HttpClient;

public class FineTuner {


    public static void main(String[] args) throws IOException {

        try {
            String secret = "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";

            var client =
                    new MyHttpClientBuilder(HttpClient.newHttpClient())
                            .create();



            // Response
            // {"filename":"json-values-training.jsonl","created_at":1683901794,"bytes":1663,"status":"uploaded","status_details":null,"id":"file-W7zyps93AjwtqWkjWV2hxeWX","purpose":"fine-tune","object":"file"}
        } catch (Exception e) {
            System.out.println(e.getCause());
        }


    }

}

