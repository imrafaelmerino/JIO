package jio.chatgpt;

import jsonvalues.JsInt;
import jsonvalues.JsObj;
import jsonvalues.JsStr;

import java.util.Objects;

public class VariationImageBuilder {

    final String image;

    /**
     *
     * @param image The image to use as the basis for the variation(s). Must be a valid PNG file, less than 4MB, and square.
     */
    public VariationImageBuilder(String image) {
        this.image = Objects.requireNonNull(image);
        this.size = DEFAULT_VALUES.DEFAULT_VARIATION_IMAGE_SIZE;
        this.responseFormat = DEFAULT_VALUES.DEFAULT_VARIATION_IMAGE_RESPONSE_FORMAT;
        this.n = DEFAULT_VALUES.DEFAULT_N_VARIATION_IMAGES;
    }

    Data.IMAGE_FORMAT responseFormat;
    String user;
    int n;
    Data.IMAGE_SIZE size;



    /**
     * size parameter setter
     * @param size The size of the generated images. Must be one of 256x256, 512x512, or 1024x1024.
     * @return this builder
     */
    public VariationImageBuilder setSize(Data.IMAGE_SIZE size) {
        this.size = Objects.requireNonNull(size);
        return this;
    }


    /**
     * response_format parameter setter
     * @param format The format in which the generated images are returned. Must be one of url or b64_json.
     * @return this builder
     */
    public VariationImageBuilder setResponseFormat(Data.IMAGE_FORMAT format) {
        this.responseFormat = Objects.requireNonNull(format);
        return this;
    }

    /**
     * n parameter setter
     * @param n The number of images to generate. Must be between 1 and 10.
     * @return this builder
     */
    public VariationImageBuilder setN(int n) {
        if (n < 0) throw new IllegalArgumentException("n < 0");
        if (n > 10) throw new IllegalArgumentException("n > 10");
        this.n = n;
        return this;
    }

    /**
     * user parameter setter
     * @param user A unique identifier representing your end-user, which can help OpenAI to monitor and detect abuse
     * @return this builder
     */
    public VariationImageBuilder setUser(String user){
        this.user = Objects.requireNonNull(user);
        return this;
    }





    public JsObj build() {

        JsObj obj = JsObj.of(JSON_FIELDS.IMAGE_FIELD, JsStr.of(image));

        if (responseFormat != DEFAULT_VALUES.DEFAULT_VARIATION_IMAGE_RESPONSE_FORMAT)
            obj = obj.set(JSON_FIELDS.RESPONSE_FORMAT_FIELD, JsStr.of(responseFormat.name()));
        if (user != null && !user.isBlank())
            obj = obj.set(JSON_FIELDS.USER_FIELD, JsStr.of(user));
        if (n != DEFAULT_VALUES.DEFAULT_N_VARIATION_IMAGES)
            obj = obj.set(JSON_FIELDS.N_FIELD, JsInt.of(n));
        if (size != DEFAULT_VALUES.DEFAULT_VARIATION_IMAGE_SIZE)
            obj = obj.set(JSON_FIELDS.SIZE_FIELD, JsStr.of(size.size));
        return obj;
    }
}
