package jio.test.pbt;

import jio.IO;
import jio.ListExp;
import jsonvalues.JsObj;
import java.util.Objects;



 class SeqProperty<O> implements Testable {

    int n;

    Property<O> prop;

    SeqProperty(int n, Property<O> prop) {
        this.n = n;
        this.prop = prop;
    }

    @Override
    public IO<Report> check(JsObj conf) {
        if (n < 1) throw new IllegalArgumentException("n < 1");
        final IO<Report> test = prop.check(Objects.requireNonNull(conf));
        var result = ListExp.seq(test);
        for (int i = 1; i < n; i++) result = result.append(test);
        return result.map(it -> it.stream().reduce(Report::aggregate).get());
    }
}
