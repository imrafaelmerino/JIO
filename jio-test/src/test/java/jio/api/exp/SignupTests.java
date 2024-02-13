package jio.api.exp;


import fun.gen.BoolGen;import fun.gen.Gen;import fun.gen.IntGen;import fun.gen.StrGen;import java.util.concurrent.Executors;import java.util.function.Supplier;import jio.IO;
import jio.Lambda;
import jio.test.junit.Debugger;
import jio.test.stub.StubBuilder;import jio.time.Clock;
import jsonvalues.JsArray;
import jsonvalues.JsObj;
import jsonvalues.JsStr;
import jsonvalues.gen.JsArrayGen;import jsonvalues.gen.JsStrGen;import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.Duration;

/**
 * The signup service processes a JSON input that has at the least (not interested in the rest) the fields email and
 * address (both a string). The provided address is initially a string, and the service validates and standardizes it
 * using the Google Geocode API. The results from Google are then sent to the frontend for the user's selection or
 * rejection. **If any errors occur during this process, an empty array of addresses is returned**.
 * <p>
 * The service also stores the client's information in a MongoDB database. The identifier returned by MongoDB serves as
 * the client identifier and must be sent back to the frontend. If the client is successfully saved in the database and
 * the user doesn't exist in the LDAP system, the service performs two additional asynchronous actions:
 * <p>
 * 1. It sends the user to the LDAP service and an activation email is sent to the user. Note that these operations run
 * asynchronously and do not block the main flow of the service. Data returned from these operations is neither
 * persisted nor returned to the end user.
 * <p>
 * The service also provides information about the total number of existing clients in the MongoDB database, which the
 * frontend can use to display a welcoming message to the user. For example: "You're the user number 3000!" If any
 * errors occur **the service returns -1**, and the frontend does not display the message.
 * <p>
 * Crucially, the signup service is designed to execute all these operations in parallel. This includes the request to
 * Google for address validation and the MongoDB operations (both persistence and counting).
 * <p>
 * The response from the signup service adheres to this structure:
 * <p>
 * ```json { "number_users": integer, // Total number of existing clients in the DB (from MongoDB) "id": string, //
 * MongoDB ID "timestamp": instant, // Timestamp when the frontend request reaches the server "addresses": array //
 * Client addresses returned by Google Geocode API } ```
 * <p>
 * <p>
 * Additionally, the service is built to be resilient in the face of errors. The developer can easily specify the types
 * of errors to handle, the number of retry attempts, the time between each retry (retry policy), and the timeout for
 * each individual operation and for the overall service.
 */

//only for java 21
public class SignupTests {


  @RegisterExtension
  static Debugger debugger = Debugger.of(Duration.ofSeconds(10));


  @Test
  public void testReadme() {

    //let's change the delay of every stub to 1 sec, for the sake of clarity
    Gen<Duration> delayGen = Gen.cons(1).map(Duration::ofSeconds);

    Supplier<IO<Integer>> countUsers =
        () -> StubBuilder.ofGen(Gen.seq(n -> n <= 4 ?
                                            IO.fail(new RuntimeException(n + "")) :
                                            IO.succeed(n)
                                      )
                              )
                        .withDelays(delayGen)
                        .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                        .get();

    Lambda<JsObj, String> persistMongo =
        a -> StubBuilder.ofSucGen(StrGen.alphabetic(20, 20))
                        .withDelays(delayGen)
                        .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                        .get();

    Lambda<JsObj, Void> sendEmail =
        a -> StubBuilder.<Void>ofSucGen(Gen.cons(null))
                        .withDelays(delayGen)
                        .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                        .get();

    Lambda<String, Boolean> existsInLDAP =
        a -> StubBuilder.ofSucGen(BoolGen.arbitrary())
                        .withDelays(delayGen)
                        .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                        .get();

    Lambda<JsObj, Void> persistLDAP =
        a -> StubBuilder.<Void>ofSucGen(Gen.cons(null))
                        .withDelays(delayGen)
                        .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                        .get();

    Lambda<String, JsArray> normalizeAddresses =
        a -> StubBuilder.ofSucGen(JsArrayGen.ofN(JsStrGen.alphabetic(), 3))
                        .withDelays(delayGen)
                        .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                        .get();

    JsObj user = JsObj.of("email",
                          JsStr.of("imrafaelmerino@gmail.com"),
                          "address",
                          JsStr.of("Elm's Street")
                         );

    var resp = new SignupService(persistLDAP,
                                 normalizeAddresses,
                                 countUsers,
                                 persistMongo,
                                 sendEmail,
                                 existsInLDAP,
                                 Clock.realTime)
        .apply(user)
        .join();

    Assertions.assertTrue(resp.containsKey("number_users"));
    Assertions.assertTrue(resp.containsKey("id"));
    Assertions.assertTrue(resp.containsKey("addresses"));
    Assertions.assertTrue(resp.containsKey("timestamp"));



  }


  @Test
  public void test(){


  }



}
