package jio.chatgpt;

import jio.http.client.MyHttpClient;

/**
 * A utility class that provides access to various services related to chat, AI models, and more.
 */
public final class Services {

    /**
     * Service for audio-related operations.
     */
    public final AudioService audioService;

    /**
     * Service for chat-related operations.
     */
    public final ChatService chatService;

    /**
     * Service for completion-related operations.
     */
    public final CompletionService completionService;

    /**
     * Service for editing operations.
     */
    public final EditService editService;

    /**
     * Service for file-related operations.
     */
    public final FileService fileService;

    /**
     * Service for fine-tuning models.
     */
    public final FineTunerService fineTunerService;

    /**
     * Service for image-related operations.
     */
    public final ImageService imageService;

    /**
     * Service for model-related operations.
     */
    public final ModelService modelService;

    /**
     * Service for content moderation.
     */
    public final ModerationService moderationService;

    /**
     * Initializes all the services with the provided configuration builder and HTTP client.
     *
     * @param confBuilder The configuration builder for the services.
     * @param client      The HTTP client used for making requests.
     */
    public Services(ConfBuilder confBuilder, MyHttpClient client) {
        audioService = new AudioService(client, confBuilder);
        chatService = new ChatService(client, confBuilder);
        completionService = new CompletionService(client, confBuilder);
        editService = new EditService(client, confBuilder);
        fileService = new FileService(client, confBuilder);
        fineTunerService = new FineTunerService(client, confBuilder);
        imageService = new ImageService(client, confBuilder);
        modelService = new ModelService(client, confBuilder);
        moderationService = new ModerationService(client, confBuilder);
    }
}
