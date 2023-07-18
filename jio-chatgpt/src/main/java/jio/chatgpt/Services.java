package jio.chatgpt;

import jio.http.client.MyHttpClient;

public final class Services {


    public Services(ConfBuilder confBuilder, MyHttpClient client) {
        audioService = new AudioService(client,confBuilder);
        chatService = new ChatService(client,confBuilder);
        completionService = new CompletionService(client,confBuilder);
        editService = new EditService(client,confBuilder);
        fileService = new FileService(client,confBuilder);
        fineTunerService = new FineTunerService(client,confBuilder);
        imageService = new ImageService(client,confBuilder);
        modelService = new ModelService(client,confBuilder);
        moderationService = new ModerationService(client,confBuilder);
    }

    public final AudioService audioService;
    public final ChatService chatService;
    public final CompletionService completionService;
    public final EditService editService;
    public final FileService fileService;
    public final FineTunerService fineTunerService;
    public final ImageService imageService;
    public final ModelService modelService;
    public final ModerationService moderationService;


}
