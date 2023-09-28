package jio.chatgpt;

import jio.IO;
import jio.http.client.MyHttpClient;
import jsonvalues.JsObj;


public class CompletionService extends AbstractService {


    public CompletionService(MyHttpClient client, ConfBuilder builder) {
        super(client, builder, "completions");
    }


    public IO<JsObj> create(CompletionBuilder builder) {

        return post(uri, builder.build());


    }
}
