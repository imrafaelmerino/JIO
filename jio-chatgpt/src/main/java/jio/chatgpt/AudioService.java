package jio.chatgpt;


import jio.IO;
import jio.http.client.MyHttpClient;
import jsonvalues.JsObj;


public class AudioService extends AbstractService {

    public AudioService(MyHttpClient client, ConfBuilder builder) {
        super(client, builder, "audio");
    }

    public IO<JsObj> createTranscription(TranscriptionBuilder builder) {
        return post(uri.resolve("/" + "transcriptions"), builder.build());
    }

    public IO<JsObj> createTranslation(TranslationBuilder builder) {
        return post(uri.resolve("/" + "translations"), builder.build());
    }

}
