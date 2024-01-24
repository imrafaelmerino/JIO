package jio;

import jdk.jfr.*;


@Label("Expression Evaluation Info")
@Name("jio.exp.EvalExp")
@Category({"JIO"})
@Description("Duration, output, context and other info related to an expression from jio-exp")
@StackTrace(value = false)
class EvalExpEvent extends Event {

  public String expression;

  public String value;

  public String context;
  /**
   * Either SUCCESS OR FAILURE
   */
  public String result;
  /**
   * The exception in case of failure
   */
  public String exception;

  public enum RESULT {SUCCESS, FAILURE}


}
