package jio.chatgpt;

final class Constraints {

    static final int MAX_CHAT_COMPLETION_TEMPERATURE = 2;
    static final int MIN_CHAT_COMPLETION_TEMPERATURE = 0;
    static final int MIN_CHOICES = 1;
    static final int MAX_SIZE_STOP = 4;
    static final double MIN_PRESENCE_PENALTY = -2.0;
    static final double MAX_PRESENCE_PENALTY = 2.0;
    static final double MIN_FREQ_PENALTY = -2.0;
    static final double MAX_FREQ_PENALTY = 2.0;

    static final int MIN_COMPLETION_CHOICES = 1;
    static final int MAX_COMPLETION_TEMPERATURE = 2;
    static final int MIN_COMPLETION_TEMPERATURE = 0;
    static final double MAX_TOP_P = 1.0;

    static final int MAX_N_EDIT = 10;
    static final double MIN_TOP_P = 0.0;
    static final int MAX_COMPLETION_LOGPROBS = 5;
    static final int MIN_COMPLETION_LOGPROBS = 0;
    static final int MAX_COMPLETION_SIZE_STOP = 4;
    static final double MIN_COMPLETION_PRESENCE_PENALTY = -2.0;
    static final double MAX_COMPLETION_PRESENCE_PENALTY = 2.0;
    static final double MIN_COMPLETION_FREQ_PENALTY = -2.0;
    static final double MAX_COMPLETION_FREQ_PENALTY = 2.0;


    static final int MAX_EDIT_TEMPERATURE = 2;
    static final int MIN_EDIT_TEMPERATURE = 0;
    static final double MAX_EDIT_TOP_P = 1.0;
    static final double MIN_EDIT_TOP_P = 0.0;
}
