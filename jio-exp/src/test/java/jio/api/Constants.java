package jio.api;

import jio.IO;

public class Constants {

  public static final IO<String> A = IO.succeed("a");
  public static final IO<String> B = IO.succeed("b");
  public static final IO<String> C = IO.succeed("c");
  public static final IO<String> D = IO.succeed("d");

  public static final IO<Integer> ONE = IO.succeed(1);
  public static final IO<Integer> TWO = IO.succeed(2);
  public static final IO<Integer> THREE = IO.succeed(3);

}
