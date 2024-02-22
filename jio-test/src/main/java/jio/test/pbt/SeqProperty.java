package jio.test.pbt;

import jio.IO;
import jio.ListExp;
import jsonvalues.JsObj;

import java.util.Objects;

non-sealed class SeqProperty<GenValue> extends Testable {

  final int executionTimes;

  final Property<GenValue> prop;

  SeqProperty(int executionTimes,
              Property<GenValue> prop) {
    this.executionTimes = executionTimes;
    this.prop = prop;
  }

  @Override
  IO<Report> create(JsObj conf) {
    if (executionTimes < 1) {
      throw new IllegalArgumentException("n < 1");
    }
    final IO<Report> test = prop.create(Objects.requireNonNull(conf));
    var result = ListExp.seq(test);
    for (int i = 1; i < executionTimes; i++) {
      result = result.append(test);
    }
    return result.map(it -> it.stream()
                              .reduce(Report::aggregate)
                              .get());
  }
}
