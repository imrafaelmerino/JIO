package jio.api.mongodb;

import com.mongodb.client.MongoClient;
import java.time.Duration;
import java.util.List;
import jio.ListExp;
import jio.mongodb.ClientSessionBuilder;
import jio.mongodb.CollectionBuilder;
import jio.mongodb.Converters;
import jio.mongodb.DatabaseBuilder;
import jio.mongodb.InsertOne;
import jio.mongodb.MongoClientBuilder;
import jio.mongodb.MongoLambda;
import jio.mongodb.TxBuilder;
import jio.test.junit.Debugger;
import jsonvalues.JsObj;
import jsonvalues.JsStr;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@Disabled
public class TxTest {

  @RegisterExtension
  static Debugger debugger = Debugger.of(Duration.ofSeconds(2));

  MongoClient mongoClient = MongoClientBuilder.DEFAULT
      .build("mongodb://localhost:27017,localhost:27018,localhost:27019/?replicaSet=rs0");
  DatabaseBuilder database = DatabaseBuilder.of(mongoClient,
                                                "test");
  CollectionBuilder collectionBuilder = CollectionBuilder.of(database,
                                                             "Person");
  MongoLambda<JsObj, String> insertOne = InsertOne.of(collectionBuilder)
                                                  .map(Converters::toHexId);
  ClientSessionBuilder sessionSupplier = ClientSessionBuilder.of(mongoClient);

  @Test
  public void test() throws Exception {
    MongoLambda<List<JsObj>, List<String>> insertAll = (session,
                                                        jsons) -> jsons.stream()
                                                                       .map(json -> insertOne.apply(session,
                                                                                                    json))
                                                                       .collect(ListExp.seqCollector());

    var tx = TxBuilder.of(sessionSupplier)
                      .build(insertAll);

    System.out.println(tx.apply(List.of(JsObj.of("hi",
                                                 JsStr.of("bye")),
                                        JsObj.of("hi",
                                                 JsStr.of("bye"))
                                       )
                               )
                         .result()
                         .call());
  }

  @Test
  @Disabled
  public void test_Insert_In_Parallel_In_Tx_Fails() throws Exception {
    MongoLambda<List<JsObj>, List<String>> insertAll =
        (session, jsons) -> jsons.stream()
                                 .map(json -> insertOne.apply(session,
                                                              json))
                                 .collect(ListExp.parCollector());

    var tx = TxBuilder.of(sessionSupplier)
                      .build(insertAll);

    System.out.println(tx.apply(List.of(JsObj.of("hi",
                                                 JsStr.of("bye")),
                                        JsObj.of("hi",
                                                 JsStr.of("bye"))
                                       )
                               )
                         .result()
                         .call());
  }

}
