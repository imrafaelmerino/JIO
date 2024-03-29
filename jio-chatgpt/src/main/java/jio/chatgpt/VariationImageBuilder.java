package jio.chatgpt;

import jsonvalues.JsInt;
import jsonvalues.JsObj;
import jsonvalues.JsStr;

import java.util.Objects;

import static jio.chatgpt.DEFAULT_VALUES.DEFAULT_N_VARIATION_IMAGES;
import static jio.chatgpt.DEFAULT_VALUES.DEFAULT_VARIATION_IMAGE_SIZE;
import static jio.chatgpt.JSON_FIELDS.*;

/**
 * Builder class to create variation images. Given a base image, this builder allows you to generate variations of the
 * image.
 */
public final class VariationImageBuilder {

    final String image;
    Data.IMAGE_FORMAT responseFormat;
    String user;
    int n;
    Data.IMAGE_SIZE size;

    /**
     * Constructs a VariationImageBuilder with the provided base image.
     *
     * @param image The image to use as the basis for the variation(s). Must be a valid PNG file, less than 4MB, and
     *              square.
     */
    private VariationImageBuilder(final String image) {
        this.image = Objects.requireNonNull(image);
        this.size = DEFAULT_VARIATION_IMAGE_SIZE;
        this.responseFormat = DEFAULT_VALUES.DEFAULT_VARIATION_IMAGE_RESPONSE_FORMAT;
        this.n = DEFAULT_N_VARIATION_IMAGES;
    }

    public static VariationImageBuilder of(final String image) {
        return new VariationImageBuilder(image);
    }

    /**
     * Sets the size parameter for the generated images.
     *
     * @param size The size of the generated images, which must be one of 256x256, 512x512, or 1024x1024.
     * @return This builder instance.
     */
    public VariationImageBuilder withSize(final Data.IMAGE_SIZE size) {
        this.size = Objects.requireNonNull(size);
        return this;
    }


    /**
     * Sets the response format parameter for the generated images.
     *
     * @param format The format in which the generated images are returned, which must be one of url or b64_json.
     * @return This builder instance.
     */
    public VariationImageBuilder withResponseFormat(final Data.IMAGE_FORMAT format) {
        this.responseFormat = Objects.requireNonNull(format);
        return this;
    }

    /**
     * Sets the number of images to generate.
     *
     * @param n The number of images to generate, which must be between 1 and 10.
     * @return This builder instance.
     */
    public VariationImageBuilder withN(int n) {
        if (n < 0) throw new IllegalArgumentException("n < 0");
        if (n > 10) throw new IllegalArgumentException("n > 10");
        this.n = n;
        return this;
    }

    /**
     * Sets the user parameter to represent your end-user, which can help OpenAI monitor and detect abuse.
     *
     * @param user A unique identifier representing your end-user.
     * @return This builder instance.
     */
    public VariationImageBuilder withUser(String user) {
        this.user = Objects.requireNonNull(user);
        return this;
    }

    /**
     * Builds a JSON object representing the parameters for generating variation images.
     *
     * @return A JsObj containing the variation image parameters.
     */
    public JsObj build() {

        JsObj obj = JsObj.of(JSON_FIELDS.IMAGE_FIELD, JsStr.of(image));

        if (!responseFormat.equals(DEFAULT_VALUES.DEFAULT_VARIATION_IMAGE_RESPONSE_FORMAT))
            obj = obj.set(RESPONSE_FORMAT_FIELD,
                          JsStr.of(responseFormat.name()));
        if (user != null && !user.isBlank())
            obj = obj.set(USER_FIELD,
                          JsStr.of(user));
        if (n != DEFAULT_N_VARIATION_IMAGES)
            obj = obj.set(N_FIELD,
                          JsInt.of(n));
        if (size != DEFAULT_VARIATION_IMAGE_SIZE)
            obj = obj.set(SIZE_FIELD,
                          JsStr.of(size.size));
        return obj;
    }
}
