package jio.pbt;

import jio.*;
import jsonvalues.JsObj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class Group implements Function<JsObj, List<Report>> {

    private Group(String name, Function<JsObj, IO<List<Report>>> task) {
        this.name = name;
        this.task = task;
    }

    private final String name;
    private final Function<JsObj, IO<List<Report>>> task;



    @Override
    public List<Report> apply(final JsObj conf) {
        return task.apply(Objects.requireNonNull(conf))
                   .join();
    }

    public List<Report> apply() {
        return task.apply(JsObj.empty()).join();
    }

    public static Group seq(String groupName,
                            List<Property<?>> properties
                           ) {
        Objects.requireNonNull(properties);
        return new Group(Objects.requireNonNull(groupName),
                         conf -> {
                             ListExp<Report> seq = ListExp.seq();
                             for (Property<?> property : properties) seq = seq.append(property.task(conf));
                             return seq;
                         }
        );

    }

    public static Group par(final String groupName,
                            final List<Property<?>> properties
                           ) {
        Objects.requireNonNull(properties);
        return new Group(Objects.requireNonNull(groupName),
                         conf -> {
                             ListExp<Report> par = ListExp.par();
                             for (Property<?> property : properties) par = par.append(property.task(conf));
                             return par;
                         }
        );

    }

    public static Group randomSeq(final String groupName,
                                  final List<Property<?>> properties
                                 ) {
        var copy = new ArrayList<>(Objects.requireNonNull(properties));
        Collections.shuffle(copy);
        return seq(Objects.requireNonNull(groupName), copy);
    }

    public static Group randomPar(final String groupName,
                                  final List<Property<?>> properties
                                 ) {
        var copy = new ArrayList<>(Objects.requireNonNull(properties));
        Collections.shuffle(copy);
        return par(Objects.requireNonNull(groupName), copy);
    }

}
