package jio.test.pbt;

import jio.IO;
import jsonvalues.JsObj;

/**
 * Represents a property that can be tested against different generated values a return a report
 */
public abstract sealed class Testable permits ParProperty, Property, SeqProperty {

  Testable() {
  }

  IO<Report> createTask() {
    return createTask(JsObj.empty());
  }

  abstract IO<Report> createTask(JsObj conf);

  /**
   * Executes the property test defined by this Testable instance.
   *
   * @return The result of the test is encapsulated in a Report object.
   */
  public Report check() {
    return createTask().join();
  }

  /**
   * Executes the property test defined by this Property instance with the given configuration.
   *
   * @param conf The JSON configuration used for property testing. The configuration provides additional information or
   *             parameters needed for the property test.
   * @return The result of the test is encapsulated in a Report object.
   */
  public Report check(final JsObj conf) {
    return createTask(conf).join();
  }
}
