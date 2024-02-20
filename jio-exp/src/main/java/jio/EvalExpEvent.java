package jio;

import jdk.jfr.*;

@Label("Expression Evaluation Info")
@Name("jio.exp.EvalExp")
@Category({"JIO", "EXP"})
@Description("Duration, output, context and other info related to an expression from jio-exp")
@StackTrace(value = false)
class EvalExpEvent extends Event {

  public String expression;

  public String value;

  public String context;

  public String result;
  public String exception;

  public enum RESULT {
    SUCCESS, FAILURE
  }

}
