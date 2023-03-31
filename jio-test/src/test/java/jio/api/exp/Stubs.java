package jio.api.exp;

import jio.IO;
import jio.test.stub.value.IOStub;

import java.time.Duration;
import java.util.concurrent.Executors;

import static java.time.temporal.ChronoUnit.SECONDS;

public class Stubs {


    public static final IO<String> A = IO.fromValue("a");
    public static final IO<String> B = IO.fromValue("b");
    public static final IO<String> C = IO.fromValue("c");
    public static final IO<String> D = IO.fromValue("d");

    public static final IO<Integer> ZERO = IO.fromValue(0);
    public static final IO<Integer> ONE = IO.fromValue(1);
    public static final IO<Integer> TWO = IO.fromValue(2);
    public static final IO<Integer> THREE = IO.fromValue(3);


    public static final IOStub<String> A_AFTER_1_SEC =
            IOStub.succeed(n -> "a",
                           n -> Duration.of(1, SECONDS)
                          )
                  .onExecutor(Executors.newCachedThreadPool());

    public static final IOStub<String> B_AFTER_1_SEC =
            IOStub.succeed(n -> "b",
                           n -> Duration.of(1,
                                            SECONDS
                                           )
                          )
                  .onExecutor(Executors.newCachedThreadPool());
    ;

    public static final IOStub<String> C_AFTER_1_SEC =
            IOStub.succeed(n -> "c",
                           n -> Duration.of(1,
                                            SECONDS
                                           )
                          );

    public static final IOStub<String> D_AFTER_1_SEC =
            IOStub.succeed(n -> "d",
                           n -> Duration.of(1,
                                            SECONDS
                                           )
                          );


}
