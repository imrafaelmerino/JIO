package jio.mongodb.api;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import jio.IO;
import jio.mongodb.*;
import jsonvalues.JsInt;
import jsonvalues.JsObj;
import jsonvalues.JsStr;
import mongovalues.JsValuesRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestErrors {


//    private static FindOne findOne;
//
//
//    private static CollectionBuilder getMongoCollectionBuilder(String connectionString) {
//        ConnectionString connString = new ConnectionString(connectionString);
//
//        MongoClientSettings settings = MongoClientSettings.builder()
//                                                          .applyConnectionString(connString)
//                                                          .codecRegistry(JsValuesRegistry.INSTANCE)
//                                                          .build();
//
//        MongoClient mongoClient = MongoClients.create(settings);
//
//        DatabaseBuilder database = DatabaseBuilder.of(mongoClient, "test");
//        return CollectionBuilder.of(database, "Data");
//
//    }
//
//    //@Test
//    public void test_Socket_Timeout_One_MilliSecond() {
//        String connection = "mongodb://localhost:27017/?connectTimeoutMS=10000&socketTimeoutMS=1&serverSelectionTimeoutMS=10000";
//        CollectionBuilder collection = getMongoCollectionBuilder(connection);
//
//        findOne = FindOne.of(collection);
//
//        JsObj obj = JsObj.of("a",
//                             JsStr.of("a"),
//                             "b",
//                             JsInt.of(1)
//                            );
//        //"java.util.concurrent.CompletionException: jio.JioFailure: Timeout while receiving message"
//        Assertions.assertTrue(findOne.standalone().apply(FindBuilder.of(obj))
//                                     .then(o -> IO.FALSE,
//                                           e -> IO.succeed(MongoExceptions.READ_TIMEOUT.test(e))
//                                          )
//                                     .result()
//                             );
//
//    }
//
//    /**
//     * Se produce el timeout por connection timeout y se espera serverSelectionTimeoutMS (en este caso 10ms) antes de
//     * dar la exception
//     */
//    @Test
//    public void test_Connect_Timeout_One_MilliSecond() {
//        String connection = "mongodb://localhost:27017/?connectTimeoutMS=1&socketTimeoutMS=10000&serverSelectionTimeoutMS=10";
//        CollectionBuilder collection = getMongoCollectionBuilder(connection);
//
//        findOne = FindOne.of(collection);
//        JsObj obj = JsObj.of("a",
//                             JsStr.of("a"),
//                             "b",
//                             JsInt.of(1)
//                            );
////        "Timed out after 10 ms while waiting to connect. Client view of cluster state is {type=UNKNOWN, " +
////                "servers=[{address=localhost:27017, type=UNKNOWN, " +
////                "state=CONNECTING, exception={com.mongodb.MongoSocketReadTimeoutException: " +
////                "Timeout while receiving message}, caused by {java.net.SocketTimeoutException: Read timed out}}]
//        Assertions.assertTrue(findOne.standalone().apply(FindBuilder.of(obj))
//                                     .then(o -> IO.TRUE,
//                                           e -> IO.succeed(MongoExceptions.CONNECTION_TIMEOUT
//                                                                   .test(e))
//                                          )
//                                     .result());
//
//    }
}
