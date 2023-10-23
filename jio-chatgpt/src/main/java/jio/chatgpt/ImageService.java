package jio.chatgpt;


import jio.Lambda;
import jio.http.client.JioHttpClientBuilder;
import jsonvalues.JsObj;

import java.util.Objects;

/**
 * Service for interacting with image-related functionality.
 */
public final class ImageService extends AbstractService {
    /**
     * Creates an image generation based on the provided builder.
     *
     * @param builder The image builder containing configuration details for image generation.
     * @return An IO (monadic) object representing the asynchronous result of the image generation request.
     */
    public final Lambda<ImageBuilder, JsObj> create;
    /**
     * Creates an image edit based on the provided builder.
     *
     * @param builder The image edit builder containing configuration details for image editing.
     * @return An IO (monadic) object representing the asynchronous result of the image edit request.
     */
    public final Lambda<EditImageBuilder, JsObj> createEdit;
    /**
     * Creates an image variation based on the provided builder.
     *
     * @param builder The image variation builder containing configuration details for image variation.
     * @return An IO (monadic) object representing the asynchronous result of the image variation request.
     */
    public final Lambda<VariationImageBuilder, JsObj> createVariation;

    /**
     * Creates an ImageService instance with the specified HTTP client and configuration builder.
     *
     * @param clientBuilder The HTTP client used for making requests.
     * @param builder       The configuration builder for this service.
     */
    ImageService(JioHttpClientBuilder clientBuilder,
                 ConfBuilder builder
                ) {

        super(clientBuilder,
              builder,
              "images");
        create = b -> post(uri.resolve("/" + "generations"),
                           Objects.requireNonNull(b).build()
                          );

        createEdit = b -> post(uri.resolve("/" + "edits"),
                               b.build()
                              );

        createVariation = b -> post(uri.resolve("/" + "variations"),
                                    b.build()
                                   );
    }


}
