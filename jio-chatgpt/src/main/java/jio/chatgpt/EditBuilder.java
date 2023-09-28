package jio.chatgpt;

import jsonvalues.*;

import java.util.Objects;

/**
 * Edit builder. Given a prompt and an instruction, the model will return an edited version of the prompt.
 */
public class EditBuilder {

    private final String model;
    private final String instruction;
    private double temperature;
    private double topP;
    private int n;


    /**
     * @param model       ID of the model to use. You can use the text-davinci-edit-001 or code-davinci-edit-001 model with this endpoint.
     * @param instruction The instruction that tells the model how to edit the prompt.
     */
    public EditBuilder(String model, String instruction) {

        this.model = Objects.requireNonNull(model);
        this.instruction = Objects.requireNonNull(instruction);
        this.temperature = DEFAULT_VALUES.DEFAULT_TEMPERATURE;
        this.topP = DEFAULT_VALUES.DEFAULT_TOP_P;
        this.n = DEFAULT_VALUES.DEFAULT_N_EDITS;
    }


    /**
     * temperature parameter setter.
     * Higher values like 0.8 will make the output more random,
     * while lower values like 0.2 will make it more focused and deterministic.
     * <p>
     * We generally recommend altering this or top_p but not both.
     *
     * @param value What sampling temperature to use, between 0 and 2. (Defaults to 1)
     * @return this builder
     */
    public EditBuilder setTemperature(double value) {
        if (value > Constraints.MAX_EDIT_TEMPERATURE)
            throw new IllegalArgumentException("temperature > " + Constraints.MAX_EDIT_TEMPERATURE);
        if (value < Constraints.MIN_EDIT_TEMPERATURE)
            throw new IllegalArgumentException("temperature < " + Constraints.MIN_EDIT_TEMPERATURE);
        this.temperature = value;
        return this;
    }

    /**
     * top_p parameter setter.
     * 0.1 means only the tokens comprising the top 10% probability mass
     * are considered.
     * <p>
     * We generally recommend altering this or temperature but not both.
     *
     * @param value An alternative to sampling with temperature, called nucleus sampling, where the
     *              model considers the results of  the tokens with top_p probability mass. (Defaults to 1)
     * @return this builder
     */
    public EditBuilder setTopP(double value) {
        if (value < Constraints.MIN_EDIT_TOP_P) throw new IllegalArgumentException("topP < 0");
        if (value > Constraints.MAX_EDIT_TOP_P) throw new IllegalArgumentException("topP > 1");
        this.topP = value;
        return this;
    }

    /**
     * @param n How many edits to generate for the input and instruction.
     * @return this builder
     */
    public EditBuilder setN(int n) {
        this.n = n;
        return this;
    }


    public JsObj build() {
        JsObj obj = JsObj.of(JSON_FIELDS.MODEL_FIELD, JsStr.of(model),
                             JSON_FIELDS.INSTRUCTION_FIELD, JsStr.of(instruction)
                            );
        if (temperature == DEFAULT_VALUES.DEFAULT_TEMPERATURE)
            obj = obj.set(JSON_FIELDS.TEMPERATURE_FIELD, JsDouble.of(temperature));
        if (topP == DEFAULT_VALUES.DEFAULT_TOP_P) obj = obj.set(JSON_FIELDS.TOP_P_FIELD, JsDouble.of(topP));
        if (n == DEFAULT_VALUES.DEFAULT_N_EDITS) obj = obj.set(JSON_FIELDS.N_FIELD, JsInt.of(n));
        return obj;

    }


}
