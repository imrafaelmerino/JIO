package jio.chatgpt;

import jsonvalues.JsInt;
import jsonvalues.JsObj;
import jsonvalues.JsStr;

import java.util.Objects;

/**
 * Builder to create images. Given a prompt and/or an input image, the model will generate a new image.
 */
public final class ImageBuilder {

    final String prompt;
    Data.IMAGE_FORMAT responseFormat;
    String user;
    int n;
    Data.IMAGE_SIZE imageSize;


    private ImageBuilder(String prompt) {
        this.prompt = Objects.requireNonNull(prompt);
        if (prompt.length() > 1000) throw new IllegalArgumentException("prompt > 1000");
        this.imageSize = DEFAULT_VALUES.DEFAULT_IMAGE_SIZE;
        this.responseFormat = DEFAULT_VALUES.DEFAULT_IMAGE_RESPONSE_FORMAT;
        this.n = DEFAULT_VALUES.DEFAULT_N_IMAGES;
    }
    /**
     * Constructs an ImageBuilder with the provided prompt.
     *
     * @param prompt A text description of the desired image(s). The maximum length is 1000 characters.
     */
    public static ImageBuilder of(String prompt) {
        return new ImageBuilder(prompt);
    }

    /**
     * Sets the size parameter for the generated images.
     *
     * @param imageSize The size of the generated images. Must be one of 256x256, 512x512, or 1024x1024.
     * @return this builder
     */
    public ImageBuilder withImageSize(Data.IMAGE_SIZE imageSize) {
        this.imageSize = Objects.requireNonNull(imageSize);
        return this;
    }

    /**
     * Sets the response format parameter for the generated images.
     *
     * @param format The format in which the generated images are returned. Must be one of url or b64_json.
     * @return this builder
     */
    public ImageBuilder withResponseFormat(Data.IMAGE_FORMAT format) {
        this.responseFormat = Objects.requireNonNull(format);
        return this;
    }

    /**
     * Sets the number of images to generate.
     *
     * @param n The number of images to generate. Must be between 1 and 10.
     * @return this builder
     */
    public ImageBuilder withN(int n) {
        if (n < 0) throw new IllegalArgumentException("n < 0");
        if (n > 10) throw new IllegalArgumentException("n > 10");
        this.n = n;
        return this;
    }

    /**
     * Sets the user parameter, representing a unique identifier for the end-user. This can help OpenAI monitor and
     * detect abuse.
     *
     * @param user A unique identifier representing your end-user.
     * @return this builder
     */
    public ImageBuilder withUser(String user) {
        this.user = Objects.requireNonNull(user);
        return this;
    }

    /**
     * Builds a JSON object representing the image creation parameters.
     *
     * @return A JsObj containing the image creation parameters.
     */
    public JsObj build() {
        JsObj obj = JsObj.of(JSON_FIELDS.PROMPT_FIELD, JsStr.of(prompt));

        if (!responseFormat.equals(DEFAULT_VALUES.DEFAULT_IMAGE_RESPONSE_FORMAT))
            obj = obj.set(JSON_FIELDS.RESPONSE_FORMAT_FIELD, JsStr.of(responseFormat.name()));
        if (user != null && !user.isBlank())
            obj = obj.set(JSON_FIELDS.USER_FIELD, JsStr.of(user));
        if (n != DEFAULT_VALUES.DEFAULT_N_IMAGES)
            obj = obj.set(JSON_FIELDS.N_FIELD, JsInt.of(n));
        if (!imageSize.equals(DEFAULT_VALUES.DEFAULT_IMAGE_SIZE))
            obj = obj.set(JSON_FIELDS.SIZE_FIELD, JsStr.of(imageSize.size));
        return obj;
    }
}
