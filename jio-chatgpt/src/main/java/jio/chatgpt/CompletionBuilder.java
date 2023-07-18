package jio.chatgpt;

import java.util.Objects;
import java.util.OptionalInt;


import jsonvalues.*;

import static jio.chatgpt.Constraints.*;
import static jio.chatgpt.DEFAULT_VALUES.*;
import static jio.chatgpt.JSON_FIELDS.*;


/**
 * Builder to create completions.
 * Given a prompt, the model will return one or more predicted completions, and can also return the probabilities of
 * alternative tokens at each position.
 */
public class CompletionBuilder {

    private static final int DEFAULT_MAX_TOKEN = 16;
    private final String model;
    private final JsArray prompt;

    private String suffix;
    private int maxTokens;
    private double temperature;
    private double topP;
    private int n;
    private boolean stream;
    private OptionalInt logprobs;
    private boolean echo;
    private double presencePenalty;
    private double frequencyPenalty;
    private int bestOf;
    private String user;
    private JsArray stop;


    /**
     * @param model  ID of the model to use. You can use the List models API to see all of your available models, or see our Model overview for descriptions of them.
     * @param prompt The prompt to generate completions for, encoded as a string
     */
    public CompletionBuilder(String model, String prompt) {
        this(model, JsArray.of(prompt));
    }

    /**
     * @param model  ID of the model to use. You can use the List models API to see all of your available models, or see our Model overview for descriptions of them.
     * @param prompt The prompts to generate completions for, encoded as an array of strings
     */
    public CompletionBuilder(String model, JsArray prompt) {
        this.model = Objects.requireNonNull(model);
        this.prompt = Objects.requireNonNull(prompt);
        this.maxTokens = DEFAULT_MAX_TOKEN;
        this.temperature = DEFAULT_TEMPERATURE;
        this.topP = DEFAULT_TOP_P;
        this.n = DEFAULT_NUMBER_CHOICES;
        this.presencePenalty = DEFAULT_PRESENCE_PENALTY;
        this.frequencyPenalty = DEFAULT_FREQ_PENALTY;
        this.bestOf = DEFAULT_BEST_OF;
        this.logprobs = OptionalInt.empty();
    }

    /**
     * suffix parameter setter
     *
     * @param suffix The suffix that comes after a completion of inserted text.
     * @return this builder
     */
    public CompletionBuilder setSuffix(String suffix) {
        this.suffix = Objects.requireNonNull(suffix);
        return this;
    }


    /**
     * max_tokens parameter setter
     * The total length of input tokens and generated tokens is limited by the model's context length.
     *
     * @param maxTokens The maximum number of tokens to generate in the completion (Default to 16)
     * @return this builder
     */
    public CompletionBuilder setMaxTokens(int maxTokens) {
        if (maxTokens <= 0) throw new IllegalArgumentException("maxTokens <= 0");
        this.maxTokens = maxTokens;
        return this;
    }

    /**
     * temperature parameter setter.
     * Higher values like 0.8 will make the output more random,
     * while lower values like 0.2 will make it more focused and deterministic.
     * <p>
     * We generally recommend altering this or top_p but not both.
     *
     * @param value What sampling temperature to use, between 0 and 2. (Defaults to 1)
     * @return this builder
     */
    public CompletionBuilder setTemperature(double value) {
        if (value > MAX_COMPLETION_TEMPERATURE) throw new IllegalArgumentException("temperature > "+MAX_COMPLETION_TEMPERATURE);
        if (value < MIN_COMPLETION_TEMPERATURE) throw new IllegalArgumentException("temperature < "+MIN_COMPLETION_TEMPERATURE);
        this.temperature = value;
        return this;
    }

