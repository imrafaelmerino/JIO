package jio.chatgpt;

import jio.IO;
import jio.http.client.MyHttpClient;
import jsonvalues.JsObj;



public class ChatService extends AbstractService {


    public ChatService(MyHttpClient client, ConfBuilder builder) {
        super(client, builder, "chat/completions");
    }


    public IO<JsObj> create(ChatBuilder builder) {

        return post(uri,builder.build());


    }
}
