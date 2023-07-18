package jio.chatgpt;

import jsonvalues.JsInt;
import jsonvalues.JsObj;
import jsonvalues.JsStr;

import java.util.Objects;

public class EditImageBuilder {

    final String prompt;
    final String image;
    Data.IMAGE_FORMAT responseFormat;
    String user;
    int n;
    Data.IMAGE_SIZE size;
    String mask;


    /**
     * size parameter setter
     * @param size The size of the generated images. Must be one of 256x256, 512x512, or 1024x1024.
     * @return this builder
     */
    public EditImageBuilder setSize(Data.IMAGE_SIZE size) {
        this.size = Objects.requireNonNull(size);
        return this;
    }

    /**
     *
     * @param mask An additional image whose fully transparent areas (e.g. where alpha is zero) indicate where image
     *             should be edited. Must be a valid PNG file, less than 4MB, and have the same dimensions as image.
     * @return this builder
     */
    public EditImageBuilder setMask(String mask){
        this.mask = Objects.requireNonNull(mask);
        return this;
    }

    /**
     * response_format parameter setter
     * @param format The format in which the generated images are returned. Must be one of url or b64_json.
     * @return this builder
     */
    public EditImageBuilder setResponseFormat(Data.IMAGE_FORMAT format) {
        this.responseFormat = Objects.requireNonNull(format);
        return this;
    }

    /**
     * n parameter setter
     * @param n The number of images to generate. Must be between 1 and 10.
     * @return this builder
     */
    public EditImageBuilder setN(int n) {
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
    public EditImageBuilder setUser(String user){
        this.user = Objects.requireNonNull(user);
        return this;
    }



    /**
     * @param prompt A text description of the desired image(s). The maximum length is 1000 characters.
     * @param image The image to edit. Must be a valid PNG file, less than 4MB, and square. If mask is not provided,
     *              image must have transparency, which will be used as the mask.
     */
    public EditImageBuilder(String prompt, String image) {
        this.image = Objects.requireNonNull(image);
        this.prompt = Objects.requireNonNull(prompt);
        if (prompt.length() > 1000) throw new IllegalArgumentException("prompt > 1000");
        this.size = DEFAULT_VALUES.DEFAULT_EDIT_IMAGE_SIZE;
        this.responseFormat = DEFAULT_VALUES.DEFAULT_EDIT_IMAGE_RESPONSE_FORMAT;
        this.n = DEFAULT_VALUES.DEFAULT_N_EDIT_IMAGES;
    }

    public JsObj build() {

        JsObj obj = JsObj.of(JSON_FIELDS.PROMPT_FIELD, JsStr.of(prompt),
                             JSON_FIELDS.IMAGE_FIELD, JsStr.of(image));

        if (responseFormat != DEFAULT_VALUES.DEFAULT_EDIT_IMAGE_RESPONSE_FORMAT)
            obj = obj.set(JSON_FIELDS.RESPONSE_FORMAT_FIELD, JsStr.of(responseFormat.name()));
        if (user != null && !user.isBlank())
            obj = obj.set(JSON_FIELDS.USER_FIELD, JsStr.of(user));
        if (n != DEFAULT_VALUES.DEFAULT_N_EDIT_IMAGES)
            obj = obj.set(JSON_FIELDS.N_FIELD, JsInt.of(n));
        if (size != DEFAULT_VALUES.DEFAULT_EDIT_IMAGE_SIZE)
            obj = obj.set(JSON_FIELDS.SIZE_FIELD, JsStr.of(size.size));
        return obj;
    }
}
