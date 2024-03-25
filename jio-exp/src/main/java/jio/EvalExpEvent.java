package jio;

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;

@Label("Expression Evaluation Info")
@Name("jio.exp.EvalExp")
@Category({"JIO", "EXP"})
@Description("Duration, output, context and other info related to a computation")
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
