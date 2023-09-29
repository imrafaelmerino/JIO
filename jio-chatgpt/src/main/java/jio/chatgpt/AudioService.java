package jio.chatgpt;


import jio.IO;
import jio.http.client.MyHttpClient;
import jsonvalues.JsObj;


/**
 * A service class for working with audio-related tasks using an API.
 */
public  final  class AudioService extends AbstractService {

    /**
     * Constructs an AudioService instance with the specified client and configuration builder.
     *
     * @param client   The HTTP client used for making API requests.
     * @param builder  The configuration builder for API settings.
     */
    public AudioService(MyHttpClient client, ConfBuilder builder) {
        super(client, builder, "audio");
    }

    /**
     * Creates a transcription job using the provided TranscriptionBuilder.
     *
     * @param builder The TranscriptionBuilder containing parameters for the transcription job.
     * @return An IO containing the result of the transcription job as a JsObj.
     */
    public IO<JsObj> createTranscription(TranscriptionBuilder builder) {
        return post(uri.resolve("/transcriptions"), builder.build());
    }

    /**
     * Creates a translation job using the provided TranslationBuilder.
     *
     * @param builder The TranslationBuilder containing parameters for the translation job.
     * @return An IO containing the result of the translation job as a JsObj.
     */
    public IO<JsObj> createTranslation(TranslationBuilder builder) {
        return post(uri.resolve("/translations"), builder.build());
    }
}

