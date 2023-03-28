package jio.mongodb.api;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import jio.IO;
import jio.mongodb.*;
import jsonvalues.JsInt;
import jsonvalues.JsObj;
import jsonvalues.JsStr;
import mongovalues.JsValuesRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

@Disabled
public class TestErrors {


    private static FindOne findOne;


    @BeforeAll
    private static void prepare() {
    }

    private static CollectionSupplier getMongoCollectionSupplier(String connectionString) {
        ConnectionString connString = new ConnectionString(connectionString);

        MongoClientSettings settings = MongoClientSettings.builder()
                                                          .applyConnectionString(connString)
                                                          .codecRegistry(JsValuesRegistry.INSTANCE)
                                                          .build();

        MongoClient mongoClient = MongoClients.create(settings);

        DatabaseSupplier database = new DatabaseSupplier(mongoClient, "test");
        return new CollectionSupplier(database, "Data");

    }

    @Test
    public void test_Socket_Timeout_One_MilliSecond() {
        String connection = "mongodb://localhost:27017/?connectTimeoutMS=10000&socketTimeoutMS=1&serverSelectionTimeoutMS=10000";
        CollectionSupplier collection = getMongoCollectionSupplier(connection);

        findOne = FindOne.of(collection);

        JsObj obj = JsObj.of("a",
                             JsStr.of("a"),
                             "b",
                             JsInt.of(1)
                            );
        //"java.util.concurrent.CompletionException: jio.JioFailure: Timeout while receiving message"
        Assertions.assertTrue(findOne.apply(FindOptions.ofFilter(obj))
                                     .then(o -> IO.FALSE,
                                           e -> IO.succeed(Failures.READ_TIMEOUT.test(e.getCause())
                                                          )
                                          )
                                     .join()
                             );

    }

    /**
     * Se produce el timeout por connection timeout y se espera serverSelectionTimeoutMS (en este caso 10ms)
     * antes de dar la exception
     */
    @Test
    public void test_Connect_Timeout_One_MilliSecond() {
        String connection = "mongodb://localhost:27017/?connectTimeoutMS=1&socketTimeoutMS=10000&serverSelectionTimeoutMS=10";
        CollectionSupplier collection = getMongoCollectionSupplier(connection);

        findOne = FindOne.of(collection);
        JsObj obj = JsObj.of("a",
                             JsStr.of("a"),
                             "b",
                             JsInt.of(1)
                            );
//        "Timed out after 10 ms while waiting to connect. Client view of cluster state is {type=UNKNOWN, " +
//                "servers=[{address=localhost:27017, type=UNKNOWN, " +
//                "state=CONNECTING, exception={com.mongodb.MongoSocketReadTimeoutException: " +
//                "Timeout while receiving message}, caused by {java.net.SocketTimeoutException: Read timed out}}]
        Assertions.assertTrue(findOne.apply(FindOptions.ofFilter(obj))
                                     .then(o -> IO.TRUE,
                                           e -> IO.succeed(Failures.CONNECTION_TIMEOUT
                                                                   .test(e.getCause()))
                                          )
                                     .join());

    }
}
