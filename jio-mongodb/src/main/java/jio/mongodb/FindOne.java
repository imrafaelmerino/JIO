package jio.mongodb;

import jsonvalues.JsObj;


public final class FindOne extends Find<JsObj> {

    private FindOne(final CollectionSupplier collection) {
        super(collection,
              Converters.iterableFirst
             );
    }

    public static FindOne of(final CollectionSupplier collection) {
        return new FindOne(collection);
    }
}
