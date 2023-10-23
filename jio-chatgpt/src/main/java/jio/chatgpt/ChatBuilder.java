package jio.chatgpt;

import jsonvalues.*;

import java.util.List;
import java.util.Objects;

import static jio.chatgpt.Constraints.*;
import static jio.chatgpt.DEFAULT_VALUES.*;
import static jio.chatgpt.JSON_FIELDS.*;


/**
 * Builder class for creating chat completions using the GPT model. This class allows you to construct a conversation
 * with messages and configure various parameters for generating responses.
 */
public final class ChatBuilder {
    private final int DEFAULT_MAX_TOKENS = Integer.MAX_VALUE;
    private final String model;
    private final JsArray messages;
    private double frequencyPenalty = DEFAULT_FREQ_PENALTY;
    private double presencePenalty = DEFAULT_PRESENCE_PENALTY;
    private JsArray stop;
    private String user;
    private int maxTokens = DEFAULT_MAX_TOKENS;
    private double temperature = DEFAULT_TEMPERATURE;
    private double topP = DEFAULT_TOP_P;
    private int n = DEFAULT_N_CHOICES;
    private boolean stream = DEFAULT_STREAM;


    private ChatBuilder(String model, JsArray messages) {

        this.model = model;
        this.messages = messages;
    }

    /**
     * Creates a ChatBuilder with the specified GPT model ID and existing conversation messages.
     *
     * @param model    The ID of the GPT model to use. See model endpoint compatibility for details.
     * @param messages A list of messages describing the conversation so far.
     */
    public static ChatBuilder of(final String model,
                                 final List<ChatMessageBuilder> messages
                                ) {
        if (messages.isEmpty()) throw new IllegalArgumentException("messages is empty");

        List<JsObj> chats = Objects.requireNonNull(messages).stream()
                                   .map(ChatMessageBuilder::build)
                                   .toList();
        return new ChatBuilder(Objects.requireNonNull(model),
                               JsArray.ofIterable(chats));
    }


    /**
     * Sets the sampling temperature for generating responses. Higher values make the output more random, while lower
     * values make it more focused and deterministic.
     *
     * @param value The temperature value (Defaults to 1).
     * @return this builder
     */
    public ChatBuilder withTemperature(double value) {
        if (value > MAX_CHAT_COMPLETION_TEMPERATURE)
            throw new IllegalArgumentException("temperature > %d".formatted(MAX_CHAT_COMPLETION_TEMPERATURE));
        if (value < MIN_CHAT_COMPLETION_TEMPERATURE)
            throw new IllegalArgumentException("temperature < " + MIN_CHAT_COMPLETION_TEMPERATURE);
        this.temperature = value;
        return this;
    }


    /**
     * Sets the top_p parameter for generating responses. A value of 0.1 means only the tokens comprising the top 10%
     * probability mass are considered.
     *
     * @param value An alternative to sampling with temperature (Defaults to 1).
     * @return this builder
     */
    public ChatBuilder withTopP(double value) {
        if (value < MIN_TOP_P) throw new IllegalArgumentException("topP < " + MIN_TOP_P);
        if (value > MAX_TOP_P) throw new IllegalArgumentException("topP > " + MAX_TOP_P);
        this.topP = value;
        return this;
    }

    /**
     * Sets the number of chat completion choices to generate for each input message.
     *
     * @param n How many chat completion choices to generate for each input message (Defaults to 1).
     * @return this builder
     */
    public ChatBuilder withNChoices(int n) {
        if (n < MIN_CHOICES)
            throw new IllegalArgumentException("n < %d".formatted(MIN_CHOICES));
        this.n = n;
        return this;
    }

    /**
     * Enables streaming partial progress back as messages are generated. If enabled, partial message deltas
     * are sent with the stream terminated by a data: [DONE] message.
     *
     * @return this builder
     */
    public ChatBuilder withStream() {
        this.stream = true;
        return this;
    }

