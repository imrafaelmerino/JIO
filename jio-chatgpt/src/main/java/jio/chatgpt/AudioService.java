package jio.chatgpt;


import jio.Lambda;
import jio.http.client.JioHttpClientBuilder;
import jsonvalues.JsObj;


/**
 * A service class for working with audio-related tasks using an API.
 */
public final class AudioService extends AbstractService {

    /**
     * Creates a transcription job using the provided TranscriptionBuilder.
     *
     * @param builder The TranscriptionBuilder containing parameters for the transcription job.
     * @return An IO containing the result of the transcription job as a JsObj.
     */
    public final Lambda<TranscriptionBuilder, JsObj> createTranscription;

    /**
     * Creates a translation job using the provided TranslationBuilder.
     *
     * @param builder The TranslationBuilder containing parameters for the translation job.
     * @return An IO containing the result of the translation job as a JsObj.
     */
    public final Lambda<TranslationBuilder, JsObj> createTranslation;

    /**
     * Constructs an AudioService instance with the specified client and configuration builder.
     *
     * @param clientBuilder The HTTP client used for making API requests.
     * @param builder       The configuration builder for API settings.
     */
    AudioService(JioHttpClientBuilder clientBuilder,
                 ConfBuilder builder
                ) {
        super(clientBuilder, builder, "audio");
        createTranscription = tb -> post(uri.resolve("/transcriptions"),
                                         tb.build());
        createTranslation = tb -> post(uri.resolve("/translations"),
                                       tb.build());
    }

}

