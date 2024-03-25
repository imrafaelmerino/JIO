package jio.chatgpt;

import jio.BiLambda;
import jio.http.client.JioHttpClientBuilder;
import jsonvalues.JsObj;
import jsonvalues.JsStr;

import java.util.Objects;

/**
 * Service for performing content moderation.
 */
public final class ModerationService extends AbstractService {

    /**
     * Creates a content moderation request.
     *
     * @param input The content to be moderated.
     * @param model The ID of the model to use for moderation.
     * @return An IO (monadic) object representing the asynchronous result of the moderation request.
     */
    public final BiLambda<String, String, JsObj> create;


    /**
     * Creates a ModerationService instance with the specified HTTP client and configuration builder.
     *
     * @param clientBuilder The HTTP client used for making requests.
     * @param builder       The configuration builder for this service.
     */
    ModerationService(final JioHttpClientBuilder clientBuilder,
                      final ConfBuilder builder
                     ) {
        super(clientBuilder,
              builder,
              "moderations"
             );
        this.create = (input, model) ->
                post(uri,
                     JsObj.of("model", JsStr.of(Objects.requireNonNull(model)),
                              "input", JsStr.of(Objects.requireNonNull(input))
                             )
                    );
    }


}
