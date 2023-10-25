package jio.mongodb.api;


import com.mongodb.client.MongoClient;
import com.mongodb.client.result.InsertOneResult;
import jio.IO;
import jio.ListExp;
import jio.mongodb.*;
import jio.test.junit.Debugger;
import jsonvalues.JsObj;
import jsonvalues.JsStr;
import jsonvalues.gen.JsIntGen;
import jsonvalues.gen.JsObjGen;
import jsonvalues.gen.JsStrGen;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.IntStream;

//@Disabled
public class TestMongo {

//    @RegisterExtension
//    static Debugger debugger = Debugger.of(Duration.ofSeconds(2));
//
//    private static MongoClient mongoClient;
//    private static DatabaseBuilder database;
//    private static MongoLambda<JsObj, InsertOneResult> insertOne;
//    private static FindOne findOne;
//    private static IO<List<JsObj>> find;
//    private static FindAll findAll;
//
//    MongoLambda<JsObj, JsObj> insertAndSetId =
//            (session, obj) ->
//                    insertOne.apply(session, obj)
//                             .map(result -> obj.set("id",
//                                                    Converters.toHexId(result))
//                                 );
//    MongoLambda<PersonAddress, PersonAddress> insertInCascade =
//            (session, pa) -> insertAndSetId.apply(session, pa.person)
//                                           .then(updatedPerson ->
//                                                         insertAndSetId.apply(session, pa.address.set("person_id", updatedPerson.getStr("id")))
//                                                                       .map(updatedAddress -> new PersonAddress(updatedPerson,
//                                                                                                                updatedAddress))
//                                                );
//
//    @BeforeAll
//    static void prepare() {
//
//        mongoClient = MongoClientBuilder.DEFAULT.build("mongodb://localhost:27019/");
//        database = DatabaseBuilder.of(mongoClient, "test");
//        var dataCollection = CollectionBuilder.of(database, "Data");
//
//        insertOne = InsertOne.of(dataCollection);
//
//        findOne = FindOne.of(dataCollection);
//
//        JsObj filter = JsObj.of("_id", JsObj.of("$lt", JsObj.of("$oid", JsStr.of(new ObjectId().toString()))));
//
//        find = FindAll.of(dataCollection)
//                      .standalone().apply(FindBuilder.of(filter)).map(Converters::toListOfJsObj);
//
//        findAll = FindAll.of(dataCollection);
//
//
//    }
//
//    @Test
//    public void testInsert() {
//
//
//        var gen = JsObjGen.of("a",
//                              JsStrGen.alphabetic(10),
//                              "b",
//                              JsIntGen.arbitrary(0, 10)
//                             );
//
//        Supplier<JsObj> supplier = gen.apply(new Random());
//
//
//        IntStream.range(0,
//                        1000
//                       )
//                 .parallel()
//                 .forEach(i -> {
//                     JsObj obj = supplier.get();
//                     Assertions.assertEquals(obj,
//                                             insertOne.standalone()
//                                                      .apply(obj)
//                                                      .map(Converters::toHexId)
//                                                      .then(id -> findOne.standalone().apply(FindBuilder.of(Converters.toObjId(id))))
//                                                      .map(it -> it.delete("_id"))
//                                                      .result()
//                                            );
//                 });
//
//        System.out.println(findAll.standalone()
//                                  .apply(FindBuilder.of(JsObj.empty()))
//                                  .map(Converters::toJsArray)
//                                  .result()
//                                  .size());
//
//        List<JsObj> arr = find.result();
//        System.out.println(arr.size());
//        Assertions.assertTrue(arr.size() > 1);
//
//
//    }
//
//    @Test
//    public void testInsertJsonsInParallel() {
//
//        var insertOne =
//                InsertOne.of(CollectionBuilder.of(database, "Tx"))
//                         .map(Converters::toHexId);
//
//        var sessionSupplier = ClientSessionBuilder.of(mongoClient);
//
//        MongoLambda<List<JsObj>, List<String>> insertAll =
//                (session, jsons) ->
//                        jsons.stream()
//                             .map(json -> insertOne.apply(session, json))
//                             .collect(ListExp.parCollector());
//
//
//        var tx = TxBuilder.of(sessionSupplier).build(insertAll);
//
//        tx.apply(List.of(JsObj.of("hi", JsStr.of("bye")),
//                         JsObj.of("hi", JsStr.of("bye"))
//                        )
//                )
//          .result();
//    }
//
//    record PersonAddress(JsObj person, JsObj address) {
//
//    }
//
}
