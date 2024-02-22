package jio.console;

import java.util.function.BiFunction;
import java.util.function.Function;
import jio.IO;

class Errors {

  public static final Function<Command, IO<String>> ARGUMENTS_MISSING = command -> IO.fail(new InvalidCommand(command,
                                                                                                              "You forgot to pass in some argument!")
                                                                                          );

  public static final BiFunction<Integer, Command, IO<String>> TOO_MANY_ARGUMENTS = (n,
                                                                                     command) -> IO.fail(new InvalidCommand(command,
                                                                                                                            n
                                                                                                                            + " are too many arguments")
                                                                                                        );
}
