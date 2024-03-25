package jio.api.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import jio.IO;
import jio.mongodb.CollectionBuilder;
import jio.mongodb.DatabaseBuilder;
import jio.mongodb.FindBuilder;
import jio.mongodb.FindOne;
import jio.mongodb.MongoExceptionFun;
import jsonvalues.JsInt;
import jsonvalues.JsObj;
import jsonvalues.JsStr;
import mongovalues.JsValuesRegistry;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;

/**
 * Follow the steps in the file steps-rs.md to deploy a replica set before executing this test
 * TODO: use testconainers to not to do it manually
 */
@Disabled
public class ErrorsTests {

  private static FindOne findOne;

  private static CollectionBuilder getMongoCollectionBuilder(String connectionString) {
    var connString = new ConnectionString(connectionString);

    var settings = MongoClientSettings.builder()
                                      .applyConnectionString(connString)
                                      .codecRegistry(JsValuesRegistry.INSTANCE)
                                      .build();

    var mongoClient = MongoClients.create(settings);

    var database = DatabaseBuilder.of(mongoClient,
                                      "test");
    return CollectionBuilder.of(database,
                                "Data");

  }

  @Test
  public void test_Socket_Timeout_One_MilliSecond() throws Exception {
    var connection = "mongodb://localhost:27017/?connectTimeoutMS=10000&socketTimeoutMS=1&serverSelectionTimeoutMS=10000";
    var collection = getMongoCollectionBuilder(connection);

    findOne = FindOne.of(collection);

    var obj = JsObj.of("a",
                       JsStr.of("a"),
                       "b",
                       JsInt.of(1)
                      );
    // "java.util.concurrent.CompletionException: jio.JioFailure: Timeout while receiving message"
    Assertions.assertTrue(findOne.standalone()
                                 .apply(FindBuilder.of(obj))
                                 .then(o -> IO.FALSE,
                                       e -> IO.succeed(MongoExceptionFun.IS_READ_TIMEOUT.test(e))
                                      )
                                 .compute()
                                 .getOutputOrThrow()
                         );

  }

  /**
   * Se produce el timeout por connection timeout y se espera serverSelectionTimeoutMS (en este caso 10ms) antes de dar
   * la exception
   */
  @Test
  public void test_Connect_Timeout_One_MilliSecond() throws Exception {
    var connection = "mongodb://localhost:27017/?connectTimeoutMS=1&socketTimeoutMS=10000&serverSelectionTimeoutMS=10";
    var collection = getMongoCollectionBuilder(connection);

    findOne = FindOne.of(collection);
    JsObj obj = JsObj.of("a",
                         JsStr.of("a"),
                         "b",
                         JsInt.of(1)
                        );
    //   "Timed out after 10 ms while waiting to connect. Client view of cluster state is {type=UNKNOWN, " +
    //   "servers=[{address=localhost:27017, type=UNKNOWN, " +
    //   "state=CONNECTING, exception={com.mongodb.MongoSocketReadTimeoutException: " +
    //   "Timeout while receiving message}, caused by {java.net.SocketTimeoutException: Read timed out}}]
    Assertions.assertTrue(findOne.standalone()
                                 .apply(FindBuilder.of(obj))
                                 .then(o -> IO.TRUE,
                                       e -> IO.succeed(MongoExceptionFun.IS_CONNECTION_TIMEOUT
                                                           .test(e))
                                      )
                                 .compute()
                                 .getOutputOrThrow());

  }
}
