package jio.chatgpt;

import jio.IO;
import jio.http.client.MyHttpClient;
import jsonvalues.JsObj;

public class ModelService extends AbstractService {

    public ModelService(MyHttpClient client, ConfBuilder builder) {
        super(client, builder, "models");
    }


    public IO<JsObj> list() {
        return get(uri);
    }

    public IO<JsObj> retrieve(String model) {
        return get(uri.resolve("/" + model));
    }

}
