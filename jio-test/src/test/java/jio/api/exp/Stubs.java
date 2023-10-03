package jio.api.exp;

import jio.IO;
import jio.test.stub.Gens;
import jio.test.stub.StubSupplier;

import java.time.Duration;
import java.util.concurrent.Executors;

import static java.time.temporal.ChronoUnit.SECONDS;

public class Stubs {


    public static final StubSupplier<String> A_AFTER_1_SEC =
            StubSupplier.ofGen(Gens.seq(n -> IO.succeed("a"),
                                n -> Duration.of(1, SECONDS),
                                        Executors.newCachedThreadPool()
                                       )
                              );

    public static final StubSupplier<String> B_AFTER_1_SEC =
            StubSupplier.ofGen(Gens.seq(n -> IO.succeed("b"),
                                n -> Duration.of(1, SECONDS),
                                        Executors.newCachedThreadPool()
                                       )
                              );


    public static final StubSupplier<String> C_AFTER_1_SEC =
            StubSupplier.ofGen(Gens.seq(n -> IO.succeed("c"),
                                n -> Duration.of(1, SECONDS),
                                        Executors.newCachedThreadPool()
                                       )
                              );


}
