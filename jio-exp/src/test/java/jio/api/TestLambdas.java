package jio.api;

import jio.BiLambda;
import jio.Lambda;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestLambdas {

  @Test
  public void test_lambda_lift() {

    Lambda<String, String> fn =
        Lambda.liftFunction(String::trim);

    Assertions.assertEquals("hi",
                            fn.apply("  hi  ")
                              .result());

    Lambda<String, Boolean> p = Lambda.liftPredicate(String::isBlank);

    Assertions.assertTrue(p.apply(" ")
                           .result());


  }

  @Test
  public void test_bilambda_lift() {

    BiLambda<String, String, String> fn =
        BiLambda.<String, String, String>liftFunction((a, b) -> a + b);

    Assertions.assertEquals("ab",
                            fn.apply("a",
                                     "b")
                              .result());

    BiLambda<String, String, Boolean> p = BiLambda.liftPredicate(String::endsWith);

    Assertions.assertTrue(p.apply("ab",
                                  "b")
                           .result());

  }
}
