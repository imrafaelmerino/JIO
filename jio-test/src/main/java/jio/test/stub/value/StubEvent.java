package jio.test.stub.value;

import jdk.jfr.*;


@Label("stubs")
@Name("jio.stub")
@Category({"JIO"})
@Description("JIO stubs")
class StubEvent extends Event {

    public enum RESULT {SUCCESS, FAILURE}

    @Label("counter")
    public int counter;

    @Label("value")
    public String value="";
    
    /**
     * Either SUCCESS OR FAILURE
     */
    @Label("result")
    public String result;

    /**
     * The exception in case of failure
     */
    @Label("exception")
    public String exception="";

}
