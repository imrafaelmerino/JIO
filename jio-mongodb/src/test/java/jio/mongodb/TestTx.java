package jio.mongodb;

import com.mongodb.client.MongoClient;
import jio.ListExp;
import jio.mongodb.*;
import jio.test.junit.Debugger;
import jsonvalues.JsObj;
import jsonvalues.JsStr;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.Duration;
import java.util.List;


public class TestTx {

//    @RegisterExtension
//    static Debugger debugger = Debugger.of(Duration.ofSeconds(2));
//
//
//    MongoClient mongoClient = MongoClientBuilder.DEFAULT
//            .build("mongodb://localhost:27017,localhost:27018,localhost:27019/?replicaSet=rs0");
//    DatabaseBuilder database = DatabaseBuilder.of(mongoClient, "test");
//    CollectionBuilder collectionBuilder = CollectionBuilder.of(database, "Person");
//    MongoLambda<JsObj, String> insertOne = InsertOne.of(collectionBuilder)
//                                                    .map(Converters::toHexId);
//    ClientSessionBuilder sessionSupplier = ClientSessionBuilder.of(mongoClient);
//
//    @Test
//    public void test() {
//        MongoLambda<List<JsObj>, List<String>> insertAll =
//                (session, jsons) ->
//                        jsons.stream()
//                             .map(json -> insertOne.apply(session, json))
//                             .collect(ListExp.parCollector());
//
//
//        var tx = TxBuilder.of(sessionSupplier).build(insertAll);
//
//        System.out.println(tx.apply(List.of(JsObj.of("hi", JsStr.of("bye")),
//                                            JsObj.of("hi", JsStr.of("bye"))
//                                           )
//                                   )
//                             .result());
//    }
//
}
