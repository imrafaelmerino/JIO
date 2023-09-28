package jio.chatgpt;


import static jio.chatgpt.JSON_FIELDS.*;

import jsonvalues.JsDouble;
import jsonvalues.JsObj;
import jsonvalues.JsStr;


/**
 * Builder class to create Translations
 */
public class TranslationBuilder {


    final String file;
    final String model;
    String prompt;
    double temperature;
    Data.RESPONSE_FORMAT responseFormat;


    public TranslationBuilder(String file, String model) {
        this.file = file;
        this.model = model;
        this.responseFormat = DEFAULT_VALUES.DEFAULT_TRANSLATION_FORMAT;
        this.temperature = DEFAULT_VALUES.DEFAULT_TRANSLATION_TEMPERATURE;
    }

    /**
     * @param responseFormat The format of the transcript output, in one of these options: json, text, srt, verbose_json, or vtt.
     *                       Defaults to json
     * @return this builder
     */
    public TranslationBuilder setResponseFormat(Data.RESPONSE_FORMAT responseFormat) {
        this.responseFormat = responseFormat;
        return this;
    }

    /**
     * Higher values like 0.8 will make the output more random, while lower values like 0.2 will make
     * it more focused and deterministic. If set to 0, the model will use log probability to automatically
     * increase the temperature until certain thresholds are hit.
     *
     * @param value The sampling temperature, between 0 and 1 (Defaults to 0)
     * @return this builder
     */
    public TranslationBuilder setTemperature(double value) {
        if (value > 1) throw new IllegalArgumentException("temperature > 1");
        if (value < 0) throw new IllegalArgumentException("temperature < 0");
        this.temperature = value;
        return this;
    }


    public JsObj build() {
        JsObj obj = JsObj.of(FILE_FIELD, JsStr.of(file),
                             MODEL_FIELD, JsStr.of(model)
                            );
        if (responseFormat != DEFAULT_VALUES.DEFAULT_TRANSLATION_FORMAT)
            obj = obj.set(RESPONSE_FORMAT_FIELD, JsStr.of(responseFormat.name()));
        if (temperature != DEFAULT_VALUES.DEFAULT_TRANSLATION_TEMPERATURE)
            obj = obj.set(TEMPERATURE_FIELD, JsDouble.of(temperature));
        if (prompt != null && !prompt.isBlank())
            obj = obj.set(PROMPT_FIELD, JsStr.of(prompt));
        return obj;
    }
}
