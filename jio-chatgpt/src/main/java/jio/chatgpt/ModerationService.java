package jio.chatgpt;

import jio.IO;
import jio.http.client.MyHttpClient;
import jsonvalues.JsObj;
import jsonvalues.JsStr;

public class ModerationService extends AbstractService {


    public ModerationService(MyHttpClient client, ConfBuilder builder) {
        super(client, builder, "moderations");
    }

    public IO<JsObj> create(String input, String model) {
        return post(uri,
                    JsObj.of("model", JsStr.of(model),
                             "input", JsStr.of(input)
                            )
                    );
    }

}
