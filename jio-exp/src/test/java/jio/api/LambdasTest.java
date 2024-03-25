package jio.api;

import jio.BiLambda;
import jio.Lambda;
import jio.Result;
import jio.Result.Success;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LambdasTest {

  @Test
  public void test_lambda_lift() {

    Lambda<String, String> fn = Lambda.liftFunction(String::trim);

    Assertions.assertEquals(new Success<>("hi"),
                            fn.apply("  hi  ")
                              .compute());

    Lambda<String, Boolean> p = Lambda.liftPredicate(String::isBlank);

    Assertions.assertEquals(Result.TRUE,
                            p.apply(" ")
                             .compute());

  }

  @Test
  public void test_bilambda_lift() {

    BiLambda<String, String, String> fn = BiLambda.<String, String, String>liftFunction((a,
                                                                                         b) -> a + b);

    Assertions.assertEquals(new Success<>("ab"),
                            fn.apply("a",
                                     "b")
                              .compute());

    BiLambda<String, String, Boolean> p = BiLambda.liftPredicate(String::endsWith);

    Assertions.assertEquals(Result.TRUE,
                            p.apply("ab",
                                    "b")
                             .compute());

  }
}
