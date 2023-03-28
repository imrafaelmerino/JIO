package jio;

import jdk.jfr.*;


@Label("expressions")
@Name("jio.exp")
@Category({"JIO"})
@Description("JIO expressions and subexpressions results")
class ExpEvent extends Event {

    public enum RESULT {SUCCESS, FAILURE}

    @Label("exp")
    public String expression;

    @Label("value")
    public String value="";

    @Label("context")
    public String context = "";

    /**
     * Either SUCCESS OR FAILURE
     */
    @Label("result")
    public String result;

    /**
     * The exception in case of failure
     */
    @Label("exception")
    public String exception ="";

}
