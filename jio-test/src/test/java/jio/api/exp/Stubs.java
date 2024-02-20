package jio.api.exp;

import fun.gen.Gen;
import java.time.Duration;
import java.util.concurrent.ForkJoinPool;
import jio.IO;
import jio.test.stub.StubBuilder;

public class Stubs {

  public static final StubBuilder<String> A_AFTER_1_SEC = StubBuilder.ofGen(Gen.seq($ -> IO.succeed("a")))
                                                                     .withDelays(Gen.seq($ -> Duration.ofSeconds(1)))
                                                                     .withExecutor(ForkJoinPool.commonPool());

  public static final StubBuilder<String> B_AFTER_1_SEC = StubBuilder.ofGen(Gen.seq($ -> IO.succeed("b")))
                                                                     .withDelays(Gen.seq($ -> Duration.ofSeconds(1)))
                                                                     .withExecutor(ForkJoinPool.commonPool());

  public static final StubBuilder<String> C_AFTER_1_SEC = StubBuilder.ofGen(Gen.seq($ -> IO.succeed("c")))
                                                                     .withDelays(Gen.seq($ -> Duration.ofSeconds(1)))
                                                                     .withExecutor(ForkJoinPool.commonPool());

}