    /**
     * top_p parameter setter.
     * 0.1 means only the tokens comprising the top 10% probability mass
     * are considered.
     * <p>
     * We generally recommend altering this or temperature but not both.
     *
     * @param value An alternative to sampling with temperature, called nucleus sampling, where the
     *              model considers the results of  the tokens with top_p probability mass. (Defaults to 1)
     * @return this builder
     */
    public CompletionBuilder setTopP(double value) {
        if (value > MAX_COMPLETION_TOP_P) throw new IllegalArgumentException("topP > "+ MAX_COMPLETION_TOP_P);
        if (value < MIN_COMPLETION_TOP_P) throw new IllegalArgumentException("topP < "+ MIN_COMPLETION_TOP_P);
        this.topP = value;
        return this;
    }

    /**
     * n parameter builder
     *
     * @param n How many chat completion choices to generate for each input message (Defaults to 1)
     * @return this builder
     */
    public CompletionBuilder setNChoices(int n) {
        if (n < MIN_COMPLETION_CHOICES) throw new IllegalArgumentException("n < "+ MIN_COMPLETION_CHOICES);
        this.n = n;
        return this;
    }


    /**
     * If set, partial message deltas will be sent, like in ChatGPT. Tokens will be sent as data-only server-sent
     * events as they become available, with the stream terminated by a data: [DONE] message
     *
     * @param stream Whether to stream back partial progress (Defaults to false)
     * @return this builder
     */
    public CompletionBuilder setStream(boolean stream) {
        this.stream = stream;
        return this;
    }

    /**
     * logprobs parameter setter.
     * For example, if logprobs is 5, the API will return a list of the 5 most likely tokens. The API will
     * always return the logprob of the sampled token, so there may be up to logprobs+1 elements in the response.
     * <p>
     * The maximum value for logprobs is 5. If you need more than this, please contact OpenAI through their Help center and describe your use case.
     *
     * @param logprobs Include the log probabilities on the logprobs most likely tokens, as well the chosen tokens (Defaults to null)
     * @return this builder
     */
    public CompletionBuilder setLogprobs(int logprobs) {
        if (logprobs < MIN_COMPLETION_LOGPROBS) throw new IllegalArgumentException("n <= "+MIN_COMPLETION_LOGPROBS);
        if (logprobs > MAX_COMPLETION_LOGPROBS) throw new IllegalArgumentException("n > "+MAX_COMPLETION_LOGPROBS);
        this.logprobs = OptionalInt.of(logprobs);
        return this;
    }

    /**
     * echo parameter setter.
     *
     * @param echo Echo back the prompt in addition to the completion (Defaults to false)
     * @return this builder
     */
    public CompletionBuilder setEcho(boolean echo) {
        this.echo = echo;
        return this;
    }


    /**
     * stop parameter setter
     *
     * @param stop up to 4 sequences where the API will stop generating further tokens (Defaults to null)
     * @return this builder
     */
    public CompletionBuilder setStop(JsArray stop) {
        this.stop = Objects.requireNonNull(stop);
        if (stop.size() > MAX_COMPLETION_SIZE_STOP) throw new IllegalArgumentException("stop size > "+MAX_COMPLETION_SIZE_STOP);
        if (stop.size() == 0) throw new IllegalArgumentException("stop empty");
        return this;
    }

    /**
     * stop parameter setter
     *
     * @param stop text where the API will stop generating further tokens (Defaults to null)
     * @return this builder
     */
    public CompletionBuilder setStop(String stop) {
        return setStop(JsArray.of(Objects.requireNonNull(stop)));
    }


    /**
     * presence_penalty parameter setter.
     *
     * @param value Number between -2.0 and 2.0. Positive values penalize new tokens based on whether they appear in the
     *              text so far, increasing the model's likelihood to talk about new topics (Defaults to 0)
     * @return this builder
     */
    public CompletionBuilder setPresencePenalty(double value) {
        if (value < MIN_COMPLETION_PRESENCE_PENALTY) throw new IllegalArgumentException("presencePenalty < "+ MIN_COMPLETION_PRESENCE_PENALTY);
        if (value > MAX_COMPLETION_PRESENCE_PENALTY) throw new IllegalArgumentException("presencePenalty > "+MAX_COMPLETION_PRESENCE_PENALTY);
        this.presencePenalty = value;
        return this;
    }

