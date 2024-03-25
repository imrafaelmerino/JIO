package jio.test;

import jdk.jfr.consumer.RecordedThread;
import jsonvalues.spec.JsArraySpec;
import jsonvalues.spec.JsObjSpec;
import jsonvalues.spec.JsSpec;
import jsonvalues.spec.JsSpecs;

/**
 * Utility class
 */
public final class Utils {

  private Utils() {
  }

  /**
   * Categorizes an HTTP status code into different groups based on its numerical output.
   *
   * @param code The HTTP status code to categorize.
   * @return A string representing the category of the status code.
   */
  public static String categorizeHttpStatusCode(int code) {
    if (code < 200) {
      return "INFORMATIONAL";
    }
    if (code < 300) {
      return "SUCCESS";
    }
    if (code < 400) {
      return "REDIRECTION";
    }
    if (code < 500) {
      return "CLIENT_ERROR";
    }
    return "SERVER_ERROR";
  }

  /**
   * Retrieves the name of a recorded thread. If the recorded thread is null, the method returns "not recorded". If the
   * Java name of the thread is empty, it returns a formatted string using the operating system (OS) thread ID with a
   * prefix "virtual-".
   *
   * @param thread The recorded thread for which to retrieve the name.
   * @return The name of the recorded thread or a default string if the thread is not recorded.
   */
  public static String getThreadName(final RecordedThread thread) {
    //happens at times that is null (don't know when)
    if (thread == null) {
      return "not recorded";
    }
    String javaName = thread.getJavaName();
    return javaName.isEmpty() ? "virtual-%s".formatted(thread.getOSThreadId()) : javaName;
  }
}
