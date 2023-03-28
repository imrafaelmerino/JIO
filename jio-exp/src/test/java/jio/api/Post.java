package jio.api;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class Post {

    Function<Integer,String> getAddressImpure =  n -> n.toString();


    Function<Integer, CompletableFuture<String>> getAddress =
            p -> {
                try {
                    return CompletableFuture.completedFuture(getAddressImpure.apply(p));
                } catch (Exception e) {
                    return CompletableFuture.failedFuture(e);
                }
            };



    @Test
    public void test(){




    }
}