    /**
     * Sets the text where the API will stop generating further tokens.
     *
     * @param stop Text where the API will stop generating further tokens (Defaults to null).
     * @return this builder
     */
    public ChatBuilder withStop(final List<String> stop) {
        if (Objects.requireNonNull(stop).isEmpty())
            throw new IllegalArgumentException("stop empty");
        if (stop.size() > MAX_SIZE_STOP) {
            throw new IllegalArgumentException("stop size > " + MAX_SIZE_STOP);
        }
        this.stop = JsArray.ofIterable(stop.stream().map(JsStr::of).toList());
        return this;
    }

    /**
     * stop parameter setter
     *
     * @param stop text where the API will stop generating further tokens (Defaults to null)
     * @return this builder
     */
    public ChatBuilder withStop(final String stop) {
        return withStop(List.of(Objects.requireNonNull(stop)));
    }


    /**
     * Sets the maximum number of tokens to generate in the completion. The total length of input tokens and generated
     * tokens is limited by the model's context length.
     *
     * @param maxTokens The maximum number of tokens to generate in the completion (Default to inf).
     * @return this builder
     */
    public ChatBuilder withMaxTokens(int maxTokens) {
        if (maxTokens <= 0) throw new IllegalArgumentException("maxTokens <= 0");
        this.maxTokens = maxTokens;
        return this;
    }

    /**
     * Sets the presence_penalty parameter, which penalizes new tokens based on whether they appear in the text so far.
     * Positive values increase the model's likelihood to talk about new topics.
     *
     * @param value Number between -2.0 and 2.0 (Defaults to 0).
     * @return this builder
     */
    public ChatBuilder withPresencePenalty(double value) {
        if (value < MIN_PRESENCE_PENALTY)
            throw new IllegalArgumentException("presencePenalty < " + MIN_PRESENCE_PENALTY);
        if (value > MAX_PRESENCE_PENALTY)
            throw new IllegalArgumentException("presencePenalty > " + MAX_PRESENCE_PENALTY);
        this.presencePenalty = value;
        return this;
    }

    /**
     * Sets the frequency_penalty parameter, which penalizes new tokens based on their existing frequency in the text so
     * far. Positive values decrease the model's likelihood to repeat the same line verbatim.
     *
     * @param value Number between -2.0 and 2.0 (Defaults to 0).
     * @return this builder
     */
    public ChatBuilder withFrequencyPenalty(double value) {
        if (value < MIN_FREQ_PENALTY)
            throw new IllegalArgumentException("frequencyPenalty < " + MIN_FREQ_PENALTY);
        if (value > MAX_FREQ_PENALTY)
            throw new IllegalArgumentException("frequencyPenalty > " + MAX_FREQ_PENALTY);
        this.frequencyPenalty = value;
        return this;
    }


    /**
     * Sets a unique identifier representing your end-user, which can help OpenAI to monitor and detect abuse.
     *
     * @param user A unique identifier representing your end-user.
     * @return this builder
     */
    public ChatBuilder withUser(String user) {
        this.user = Objects.requireNonNull(user);
        return this;
    }


    public JsObj build() {
        JsObj obj = JsObj.of(MODEL_FIELD, JsStr.of(model),
                             MESSAGES_FIELD, messages);
        if (stop != null)
            obj = obj.set(STOP_FIELD, stop);
        if (maxTokens != DEFAULT_MAX_TOKENS)
            obj = obj.set(MAX_TOKENS_FIELD, JsInt.of(maxTokens));
        if (temperature != DEFAULT_TEMPERATURE)
            obj = obj.set(TEMPERATURE_FIELD, JsDouble.of(temperature));
        if (topP != DEFAULT_TOP_P)
            obj = obj.set(TOP_P_FIELD, JsDouble.of(topP));
        if (n != DEFAULT_N_CHOICES)
            obj = obj.set(N_FIELD, JsInt.of(n));
        if (stream)
            obj = obj.set(STREAM_FIELD, JsBool.TRUE);
        if (presencePenalty != DEFAULT_PRESENCE_PENALTY)
            obj = obj.set(PRESENCE_PENALTY_FIELD, JsDouble.of(presencePenalty));
        if (frequencyPenalty != DEFAULT_FREQ_PENALTY)
            obj = obj.set(FREQ_PENALTY_FIELD, JsDouble.of(frequencyPenalty));
        if (user != null)
            obj = obj.set(USER_FIELD, JsStr.of(user));

        return obj;

    }


}
