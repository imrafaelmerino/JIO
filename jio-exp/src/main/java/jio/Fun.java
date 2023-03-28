package jio;

import java.util.function.Function;
import java.util.function.Supplier;

class Fun {

    static <A, B> Function<Supplier<A>, Supplier<B>> mapSupplier(Function<A, B> map) {
        return supplier -> () -> map.apply(supplier.get());
    }

    static void publishException(String exp,String context,Throwable exc){
        ExpEvent event = new ExpEvent();
        event.exception = String.format("%s:%s",exc.getClass().getName(),exc.getMessage());
        event.result = ExpEvent.RESULT.FAILURE.name();
        event.expression = exp;
        event.context = context;
        event.commit();
    }
}
