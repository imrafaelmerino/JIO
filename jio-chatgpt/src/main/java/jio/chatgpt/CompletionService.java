package jio.chatgpt;

import jio.Lambda;
import jio.http.client.JioHttpClientBuilder;
import jsonvalues.JsObj;

import java.util.Objects;

/**
 * A service class for interacting with the GPT-3 API to create text completions. This service provides a convenient way
 * to create completions using the provided builder.
 */
public final class CompletionService extends AbstractService {

    /**
     * Create a text completion using the specified builder.
     *
     * @param builder The CompletionBuilder instance configured with the desired parameters for text completion.
     * @return An IO (I/O) monad representing the asynchronous result of the API call, which can be used to obtain the
     * completion response.
     */
    public final Lambda<CompletionBuilder, JsObj> create;

    /**
     * Creates a new CompletionService instance.
     *
     * @param clientBuilder The HTTP client for making API requests.
     * @param builder       The configuration builder used for setting up the service.
     */
    CompletionService(JioHttpClientBuilder clientBuilder,
                      ConfBuilder builder
                     ) {

        super(clientBuilder, builder, "completions");
        create = b -> post(uri, Objects.requireNonNull(b).build());
        ;
    }


}
