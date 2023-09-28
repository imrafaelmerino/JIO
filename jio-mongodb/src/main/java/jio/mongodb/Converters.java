package jio.mongodb;


import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import jsonvalues.*;
import jsonvalues.spec.JsSpecs;
import mongovalues.JsValuesRegistry;
import org.bson.BsonDocumentWrapper;
import org.bson.BsonValue;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class containing a collection of converters and transformation functions
 * between JSON values ({@link JsObj}, {@link JsArray}) and MongoDB BSON objects ({@link Bson}).
 * This class simplifies the conversion process and provides convenient methods to work
 * with MongoDB and JSON values.
 */
public final class Converters {
    /**
     * Converts a JSON object ({@link JsObj}) to a MongoDB BSON object ({@link Bson}).
     */
    public static final Function<JsObj, Bson> jsObj2Bson = obj ->
            new BsonDocumentWrapper<>(obj,
                                      JsValuesRegistry.INSTANCE.get(JsObj.class)
            );
    /**
     * Converts a JSON array ({@link JsArray}) to a list of JSON objects ({@link JsObj}).
     */
    public static final Function<JsArray, List<JsObj>> jsArray2ListOfJsObj =
            array -> {
                var errors = JsSpecs.arrayOfObj().test(array);
                if (!errors.isEmpty()) throw new IllegalArgumentException(errors.toString());

                var list = new ArrayList<JsObj>();
                array.iterator()
                     .forEachRemaining(it -> list.add(it.toJsObj()));
                return list;
            };
    /**
     * Converts a JSON array ({@link JsArray}) to a list of MongoDB BSON objects ({@link Bson}).
     */
    public static final Function<JsArray, List<Bson>> jsArray2ListOfBson =
            jsArray2ListOfJsObj.andThen(list -> list.stream()
                                                    .map(it -> jsObj2Bson.apply(it.toJsObj()))
                                                    .collect(Collectors.toList()));
    /**
     * Converts a MongoDB ObjectId ({@link BsonValue}) to its hexadecimal representation as a string.
     */
    public static final Function<BsonValue, String> objectId2Hex =
            bsonValue -> bsonValue.asObjectId()
                                  .getValue()
                                  .toHexString();
    /**
     * Converts a MongoDB {@link InsertOneResult} into a JSON object ({@link JsObj})
     * representing the result of a single document insertion operation.
     * The resulting JSON object contains information about the inserted document's ID
     * and the acknowledgment status.
     */
    public static final Function<InsertOneResult, String> insertOneResult2HexId =
            result -> objectId2Hex.apply(result.getInsertedId());
    /**
     * Converts a MongoDB {@link UpdateResult} into a JSON object ({@link JsObj})
     * representing the result of an update operation.
     * The resulting JSON object contains information about the upserted ID, matched count,
     * modified count, acknowledgment status, and the type of the result.
     */
    public static final Function<UpdateResult, Optional<String>> updateResult2OptHexId = it -> {
        var upsertedId = it.getUpsertedId();
        if (upsertedId == null) return Optional.empty();
        return Optional.of(objectId2Hex.apply(upsertedId));
    };
    /**
     * Converts the first result of a MongoDB {@link FindIterable} into a JSON object ({@link JsObj}).
     * This function is useful when querying for a single document and converting the result to JSON.
     */
    public static final Function<FindIterable<JsObj>, JsObj> iterableFirst = MongoIterable::first;
    /**
     * Converts a MongoDB {@link FindIterable} into a JSON array ({@link JsArray}).
     * This function is useful when querying multiple documents and converting the result to a JSON array.
     */
    public static final Function<FindIterable<JsObj>, JsArray> iterable2JsArray = JsArray::ofIterable;
    /**
     * Converts a MongoDB {@link InsertManyResult} into a JSON array ({@link JsArray}) of hexadecimal IDs.
     * This function is used to represent the IDs of inserted documents in JSON format.
     */
    public static final Function<InsertManyResult, JsArray> insertManyResult2JsArrayOfHexIds =
            result -> {
                var map = result.getInsertedIds();
                var array = JsArray.empty();
                for (var e : map.entrySet()) {
                    array = array.append(JsStr.of(objectId2Hex.apply(e.getValue())));
                }
                return array;
            };
    /**
     * Converts a MongoDB {@link AggregateIterable} into a JSON array ({@link JsArray}).
     * This function is useful when performing aggregation operations and converting the results to a JSON array.
     */
    public static final Function<AggregateIterable<JsObj>, JsArray>
            aggregateResult2JsArray = JsArray::ofIterable;
    private static final String ID = "_id";
    private static final String OID = "$oid";

    /**
     * Converts a hexadecimal string ID into a JSON object ({@link JsObj}) with the format {"_id": {"$oid": "id"}}.
     * This function is used to represent MongoDB ObjectIds in JSON format.
     */
    public static final Function<String, JsObj> str2Oid =
            id -> JsObj.of(ID,
                           JsObj.of(OID,
                                    JsStr.of(id)
                                   )
                          );
    private static final String TYPE_FIELD = "type";
    private static final String INSERTED_ID_FIELD = "insertedId";
    private static final String WAS_ACKNOWLEDGED_FIELD = "wasAcknowledged";

    /**
     * Converts a MongoDB {@link InsertOneResult} into a JSON object ({@link JsObj}).
     * This function is used to represent the result of an insert operation for a single document in JSON format.
     */
    public static final Function<InsertOneResult, JsObj> insertOneResult2JsObj =
            result -> JsObj.of(INSERTED_ID_FIELD,
                               JsStr.of(insertOneResult2HexId.apply(result)),
                               WAS_ACKNOWLEDGED_FIELD,
                               JsBool.of(result.wasAcknowledged()),
                               TYPE_FIELD,
                               JsStr.of(result.getClass()
                                              .getSimpleName()
                                       )
                              );

    private static final String DELETED_COUNT_FIELD = "deleted_count";

    /**
     * Converts a MongoDB {@link DeleteResult} into a JSON object ({@link JsObj}).
     * This function is used to represent the result of a delete operation in JSON format.
     */
    public static final Function<DeleteResult, JsObj> deleteResult2JsObj =
            result -> JsObj.of(DELETED_COUNT_FIELD,
                               JsLong.of(result.getDeletedCount()),
                               WAS_ACKNOWLEDGED_FIELD,
                               JsBool.of(result.wasAcknowledged())
                              );
    private static final String MATCHED_COUNT_FIELD = "matchedCount";
    private static final String UPSERTED_ID_FIELD = "upsertedId";
    private static final String MODIFIED_COUNT_FIELD = "modifiedCount";
    /**
     * Converts a MongoDB {@link UpdateResult} into a JSON object ({@link JsObj}).
     * This function is used to represent the result of an update operation in JSON format.
     */
    public static final Function<UpdateResult, JsObj> updateResult2JsObj = result -> {
        var optStr = updateResult2OptHexId.apply(result);
        return JsObj.of(UPSERTED_ID_FIELD,
                        optStr.isPresent() ?
                                JsStr.of(optStr.get()) :
                                JsNull.NULL,
                        MATCHED_COUNT_FIELD,
                        JsLong.of(result.getMatchedCount()),
                        MODIFIED_COUNT_FIELD,
                        JsLong.of(result.getModifiedCount()),
                        WAS_ACKNOWLEDGED_FIELD,
                        JsBool.of(result.wasAcknowledged()),
                        TYPE_FIELD,
                        JsStr.of(result.getClass()
                                       .getSimpleName()
                                )
                       );
    };


    private Converters() {
    }
}
