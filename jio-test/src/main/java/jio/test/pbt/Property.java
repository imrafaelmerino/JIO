package jio.test.pbt;

import static java.util.Objects.requireNonNull;

import fun.gen.Gen;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.SplittableRandom;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import jio.BiLambda;
import jio.IO;
import jio.Result;
import jsonvalues.JsObj;

/**
 * Represents a property of a piece of code or program that should always be held and never fails.
 *
 * @param <GenValue> the type of the data generated to feed the property tests
 */
public non-sealed class Property<GenValue> extends Testable {

  private static final RandomGenerator seedGen = new SplittableRandom();
  final String name;
  final int times;
  final Gen<GenValue> gen;
  final BiLambda<JsObj, GenValue, TestResult> property;

  final String description;

  private final boolean collect;
  private final Path path;
  private final Map<String, Predicate<GenValue>> classifiers;

  Property(String name,
           Gen<GenValue> gen,
           BiLambda<JsObj, GenValue, TestResult> property,
           String description,
           int times,
           Path path,
           boolean collect,
           Map<String, Predicate<GenValue>> classifiers) {
    this.name = name;
    this.gen = gen;
    this.property = property;
    this.collect = collect;
    this.path = path;
    this.classifiers = classifiers;
    this.description = description;
    this.times = times;
  }

  private String getTags(GenValue value) {
    if (classifiers == null) {
      return "";
    }
    return classifiers.keySet()
                      .stream()
                      .filter(key -> classifiers.get(key)
                                                .test(value))
                      .collect(Collectors.joining(","));
  }

  void dump(Report report) {
    synchronized (Property.class) {
      try {
        Files.writeString(path,
                          STR."\{report}\n");
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
  }

  /**
   * Returns a new testable instance that represents the property and will be executed in parallel for the specified
   * number of times, using multiple threads from the common ForkJoinPool.
   *
   * @param n the number of parallel executions for the property
   * @return a new testable instance with parallel execution
   */
  public Testable repeatPar(final int n) {
    return new ParProperty<>(n,
                             this);
  }

  /**
   * Returns a new testable instance that represents the property and will be executed sequentially for the specified
   * number of times.
   *
   * @param n the number of sequential executions for the property
   * @return a new testable instance with sequential execution
   */
  public Testable repeatSeq(final int n) {
    return new SeqProperty<>(n,
                             this);
  }

  @Override
  IO<Report> create(final JsObj conf) {
    Supplier<Report> task = () -> {
      Report report = new Report(name,
                                 description);
      long seed = seedGen.nextLong();
      Supplier<GenValue> rg = gen.apply(new Random(seed));
      report.setStartTime(Instant.now());
      for (int i = 1; i <= times; i++) {
        report.incTest();
        var tic = Instant.now();
        var generated = rg.get();
        String tags = getTags(generated);
        if (classifiers != null) {
          report.classify(tags);
        }
        if (collect) {
          report.collect(generated == null ? "null" : generated.toString());
        }
        var context = new Context(tic,
                                  seed,
                                  i,
                                  generated,
                                  tags);
        var result = property.apply(conf,
                                    generated)
                             .compute();
        switch (result) {
          case Result.Success(TestResult tr) -> {
            report.tac(tic);
            if (tr instanceof TestFailure tf) {
              report.addFailure(new FailureContext(context,
                                                   tf));
            }
          }
          case Result.Failure(Exception exc) -> {
            report.tac(tic);
            if (requireNonNull(exc) instanceof TestFailure tf) {
              report.addFailure(new FailureContext(context,
                                                   tf));
            } else {
              report.addException(new ExceptionContext(context,
                                                       exc));
            }
          }
        }
      }
      report.setEndTime(Instant.now());
      return report;
    };

    IO<Report> io = IO.lazy(task)
                      .peekSuccess(Report::summarize);

    return path == null ? io : io.peekSuccess(this::dump);

  }

}
