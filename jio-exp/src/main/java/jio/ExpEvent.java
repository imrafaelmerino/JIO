package jio;

import jdk.jfr.*;


@Label("jio-eval-expression")
@Name("jio.exp")
@Category({"JIO"})
@Description("JIO expressions and subexpressions results")
class ExpEvent extends Event {

    static final String EXP_LABEL = "exp";
    static final String VALUE_LABEL = "value";
    static final String CONTEXT_LABEL = "context";
    static final String RESULT_LABEL = "result";
    static final String EXCEPTION_LABEL = "exception";
    @Label(EXP_LABEL)
    public String expression;
    @Label(VALUE_LABEL)
    public String value = "";
    @Label(CONTEXT_LABEL)
    public String context = "";
    /**
     * Either SUCCESS OR FAILURE
     */
    @Label(RESULT_LABEL)
    public String result;
    /**
     * The exception in case of failure
     */
    @Label(EXCEPTION_LABEL)
    public String exception = "";

    public enum RESULT {SUCCESS, FAILURE}


}
