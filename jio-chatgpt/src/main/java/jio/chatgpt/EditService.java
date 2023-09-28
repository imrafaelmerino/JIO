package jio.chatgpt;


import jio.IO;
import jio.http.client.MyHttpClient;
import jsonvalues.JsObj;


public class EditService extends AbstractService {


    public EditService(MyHttpClient client, ConfBuilder builder) {
        super(client, builder, "edits");
    }

    public IO<JsObj> create(EditBuilder builder) {
        return post(uri, builder.build());
    }

}
