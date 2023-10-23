package jio.chatgpt;

import jio.http.client.JioHttpClientBuilder;

import java.util.Objects;

/**
 * A utility class that provides access to various services related to chat, AI models, and more.
 */
public final class ChatGPTServices {

    /**
     * Service for audio-related operations.
     */
    public final AudioService audio;

    /**
     * Service for chat-related operations.
     */
    public final ChatService chat;

    /**
     * Service for completion-related operations.
     */
    public final CompletionService completion;


    /**
     * Service for file-related operations.
     */
    public final FileService file;

    /**
     * Service for fine-tuning models.
     */
    public final FineTunerService fineTuner;

    /**
     * Service for image-related operations.
     */
    public final ImageService image;

    /**
     * Service for model-related operations.
     */
    public final ModelService model;

    /**
     * Service for content moderation.
     */
    public final ModerationService moderation;


    private ChatGPTServices(final ConfBuilder confBuilder,
                            final JioHttpClientBuilder httpClientBuilder
                           ) {
        Objects.requireNonNull(confBuilder);
        Objects.requireNonNull(httpClientBuilder);
        audio = new AudioService(httpClientBuilder, confBuilder);
        chat = new ChatService(httpClientBuilder, confBuilder);
        completion = new CompletionService(httpClientBuilder, confBuilder);
        file = new FileService(httpClientBuilder, confBuilder);
        fineTuner = new FineTunerService(httpClientBuilder, confBuilder);
        image = new ImageService(httpClientBuilder, confBuilder);
        model = new ModelService(httpClientBuilder, confBuilder);
        moderation = new ModerationService(httpClientBuilder, confBuilder);
    }

    /**
     * Initializes all the services with the provided configuration builder and HTTP client.
     *
     * @param confBuilder       The configuration builder for the services.
     * @param httpClientBuilder The HTTP client used for making requests.
     */
    public static ChatGPTServices of(final ConfBuilder confBuilder,
                                     final JioHttpClientBuilder httpClientBuilder
                                    ) {
        return new ChatGPTServices(confBuilder, httpClientBuilder);
    }
}
