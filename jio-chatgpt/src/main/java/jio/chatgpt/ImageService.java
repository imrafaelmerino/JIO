package jio.chatgpt;


import jio.IO;
import jio.http.client.MyHttpClient;
import jsonvalues.JsObj;

import java.util.Objects;

/**
 * Service for interacting with image-related functionality.
 */
public final class ImageService extends AbstractService {
    /**
     * Creates an ImageService instance with the specified HTTP client and configuration builder.
     *
     * @param client  The HTTP client used for making requests.
     * @param builder The configuration builder for this service.
     */
    ImageService(MyHttpClient client, ConfBuilder builder) {

        super(client,
              builder,
              "images");
    }

    /**
     * Creates an image generation based on the provided builder.
     *
     * @param builder The image builder containing configuration details for image generation.
     * @return An IO (monadic) object representing the asynchronous result of the image generation request.
     */
    public IO<JsObj> create(ImageBuilder builder) {
        return post(uri.resolve("/" + "generations"),
                    Objects.requireNonNull(builder).build()
                   );
    }

    /**
     * Creates an image edit based on the provided builder.
     *
     * @param builder The image edit builder containing configuration details for image editing.
     * @return An IO (monadic) object representing the asynchronous result of the image edit request.
     */
    public IO<JsObj> createEdit(EditImageBuilder builder) {
        return post(uri.resolve("/" + "edits"),
                    builder.build()
                   );
    }

    /**
     * Creates an image variation based on the provided builder.
     *
     * @param builder The image variation builder containing configuration details for image variation.
     * @return An IO (monadic) object representing the asynchronous result of the image variation request.
     */
    public IO<JsObj> createVariation(VariationImageBuilder builder) {
        return post(uri.resolve("/" + "variations"),
                    builder.build()
                   );
    }
}
