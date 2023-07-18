package jio.chatgpt;

import jsonvalues.*;

import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class FineTuneBuilder {
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

    public FineTuneBuilder(String trainingFile) {
        this.trainingFile = Objects.requireNonNull(trainingFile);
        this.nEpochs = DEFAULT_VALUES.DEFAULT_FINE_TUNE_NEPOCHS;
        this.model = DEFAULT_VALUES.DEFAULT__FINE_TUNE_MODEL;
        this.batchSize = OptionalInt.empty();
        this.learningRateMultiplier = OptionalDouble.empty();
        this.classificationNClasses = OptionalInt.empty();
        this.promptLossWeight = DEFAULT_VALUES.DEFAULT__FINE_TUNE_PROMPTLOSSWEIGHT;
    }


    public FineTuneBuilder setValidationFile(String validationFile) {
        this.validationFile = Objects.requireNonNull(validationFile);
        return this;
    }

    public FineTuneBuilder setModel(String model) {
        this.model = Objects.requireNonNull(model);
        return this;
    }

    public FineTuneBuilder setNEpochs(int nEpochs) {
        if (nEpochs < 0) throw new IllegalArgumentException("nEpochs < 0");
        this.nEpochs = nEpochs;
        return this;
    }

    public FineTuneBuilder setBatchSize(int batchSize) {
        if (batchSize < 0) throw new IllegalArgumentException("batchSize < 0");
        this.batchSize = OptionalInt.of(batchSize);
        return this;
    }

    public FineTuneBuilder setLearningRateMultiplier(double learningRateMultiplier) {
        if (learningRateMultiplier < 0) throw new IllegalArgumentException("learningRateMultiplier < 0");
        this.learningRateMultiplier = OptionalDouble.of(learningRateMultiplier);
        return this;
    }

    public FineTuneBuilder setPromptLossWeight(double promptLossWeight) {
        if (promptLossWeight < 0) throw new IllegalArgumentException("promptLossWeight < 0");
        this.promptLossWeight = promptLossWeight;
        return this;
    }

    public FineTuneBuilder setComputeClassificationMetrics(boolean computeClassificationMetrics) {
        this.computeClassificationMetrics = computeClassificationMetrics;
        return this;
    }

    public FineTuneBuilder setClassificationNClasses(int classificationNClasses) {
        if (classificationNClasses < 0) throw new IllegalArgumentException("classificationNClasses < 0");
        this.classificationNClasses = OptionalInt.of(classificationNClasses);
        return this;
    }

    public FineTuneBuilder setClassificationPositiveClass(String classificationPositiveClass) {
        this.classificationPositiveClass = Objects.requireNonNull(classificationPositiveClass);
        return this;
    }

    public FineTuneBuilder setSuffix(String suffix) {
        this.suffix = Objects.requireNonNull(suffix);
        return this;
    }

    public JsObj build() {
        var body = JsObj.of(JSON_FIELDS.TRAINING_FILE_FIELD, JsStr.of(trainingFile));
        if (validationFile != null)
            body = body.set(JSON_FIELDS.VALIDATION_FILE_FIELD, JsStr.of(validationFile));
        if (!model.equals(DEFAULT_VALUES.DEFAULT__FINE_TUNE_MODEL))
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