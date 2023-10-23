package jio.chatgpt;

import jio.Lambda;
import jio.http.client.JioHttpClientBuilder;
import jsonvalues.JsObj;

import static java.util.Objects.requireNonNull;

/**
 * Service class for interacting with the GPT-based chat API to create chat completions.
 */
public final class ChatService extends AbstractService {

    /**
     * Creates a chat completion by sending a request with the provided builder.
     *
     * @param builder A ChatBuilder instance representing the chat message and parameters.
     * @return An IO monad that may resolve to a JSON object representing the chat completion response.
     */
    public final Lambda<ChatBuilder, JsObj> create;

    /**
     * Creates a new ChatService instance.
     *
     * @param clientBuilder The HTTP client for making requests.
     * @param builder       A configuration builder for the chat service.
     */
    ChatService(JioHttpClientBuilder clientBuilder,
                ConfBuilder builder
               ) {
        super(clientBuilder, builder, "chat/completions");
        create = b -> post(uri, requireNonNull(b).build());

    }

}