    /**
     * frequency_penalty parameter setter
     *
     * @param value between -2.0 and 2.0. Positive values penalize new tokens based on their existing frequency in
     *              the text so far, decreasing the model's likelihood to repeat the same line verbatim. (Defaults to 0)
     * @return this builder
     */
    public CompletionBuilder setFrequencyPenalty(double value) {
        if (value < MIN_COMPLETION_FREQ_PENALTY) throw new IllegalArgumentException("frequencyPenalty < "+MIN_COMPLETION_FREQ_PENALTY);
        if (value > MAX_COMPLETION_FREQ_PENALTY) throw new IllegalArgumentException("frequencyPenalty > "+MAX_COMPLETION_FREQ_PENALTY);
        this.frequencyPenalty = value;
        return this;
    }

    /***
     *
     * best_of parameter builder
     * When used with n, best_of controls the number of candidate completions and n specifies how many to return
     * – best_of must be greater than n. Because this parameter generates many completions, it can quickly consume
     * your token quota.
     * Use carefully and ensure that you have reasonable settings for max_tokens and stop.
     * @param bestOf Generates best_of completions server-side and returns the "best" (the one with the highest
     *               log probability per token).
     *               Results cannot be streamed. Defaults to 1
     * @return this builder
     */
    public CompletionBuilder setBestOf(int bestOf) {
        this.bestOf = bestOf;
        return this;
    }

    /**
     * user parameter builder
     * @param user A unique identifier representing your end-user, which can help OpenAI to monitor and detect abuse.
     * @return this builder
     */
    public CompletionBuilder setUser(String user) {
        this.user = Objects.requireNonNull(user);
        return this;
    }

    public JsObj build() {
        JsObj obj = JsObj.empty()
                         .set(MODEL_FIELD, JsStr.of(model))
                         .set(PROMPT_FIELD, prompt);

        if (suffix != null)
            obj = obj.set(SUFFIX_FIELD, JsStr.of(suffix));
        if (maxTokens != DEFAULT_MAX_TOKEN)
            obj = obj.set(MAX_TOKENS_FIELD, JsInt.of(maxTokens));
        if (temperature != DEFAULT_TEMPERATURE)
            obj = obj.set(TEMPERATURE_FIELD, JsDouble.of(temperature));
        if (topP != DEFAULT_TOP_P)
            obj = obj.set(TOP_P_FIELD, JsDouble.of(topP));
        if (n != DEFAULT_NUMBER_CHOICES)
            obj = obj.set(N_FIELD, JsInt.of(n));
        if (stream)
            obj = obj.set(STREAM_FIELD, JsBool.TRUE);
        if (logprobs.isPresent())
            obj = obj.set(LOGPROBS_FIELD, JsInt.of(logprobs.getAsInt()));
        if (echo)
            obj = obj.set(ECHO_FIELD, JsBool.TRUE);
        if (presencePenalty != DEFAULT_PRESENCE_PENALTY)
            obj = obj.set(PRESENCE_PENALTY_FIELD, JsDouble.of(presencePenalty));
        if (frequencyPenalty != DEFAULT_FREQ_PENALTY)
            obj = obj.set(FREQ_PENALTY_FIELD, JsDouble.of(frequencyPenalty));
        if (bestOf != DEFAULT_BEST_OF)
            obj = obj.set(BEST_OF_FIELD, JsInt.of(bestOf));
        if (user != null && !user.isBlank())
            obj = obj.set(USER_FIELD, JsStr.of(user));
        if (stop != null && !stop.isEmpty())
            obj = obj.set(STOP_FIELD, stop);

        return obj;

    }


}
