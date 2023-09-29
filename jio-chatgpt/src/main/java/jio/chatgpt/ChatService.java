package jio.chatgpt;

import jio.IO;
import jio.http.client.MyHttpClient;
import jsonvalues.JsObj;

/**
 * Service class for interacting with the GPT-based chat API to create chat completions.
 */
public  final class ChatService extends AbstractService {

    /**
     * Creates a new ChatService instance.
     *
     * @param client  The HTTP client for making requests.
     * @param builder A configuration builder for the chat service.
     */
    public ChatService(MyHttpClient client, ConfBuilder builder) {
        super(client, builder, "chat/completions");
    }

    /**
     * Creates a chat completion by sending a request with the provided builder.
     *
     * @param builder A ChatBuilder instance representing the chat message and parameters.
     * @return An IO monad that may resolve to a JSON object representing the chat completion response.
     */
    public IO<JsObj> create(ChatBuilder builder) {

        return post(uri, builder.build());


    }
}
