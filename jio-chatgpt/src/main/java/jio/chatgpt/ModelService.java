package jio.chatgpt;

import jio.IO;
import jio.Lambda;
import jio.http.client.JioHttpClientBuilder;
import jsonvalues.JsObj;

import java.util.function.Supplier;

/**
 * Service for interacting with models.
 */
public final class ModelService extends AbstractService {
    /**
     * Lists available models.
     *
     * @return An IO (monadic) object representing the asynchronous result of the model listing request.
     */
    public final Supplier<IO<JsObj>> list;
    /**
     * Retrieves information about a specific model.
     *
     * @param model The ID of the model to retrieve information about.
     * @return An IO (monadic) object representing the asynchronous result of the model retrieval request.
     */
    public final Lambda<String, JsObj> retrieve;

    /**
     * Creates a ModelService instance with the specified HTTP client and configuration builder.
     *
     * @param clientBuilder The HTTP client used for making requests.
     * @param builder       The configuration builder for this service.
     */
    ModelService(JioHttpClientBuilder clientBuilder,
                 ConfBuilder builder
                ) {

        super(clientBuilder, builder, "models");
        this.retrieve = model -> get(uri.resolve("/" + model));
        this.list = () -> get(uri);
    }


}
