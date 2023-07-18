package jio.chatgpt;

public class DEFAULT_VALUES {

    static final double DEFAULT_TEMPERATURE = 1.0;
    static final double DEFAULT_TOP_P = 1;
    static final boolean DEFAULT_STREAM = false;
    static final double DEFAULT_PRESENCE_PENALTY = 0.0;
    static final double DEFAULT_FREQ_PENALTY = 0.0;
    static final int DEFAULT_N_CHOICES = 1;
    static final int DEFAULT_N_EDITS = 1;
    static final int DEFAULT_NUMBER_CHOICES = 1;
    static final int DEFAULT_BEST_OF = 1;
    static final Data.IMAGE_SIZE DEFAULT_IMAGE_SIZE = Data.IMAGE_SIZE._1024;
    static final Data.IMAGE_SIZE DEFAULT_EDIT_IMAGE_SIZE = Data.IMAGE_SIZE._1024;
    static final Data.IMAGE_SIZE DEFAULT_VARIATION_IMAGE_SIZE = Data.IMAGE_SIZE._1024;
    static final Data.IMAGE_FORMAT DEFAULT_IMAGE_RESPONSE_FORMAT = Data.IMAGE_FORMAT.url;
    static final Data.IMAGE_FORMAT DEFAULT_EDIT_IMAGE_RESPONSE_FORMAT = Data.IMAGE_FORMAT.url;
    static final Data.IMAGE_FORMAT DEFAULT_VARIATION_IMAGE_RESPONSE_FORMAT = Data.IMAGE_FORMAT.url;
    static final int DEFAULT_N_IMAGES = 1;
    static final int DEFAULT_N_EDIT_IMAGES = 1;
    static final int DEFAULT_N_VARIATION_IMAGES = 1;
    final static double DEFAULT_TRANSCRIPTION_TEMPERATURE = 0;
    final static Data.RESPONSE_FORMAT DEFAULT_TRANSCRIPTION_FORMAT = Data.RESPONSE_FORMAT.json;
    final static double DEFAULT_TRANSLATION_TEMPERATURE = 0;
    final static Data.RESPONSE_FORMAT DEFAULT_TRANSLATION_FORMAT = Data.RESPONSE_FORMAT.json;

    static final int DEFAULT_FINE_TUNE_NEPOCHS = 4;
    static final String DEFAULT__FINE_TUNE_MODEL = "curie";
    static final double DEFAULT__FINE_TUNE_PROMPTLOSSWEIGHT = 0.01;
}
