package jio.api.exp;

import jio.IO;
import jio.test.stub.Gens;
import jio.test.stub.StubBuilder;

import java.time.Duration;
import java.util.concurrent.Executors;

import static java.time.temporal.ChronoUnit.SECONDS;

public class Stubs {


    public static final StubBuilder<String> A_AFTER_1_SEC =
            StubBuilder.ofIOGen(Gens.seq(n -> IO.succeed("a"),
                                n -> Duration.of(1, SECONDS),
                                         Executors.newCachedThreadPool()
                                        )
                               );

    public static final StubBuilder<String> B_AFTER_1_SEC =
            StubBuilder.ofIOGen(Gens.seq(n -> IO.succeed("b"),
                                n -> Duration.of(1, SECONDS),
                                         Executors.newCachedThreadPool()
                                        )
                               );


    public static final StubBuilder<String> C_AFTER_1_SEC =
            StubBuilder.ofIOGen(Gens.seq(n -> IO.succeed("c"),
                                n -> Duration.of(1, SECONDS),
                                         Executors.newCachedThreadPool()
                                        )
                               );


}
