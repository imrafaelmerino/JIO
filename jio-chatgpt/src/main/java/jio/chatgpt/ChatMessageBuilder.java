package jio.chatgpt;

import jsonvalues.JsObj;
import jsonvalues.JsStr;

import java.util.Objects;


/**
 * builder to create chat messages.
 */
public class ChatMessageBuilder {

    private final Data.ROLE role;
    private final String content;
    private String name;


    /**
     *
     * @param role The role of the author of this message
     * @param content The contents of the message.
     */
    public ChatMessageBuilder(Data.ROLE role, String content) {
        this.role = role;
        this.content = content;
    }

    /**
     *
     * @param name The name of the author of this message. May contain a-z, A-Z, 0-9, and underscores, with a maximum length of 64 characters.
     * @return this builder
     */
    public ChatMessageBuilder setName(String name) {
        this.name = Objects.requireNonNull(name);
        return this;
    }

    public JsObj build() {
        JsObj message = JsObj.of(JSON_FIELDS.ROLE_FIELD, JsStr.of(role.name()),
                                 JSON_FIELDS.CONTENT_FIELD, JsStr.of(content)
                                );
        if (name != null && !name.isBlank())
            message = message.set(JSON_FIELDS.NAME_FIELD, JsStr.of(name));
        return message;
    }
}
