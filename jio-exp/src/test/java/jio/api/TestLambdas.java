package jio.api;

import jio.BiLambda;
import jio.Lambda;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestLambdas {

    @Test
    public void test_lambda_lift() {

        Lambda<String, String> fn =
                Lambda.lift(String::trim);

        Assertions.assertEquals("hi", fn.apply("  hi  ").join());

        Lambda<String, Boolean> p = Lambda.lift(String::isBlank);

        Assertions.assertTrue(p.apply(" ").join());


    }

    @Test
    public void test_bilambda_lift() {

        BiLambda<String, String, String> fn =
                BiLambda.<String, String, String>lift((a, b) -> a + b);

        Assertions.assertEquals("ab", fn.apply("a", "b").join());

        BiLambda<String, String, Boolean> p = BiLambda.lift(String::endsWith);

        Assertions.assertTrue(p.apply("ab", "b").join());

    }
}
