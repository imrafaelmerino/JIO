package jio.chatgpt;

import jio.IO;
import jio.http.client.MyHttpClient;
import jsonvalues.JsObj;

import java.util.Objects;

/**
 * A service class for interacting with the GPT-3 API to create text completions.
 * This service provides a convenient way to create completions using the provided builder.
 */
public  final  class CompletionService extends AbstractService {

    /**
     * Creates a new CompletionService instance.
     *
     * @param client  The HTTP client for making API requests.
     * @param builder The configuration builder used for setting up the service.
     */
    public CompletionService(MyHttpClient client, ConfBuilder builder) {
        super(client, builder, "completions");
    }

    /**
     * Create a text completion using the specified builder.
     *
     * @param builder The CompletionBuilder instance configured with the desired parameters for text completion.
     * @return An IO (I/O) monad representing the asynchronous result of the API call, which can be used to obtain the completion response.
     */
    public IO<JsObj> create(CompletionBuilder builder) {

        return post(uri, Objects.requireNonNull(builder).build());


    }
}
