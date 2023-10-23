package jio.test.pbt;

import jio.IO;
import jio.ListExp;
import jsonvalues.JsObj;


non-sealed class ParProperty<O> extends Testable {

    int n;

    Property<O> prop;

    ParProperty(int n, Property<O> prop) {
        this.n = n;
        this.prop = prop;
    }

    @Override
    IO<Report> createTask(JsObj conf) {
        if (n < 1) throw new IllegalArgumentException("n < 1");
        final IO<Report> test = prop.createTask(conf);
        var result = ListExp.par(test);
        for (int i = 1; i < n; i++) result = result.append(test);
        return result.map(it -> it.stream().reduce(Report::aggregatePar).get());
    }
}
