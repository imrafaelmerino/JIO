package jio.mongodb.api;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import jio.IO;
import jio.mongodb.*;
import jsonvalues.JsArray;
import jsonvalues.JsObj;
import jsonvalues.JsStr;
import jsonvalues.gen.JsIntGen;
import jsonvalues.gen.JsObjGen;
import jsonvalues.gen.JsStrGen;
import mongovalues.JsValuesRegistry;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static jio.mongodb.Converters.str2Oid;

@Disabled
public class TestMongo {

    private static InsertOne<String> insertOne;
    private static FindOne findOne;
    private static IO<JsArray> find;
    private static FindAll findAll;

    @BeforeAll
    private static void prepare() {
        ConnectionString connString = new ConnectionString(
                "mongodb://localhost:27017/?connectTimeoutMS=10000&socketTimeoutMS=10000&serverSelectionTimeoutMS=10000"
        );

        MongoClientSettings settings = MongoClientSettings.builder()
                                                          .applyConnectionString(connString)
                                                          .codecRegistry(JsValuesRegistry.INSTANCE)
                                                          .build();


        MongoClient mongoClient = MongoClients.create(settings);
        DatabaseSupplier database = new DatabaseSupplier(mongoClient, "test");
        CollectionSupplier collectionSupplier = new CollectionSupplier(database, "Data");


        insertOne = InsertOne.of(collectionSupplier,
                                 Converters.insertOneResult2HexId
                                );

        findOne = FindOne.of(collectionSupplier);

        JsObj filter = JsObj.of("_id", JsObj.of("$lt", JsObj.of("$oid", JsStr.of("63ea462f9ecb966d69cfb85c"))));
        System.out.println(filter);
        find = FindAll.of(collectionSupplier).apply(FindOptions.ofFilter(filter));

        findAll = FindAll.of(collectionSupplier);

    }

    @Test
    public void testInsert() {


        var gen = JsObjGen.of("a",
                              JsStrGen.alphabetic(10),
                              "b",
                              JsIntGen.arbitrary(0, 10)
                             );

        Supplier<JsObj> supplier = gen.apply(new Random());


        IntStream.range(0,
                        1000
                       )
                 .parallel()
                 .forEach(i -> {
                     JsObj obj = supplier.get();
                     Assertions.assertEquals(obj,
                                             insertOne.apply(obj
                                                            )
                                                      .then(id -> findOne.apply(FindOptions.ofFilter(str2Oid.apply(id))))
                                                      .map(it -> it.delete("_id"))
                                                      .result()
                                            );
                 });

        System.out.println(findAll.apply(FindOptions.ofFilter(JsObj.empty())).result());

        JsArray arr = find.result();
        System.out.println(arr);
        Assertions.assertTrue(arr.size() > 1);


    }
}
