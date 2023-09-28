package jio.test.pbt;

import jio.*;
import jsonvalues.JsObj;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.util.Objects.requireNonNull;

public final class Group {

    private final List<Testable> props;
    private final String name;

    private Path path;


    private Group(String name, List<Testable> props) {
        this.name = Objects.requireNonNull(name);
        this.props = Objects.requireNonNull(props);
    }

    private Group(String name, Testable... props) {
        this.name = Objects.requireNonNull(name);
        this.props = Arrays.stream(Objects.requireNonNull(props)).toList();
    }

    public static Group of(String name, Testable... props) {
        return new Group(name, props);
    }

    public static Group of(String name, List<Testable> props) {
        return new Group(name, props);
    }

    public IO<GroupReport> randomSeq(JsObj conf) {
        var copy = new ArrayList<>(props);
        Collections.shuffle(copy);
        ListExp<Report> seq = ListExp.seq();
        for (var property : copy) seq = seq.append(property.check(conf));
        return processReport(seq.map(l -> new GroupReport(l, name)));
    }

    public IO<GroupReport> randomSeq() {
        return randomSeq(JsObj.empty());
    }

    public IO<GroupReport> randomPar(JsObj conf) {
        var copy = new ArrayList<>(props);
        Collections.shuffle(copy);
        ListExp<Report> par = ListExp.par();
        for (var property : copy) par = par.append(property.check(conf));
        return processReport(par.map(l -> new GroupReport(l, name)));
    }

    public IO<GroupReport> randomPar() {
        return randomPar(JsObj.empty());
    }

    public IO<GroupReport> seq() {
        return seq(JsObj.empty());
    }

    public IO<GroupReport> seq(JsObj conf) {
        ListExp<Report> seq = ListExp.seq();
        for (var property : props) seq = seq.append(property.check(conf));
        return processReport(seq.map(l -> new GroupReport(l, name)));
    }


    public IO<GroupReport> par() {
        return par(JsObj.empty());
    }

    public IO<GroupReport> par(JsObj conf) {
        ListExp<Report> par = ListExp.par();
        for (var property : props) par = par.append(property.check(conf));
        return processReport(par.map(l -> new GroupReport(l, name)));
    }

    public Group withExportPath(Path path) {
        if (!Files.isRegularFile(requireNonNull(path)))
            throw new IllegalArgumentException(String.format("%s is not a regular file", path));
        if (!Files.exists(path))
            throw new IllegalArgumentException(String.format("%s doesn't exist", path));
        this.path = path;
        return this;
    }

    synchronized void dump(GroupReport report) {
        try {
            Files.writeString(path, report + "\n");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    IO<GroupReport> processReport(final IO<GroupReport> io) {
        return path == null ? io : io.peekSuccess(this::dump);
    }
}
