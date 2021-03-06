package jio.api;

import jio.*;
import jsonvalues.JsArray;
import jsonvalues.JsBool;
import jsonvalues.JsInt;
import jsonvalues.JsObj;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TestConstructors {

    @Test
    public void succeed_constructor() {

        IO<String> foo = IO.fromValue("foo");

        Assertions.assertEquals("foo", foo.join());

        Instant before = Instant.now();
        IO<Instant> now = IO.fromSupplier(Instant::now);

        Assertions.assertTrue(before.isBefore(now.join()));

    }


    @Test
    public void computation_constructor() {

        String forkJoinPoolThreadName = IO.fromSupplier(
                () -> Thread.currentThread().getName(),
                ForkJoinPool.commonPool()
                                                       ).join();

        Assertions.assertTrue(forkJoinPoolThreadName.startsWith("ForkJoinPool.commonPool-worker-"));

        String executorThreadName =
                IO.fromSupplier(
                        () -> Thread.currentThread().getName(),
                        Executors.newSingleThreadExecutor()
                               ).join();

        System.out.println("----------" + executorThreadName);

        Assertions.assertTrue(executorThreadName.startsWith("pool") &&
                                      executorThreadName.endsWith("thread-1"));

    }

    @Test
    public void testSupplier() throws InterruptedException {

        Supplier<String> a = () -> {
            throw new RuntimeException();
        };

        IO<String> b = IO.fromSupplier(a, Executors.newSingleThreadExecutor());

        CompletableFuture<String> fut = b.get();

        Thread.sleep(1000);
        Assertions.assertTrue(fut.isCompletedExceptionally());
    }


    @Test
    public void testAll() {

        IO<Boolean> par = AllExp.par(IO.FALSE, IO.TRUE, IO.FALSE).debugEach("my-op");

        Assertions.assertFalse(par.join());

        IO<Boolean> seq = AllExp.seq(IO.FALSE, IO.TRUE, IO.FALSE).debugEach("my-op");

        Assertions.assertFalse(seq.join());


    }

    @Test
    public void testIfElse() {
        Assertions.assertEquals("alternative",
                                IfElseExp.<String>predicate(IO.FALSE)
                                         .consequence(() -> IO.fromValue("consequence"))
                                         .alternative(() -> IO.fromValue("alternative"))
                                         .debugEach("my-op")
                                         .join()
                               );
    }

    @Test
    public void testJsObj() {
        Assertions.assertEquals(JsObj.of("a", JsInt.of(1),
                                         "b", JsInt.of(2),
                                         "c", JsInt.of(3),
                                         "d", JsObj.of("e", JsInt.of(4),
                                                       "f", JsInt.of(5),
                                                       "g", JsArray.of(true, false)
                                                      )
                                        ),
                                JsObjExp.par("a", IO.fromValue(1).map(JsInt::of),
                                             "b", IO.fromValue(2).map(JsInt::of),
                                             "c", IO.fromValue(3).map(JsInt::of),
                                             "d", JsObjExp.seq("e", IO.fromValue(4).map(JsInt::of),
                                                               "f", IO.fromValue(5).map(JsInt::of),
                                                               "g", JsArrayExp.seq(IO.TRUE.map(JsBool::of),
                                                                                   IO.FALSE.map(JsBool::of)
                                                                                  )
                                                              )
                                            )
                                        .debugEach("my-op")
                                        .join()

                               );
    }

    @Test
    public void testResource() {


        String a = IO.fromResource(() -> {
                                   File file = File.createTempFile("example", "text");
                                   Files.writeString(file.toPath(), "hola");
                                   return new BufferedReader(new FileReader(file,StandardCharsets.UTF_8));
                               },
                               it -> it.lines().collect(Collectors.joining())
                                  )
                     .join();


        Assertions.assertEquals("hola", a);
    }

    @Test
    public void testOn() {

        try {
            IO.fromTask(() -> {
                  throw new IllegalArgumentException("hola");
              })
              .debug()
              .join();
        } catch (Exception e) {
            Assertions.assertEquals("hola", e.getCause().getMessage());
        }

        try {
            IO.fromTask(() -> {
                          throw new IllegalArgumentException("hola");
                      },
                        Executors.newCachedThreadPool()
                       )
              .debug()
              .join();
        } catch (Exception e) {
            Assertions.assertEquals("hola", e.getCause().getMessage());
        }
    }


}


