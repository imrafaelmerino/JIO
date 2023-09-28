package jio.mongodb;

import jsonvalues.JsArray;


public final class FindAll extends Find<JsArray> {

    private FindAll(final CollectionSupplier collection) {
        super(collection,
              Converters.iterable2JsArray
             );
    }

    public static FindAll of(final CollectionSupplier collection) {
        return new FindAll(collection);
    }
}
