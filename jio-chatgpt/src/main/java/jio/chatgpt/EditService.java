package jio.chatgpt;


import jio.IO;
import jio.http.client.MyHttpClient;
import jsonvalues.JsObj;

import java.util.Objects;

/**
 * Service for creating edits using the GPT model. This service allows you to send an instruction to the model, which
 * will return an edited version of the provided text.
 */
public final class EditService extends AbstractService {

    /**
     * Creates an EditService instance with the specified HTTP client and configuration builder.
     *
     * @param client  The HTTP client used for making requests.
     * @param builder The configuration builder for this service.
     */
    EditService(MyHttpClient client, ConfBuilder builder) {
        super(client, builder, "edits");
    }

    /**
     * Creates an edit based on the provided builder configuration.
     *
     * @param builder The EditBuilder instance containing the model and instruction for editing.
     * @return An IO (monadic) object representing the asynchronous result of the edit creation request.
     */
    public IO<JsObj> create(EditBuilder builder) {
        return post(uri, Objects.requireNonNull(builder).build());
    }

}
