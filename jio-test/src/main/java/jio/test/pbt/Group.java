package jio.test.pbt;

import jio.IO;
import jio.ListExp;
import jsonvalues.JsObj;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Represents a group of testable properties to be executed together. Properties can be executed sequentially or in
 * parallel, and the execution order can be randomized for property-based testing.
 */
public final class Group {

  private final List<Testable> props;
  private final String name;

  private Path path;

  private Group(String name,
                List<Testable> props) {
    this.name = Objects.requireNonNull(name);
    this.props = Objects.requireNonNull(props);
  }

  private Group(String name,
                Testable... props) {
    this.name = Objects.requireNonNull(name);
    this.props = Arrays.stream(Objects.requireNonNull(props))
                       .toList();
  }

  /**
   * Creates a new group of testable properties with the given name and properties.
   *
   * @param name  The name of the group.
   * @param props An ordered list of testable properties.
   * @return A new Group instance.
   */
  public static Group of(String name,
                         Testable... props) {
    return new Group(name,
                     props);
  }

  /**
   * Creates a new group of testable properties with the given name and properties.
   *
   * @param name  The name of the group.
   * @param props An ordered list of testable properties.
   * @return A new Group instance.
   */
  public static Group of(String name,
                         List<Testable> props) {
    return new Group(name,
                     props);
  }

  /**
   * Executes the properties in the group in a random sequence order with the specified configuration.
   *
   * @param conf The configuration as a JsObj.
   * @return An IO computation representing the group report.
   */
  public IO<GroupReport> randomSeq(JsObj conf) {
    var copy = new ArrayList<>(props);
    Collections.shuffle(copy);
    ListExp<Report> seq = ListExp.seq();
    for (var property : copy) {
      seq = seq.append(property.create(conf));
    }
    return processReport(seq.map(l -> new GroupReport(l,
                                                      name)));
  }

  /**
   * Executes the properties in the group in a random sequence order.
   *
   * @return An IO computation representing the group report.
   */
  public IO<GroupReport> randomSeq() {
    return randomSeq(JsObj.empty());
  }

  /**
   * Executes the properties in the group in parallel with a random order and the specified configuration.
   *
   * @param conf The configuration as a JsObj.
   * @return An IO computation representing the group report.
   */
  public IO<GroupReport> randomPar(JsObj conf) {
    var copy = new ArrayList<>(props);
    Collections.shuffle(copy);
    ListExp<Report> par = ListExp.par();
    for (var property : copy) {
      par = par.append(property.create(conf));
    }
    return processReport(par.map(l -> new GroupReport(l,
                                                      name)));
  }

  /**
   * Executes the properties in the group in parallel with a random order.
   *
   * @return An IO computation representing the group report.
   */
  public IO<GroupReport> randomPar() {
    return randomPar(JsObj.empty());
  }

  /**
   * Executes the properties in the group sequentially.
   *
   * @return An IO computation representing the group report.
   */
  public IO<GroupReport> seq() {
    return seq(JsObj.empty());
  }

  /**
   * Executes the properties in the group sequentially with the specified configuration.
   *
   * @param conf The configuration as a JsObj.
   * @return An IO computation representing the group report.
   */
  public IO<GroupReport> seq(JsObj conf) {
    ListExp<Report> seq = ListExp.seq();
    for (var property : props) {
      seq = seq.append(property.create(conf));
    }
    return processReport(seq.map(l -> new GroupReport(l,
                                                      name)));
  }

  /**
   * Executes the properties in the group in parallel.
   *
   * @return An IO computation representing the group report.
   */
  public IO<GroupReport> par() {
    return par(JsObj.empty());
  }

  /**
   * Executes the properties in the group in parallel with the specified configuration.
   *
   * @param conf The configuration as a JsObj.
   * @return An IO computation representing the group report.
   */
  public IO<GroupReport> par(JsObj conf) {
    ListExp<Report> par = ListExp.par();
    for (var property : props) {
      par = par.append(property.create(conf));
    }
    return processReport(par.map(l -> new GroupReport(l,
                                                      name)));
  }

  /**
   * Sets the export path for saving the group report as a file. The group report will be saved to the specified path
   * after executing the group of properties.
   *
   * @param path The export path where the group report will be saved as a file.
   * @return This Group instance with the export path set.
   * @throws IllegalArgumentException If the specified path is not a regular file or doesn't exist.
   */
  public Group withExportPath(Path path) {
    if (!Files.isRegularFile(requireNonNull(path))) {
      throw new IllegalArgumentException(String.format("%s is not a regular file",
                                                       path));
    }
    if (!Files.exists(path)) {
      throw new IllegalArgumentException(String.format("%s doesn't exist",
                                                       path));
    }
    this.path = path;
    return this;
  }

  void dump(GroupReport report) {
    synchronized (Group.class) {
      try {
        Files.writeString(path,
                          report + "\n");
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
  }

  IO<GroupReport> processReport(final IO<GroupReport> io) {
    return path == null ? io : io.peekSuccess(this::dump);
  }
}
