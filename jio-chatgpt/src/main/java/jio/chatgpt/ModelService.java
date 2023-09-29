package jio.chatgpt;

import jio.IO;
import jio.http.client.MyHttpClient;
import jsonvalues.JsObj;
/**
 * Service for interacting with models.
 */
public  final class ModelService extends AbstractService {
    /**
     * Creates a ModelService instance with the specified HTTP client and configuration builder.
     *
     * @param client  The HTTP client used for making requests.
     * @param builder The configuration builder for this service.
     */
    public ModelService(MyHttpClient client, ConfBuilder builder) {
        super(client, builder, "models");
    }

    /**
     * Lists available models.
     *
     * @return An IO (monadic) object representing the asynchronous result of the model listing request.
     */
    public IO<JsObj> list() {
        return get(uri);
    }
    /**
     * Retrieves information about a specific model.
     *
     * @param model The ID of the model to retrieve information about.
     * @return An IO (monadic) object representing the asynchronous result of the model retrieval request.
     */
    public IO<JsObj> retrieve(String model) {
        return get(uri.resolve("/" + model));
    }

}
