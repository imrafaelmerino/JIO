package jio.test.pbt;

import jio.IO;
import jsonvalues.JsObj;

/**
 * Represents a property that can be tested against different generated values a return a report
 */
public abstract sealed class Testable permits ParProperty, Property, SeqProperty {

  Testable() {
  }

  IO<Report> create() {
    return create(JsObj.empty());
  }

  abstract IO<Report> create(JsObj conf);

  /**
   * Executes the property test defined by this Testable instance.
   *
   * @return The result of the test is encapsulated in a Report object.
   */
  public Report check() {
    try {
      return create().result()
                     .tryGet();
    } catch (Exception e) {
      throw new ReportNotGenerated(e);
    }
  }

  /**
   * Executes the property test defined by this Property instance with the given configuration.
   *
   * @param conf The JSON configuration used for property testing. The configuration provides additional information or
   *             parameters needed for the property test.
   * @return The result of the test is encapsulated in a Report object.
   */
  public Report check(final JsObj conf) {
    try {
      return create(conf).result()
                         .tryGet();
    } catch (Exception e) {
      throw new ReportNotGenerated(e);
    }
  }
}
