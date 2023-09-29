package jio.chatgpt;

import jio.IO;
import jio.http.client.MyHttpClient;
import jsonvalues.JsObj;
import jsonvalues.JsStr;
/**
 * Service for performing content moderation.
 */
public final  class ModerationService extends AbstractService {

    /**
     * Creates a ModerationService instance with the specified HTTP client and configuration builder.
     *
     * @param client  The HTTP client used for making requests.
     * @param builder The configuration builder for this service.
     */
    public ModerationService(MyHttpClient client, ConfBuilder builder) {
        super(client, builder, "moderations");
    }
    /**
     * Creates a content moderation request.
     *
     * @param input The content to be moderated.
     * @param model The ID of the model to use for moderation.
     * @return An IO (monadic) object representing the asynchronous result of the moderation request.
     */
    public IO<JsObj> create(String input, String model) {
        return post(uri,
                    JsObj.of("model", JsStr.of(model),
                             "input", JsStr.of(input)
                            )
                   );
    }

}
