package jio.test.pbt;

import jio.IO;
import jio.ListExp;
import jsonvalues.JsObj;

non-sealed class ParProperty<GenValue> extends Testable {

  final int executionTimes;

  final Property<GenValue> prop;

  ParProperty(int executionTimes,
              Property<GenValue> prop) {
    this.executionTimes = executionTimes;
    this.prop = prop;
  }

  @Override
  IO<Report> create(JsObj conf) {
    if (executionTimes < 1) {
      throw new IllegalArgumentException("n < 1");
    }
    IO<Report> test = prop.create(conf);
    var result = ListExp.par(test);
    for (int i = 1; i < executionTimes; i++) {
      result = result.append(test);
    }
    return result.map(it -> it.stream()
                              .reduce(Report::aggregatePar)
                              .get());
  }
}
