package jio.chatgpt;

import jsonvalues.JsObj;
import jsonvalues.JsStr;

import java.util.Objects;


/**
 * Builder class for creating chat messages.
 */
public final class ChatMessageBuilder {

    private final Data.ROLE role;
    private final String content;
    private String name;


    /**
     * Creates a ChatMessageBuilder with the specified role and content.
     *
     * @param role    The role of the author of this message.
     * @param content The contents of the message.
     */
    public ChatMessageBuilder(Data.ROLE role, String content) {
        this.role = role;
        this.content = content;
    }

    /**
     * Sets the name of the author of this message.
     *
     * @param name The name of the author of this message.
     * @return this builder
     */
    public ChatMessageBuilder setName(String name) {
        this.name = Objects.requireNonNull(name);
        return this;
    }

    /**
     * Builds and returns a JSON object representing the chat message.
     *
     * @return A JSON object representing the chat message.
     */
    public JsObj build() {
        JsObj message = JsObj.of(JSON_FIELDS.ROLE_FIELD, JsStr.of(role.name()),
                                 JSON_FIELDS.CONTENT_FIELD, JsStr.of(content)
                                );
        if (name != null && !name.isBlank())
            message = message.set(JSON_FIELDS.NAME_FIELD, JsStr.of(name));
        return message;
    }
}
