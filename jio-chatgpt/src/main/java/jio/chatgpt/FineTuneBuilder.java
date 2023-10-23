package jio.chatgpt;

import jsonvalues.*;

import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;


/**
 * A builder class for configuring and creating a fine-tuning job.
 */
public final class FineTuneBuilder {
    private final String trainingFile;
    private String validationFile;
    private String model;
    private int nEpochs;
    private double promptLossWeight;
    private boolean computeClassificationMetrics;
    private String classificationPositiveClass;
    private String suffix;
    private OptionalInt batchSize;
    private OptionalDouble learningRateMultiplier;
    private OptionalInt classificationNClasses;


    private FineTuneBuilder(String trainingFile) {
        this.trainingFile = Objects.requireNonNull(trainingFile);
        this.nEpochs = DEFAULT_VALUES.DEFAULT_FINE_TUNE_NEPOCHS;
        this.model = DEFAULT_VALUES.DEFAULT_FINE_TUNE_MODEL;
        this.batchSize = OptionalInt.empty();
        this.learningRateMultiplier = OptionalDouble.empty();
        this.classificationNClasses = OptionalInt.empty();
        this.promptLossWeight = DEFAULT_VALUES.DEFAULT__FINE_TUNE_PROMPTLOSSWEIGHT;
    }

    /**
     * Constructs a new FineTuneBuilder with the required training file.
     *
     * @param trainingFile The path or URL of the training data file.
     */
    public static FineTuneBuilder of(final String trainingFile) {
        return new FineTuneBuilder(trainingFile);
    }

    /**
     * Sets the validation data file.
     *
     * @param validationFile The path or URL of the validation data file.
     * @return This FineTuneBuilder instance for method chaining.
     */
    public FineTuneBuilder withValidationFile(String validationFile) {
        this.validationFile = Objects.requireNonNull(validationFile);
        return this;
    }

    /**
     * Sets the ID of the model to fine-tune.
     *
     * @param model The ID of the model.
     * @return This FineTuneBuilder instance for method chaining.
     */
    public FineTuneBuilder withModel(String model) {
        this.model = Objects.requireNonNull(model);
        return this;
    }

    /**
     * Sets the number of training epochs.
     *
     * @param nEpochs The number of training epochs.
     * @return This FineTuneBuilder instance for method chaining.
     */
    public FineTuneBuilder withNEpochs(int nEpochs) {
        if (nEpochs < 0) throw new IllegalArgumentException("nEpochs < 0");
        this.nEpochs = nEpochs;
        return this;
    }

    /**
     * Sets the batch size for training.
     *
     * @param batchSize The batch size.
     * @return This FineTuneBuilder instance for method chaining.
     */
    public FineTuneBuilder withBatchSize(int batchSize) {
        if (batchSize < 0) throw new IllegalArgumentException("batchSize < 0");
        this.batchSize = OptionalInt.of(batchSize);
        return this;
    }

    /**
     * Sets the learning rate multiplier.
     *
     * @param learningRateMultiplier The learning rate multiplier.
     * @return This FineTuneBuilder instance for method chaining.
     */
    public FineTuneBuilder withLearningRateMultiplier(double learningRateMultiplier) {
        if (learningRateMultiplier < 0) throw new IllegalArgumentException("learningRateMultiplier < 0");
        this.learningRateMultiplier = OptionalDouble.of(learningRateMultiplier);
        return this;
    }

    /**
     * Sets the weight assigned to the prompt loss.
     *
     * @param promptLossWeight The prompt loss weight.
     * @return This FineTuneBuilder instance for method chaining.
     */
    public FineTuneBuilder withPromptLossWeight(double promptLossWeight) {
        if (promptLossWeight < 0) throw new IllegalArgumentException("promptLossWeight < 0");
        this.promptLossWeight = promptLossWeight;
        return this;
    }

    /**
     * Sets whether to compute classification metrics.
     *
     * @param computeClassificationMetrics True to compute classification metrics, false otherwise.
     * @return This FineTuneBuilder instance for method chaining.
     */
    public FineTuneBuilder withComputeClassificationMetrics(boolean computeClassificationMetrics) {
        this.computeClassificationMetrics = computeClassificationMetrics;
        return this;
    }

    /**
     * Sets the number of classes for classification tasks.
     *
     * @param classificationNClasses The number of classes.
     * @return This FineTuneBuilder instance for method chaining.
     */
    public FineTuneBuilder withClassificationNClasses(int classificationNClasses) {
        if (classificationNClasses < 0) throw new IllegalArgumentException("classificationNClasses < 0");
        this.classificationNClasses = OptionalInt.of(classificationNClasses);
        return this;
    }

    /**
     * Sets the positive class for classification tasks.
     *
     * @param classificationPositiveClass The positive class.
     * @return This FineTuneBuilder instance for method chaining.
     */
    public FineTuneBuilder withClassificationPositiveClass(String classificationPositiveClass) {
        this.classificationPositiveClass = Objects.requireNonNull(classificationPositiveClass);
        return this;
    }

    /**
     * Sets a suffix to add to the fine-tuned model's name.
     *
     * @param suffix The suffix to add.
     * @return This FineTuneBuilder instance for method chaining.
     */
    public FineTuneBuilder withSuffix(String suffix) {
        this.suffix = Objects.requireNonNull(suffix);
        return this;
    }

    /**
     * Builds a JSON object representing the fine-tuning configuration.
     *
     * @return A JsObj containing the fine-tuning parameters.
     */
    public JsObj build() {
        var body = JsObj.of(JSON_FIELDS.TRAINING_FILE_FIELD, JsStr.of(trainingFile));
        if (validationFile != null)
            body = body.set(JSON_FIELDS.VALIDATION_FILE_FIELD, JsStr.of(validationFile));
        if (!model.equals(DEFAULT_VALUES.DEFAULT_FINE_TUNE_MODEL))
            body = body.set(JSON_FIELDS.MODEL_FIELD, JsStr.of(model));
        if (suffix != null) body = body.set(JSON_FIELDS.SUFFIX_FIELD, JsStr.of(suffix));
        if (classificationPositiveClass != null)
            body = body.set(JSON_FIELDS.CLASSIFICATION_POSITIVE_FIELD, JsStr.of(classificationPositiveClass));
        if (computeClassificationMetrics)
            body = body.set(JSON_FIELDS.COMPUTE_CLASSIFICATION_METRICS_FIELD, JsBool.TRUE);
        if (learningRateMultiplier.isPresent())
            body = body.set(JSON_FIELDS.LEARNING_RATE_MULTIPLIER_FIELD, JsDouble.of(learningRateMultiplier.getAsDouble()));
        if (promptLossWeight != DEFAULT_VALUES.DEFAULT__FINE_TUNE_PROMPTLOSSWEIGHT)
            body = body.set(JSON_FIELDS.PROMPT_LOSS_WEIGHT_FIELD, JsDouble.of(promptLossWeight));
        if (nEpochs != DEFAULT_VALUES.DEFAULT_FINE_TUNE_NEPOCHS)
            body = body.set(JSON_FIELDS.N_EPOCHS_FIELD, JsInt.of(nEpochs));
        if (batchSize.isPresent())
            body = body.set(JSON_FIELDS.BATCH_SIZE_FIELD, JsInt.of(batchSize.getAsInt()));
        if (classificationNClasses.isPresent())
            body = body.set(JSON_FIELDS.CLASSIFICATION_N_FIELD, JsInt.of(classificationNClasses.getAsInt()));
        return body;
    }
}
