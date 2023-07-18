package jio.chatgpt;


import jio.IO;
import jio.http.client.MyHttpClient;
import jsonvalues.JsObj;


public class ImageService extends AbstractService {
    public ImageService(MyHttpClient client, ConfBuilder builder) {
        super(client, builder, "images");
    }


    public IO<JsObj> create(ImageBuilder builder) {
        return post(uri.resolve("/" + "generations"),
                    builder.build()
                   );
    }

    public IO<JsObj> createEdit(EditImageBuilder builder) {
        return post(uri.resolve("/" + "edits"),
                    builder.build()
                   );
    }

    public IO<JsObj> createVariation(VariationImageBuilder builder) {
        return post(uri.resolve("/" + "variations"),
                    builder.build()
                   );
    }
}
