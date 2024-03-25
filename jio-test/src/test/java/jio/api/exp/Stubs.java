package jio.api.exp;

import fun.gen.Gen;
import java.time.Duration;
import jio.Result.Success;
import jio.test.stub.StubBuilder;

public class Stubs {

  public static final StubBuilder<String> A_AFTER_1_SEC =
      StubBuilder.ofGen(Gen.seq(_ -> new Success<>("a")))
                 .withDelays(Gen.seq(_ -> Duration.ofSeconds(1)));

  public static final StubBuilder<String> B_AFTER_1_SEC =
      StubBuilder.ofGen(Gen.seq(_ -> new Success<>("b")))
                 .withDelays(Gen.seq(_ -> Duration.ofSeconds(1)));

  public static final StubBuilder<String> C_AFTER_1_SEC =
      StubBuilder.ofGen(Gen.seq(_ -> new Success<>("c")))
                 .withDelays(Gen.seq(_ -> Duration.ofSeconds(1)));

}
