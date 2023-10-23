package jio.mongodb;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Utility class containing a collection of converters and transformation functions between JSON values ({@link JsObj},
 * {@link JsArray}) and MongoDB BSON objects ({@link Bson}), as well as MongoDB result types such as
 * {@link UpdateResult}, {@link DeleteResult}, and more. This class simplifies the conversion process and provides
 * convenient methods to work with MongoDB and JSON values.
 */
public final class Converters {
    private static final String ID = "_id";
    private static final String OID = "$oid";
    private static final String TYPE_FIELD = "type";
    private static final String INSERTED_ID_FIELD = "insertedId";
    private static final String WAS_ACKNOWLEDGED_FIELD = "wasAcknowledged";
    private static final String DELETED_COUNT_FIELD = "deleted_count";
    private static final String MATCHED_COUNT_FIELD = "matchedCount";
    private static final String UPSERTED_ID_FIELD = "upsertedId";
    private static final String MODIFIED_COUNT_FIELD = "modifiedCount";

    private Converters() {
    }

    /**
     * Converts a JSON object ({@link JsObj}) to a MongoDB BSON object ({@link Bson}).
     */
    public static Bson toBson(final JsObj obj) {
        return new BsonDocumentWrapper<>(obj, JsValuesRegistry.INSTANCE.get(JsObj.class));
    }

    /**
     * Converts a JSON array ({@link JsArray}) to a list of JSON objects ({@link JsObj}).
     */
    public static List<JsObj> toListOfJsObj(final JsArray array) {
        var errors = JsSpecs.arrayOfObj().test(array);
        if (!errors.isEmpty()) throw new IllegalArgumentException(errors.toString());

        var list = new ArrayList<JsObj>();
        array.iterator().forEachRemaining(it -> list.add(it.toJsObj()));
        return list;
    }

    /**
     * Converts a JSON array ({@link JsArray}) to a list of MongoDB BSON objects ({@link Bson}).
     */
    public static List<Bson> toListOfBson(final JsArray array) {
        var list = toListOfJsObj(requireNonNull(array));
        return list.stream()
                   .map(Converters::toBson)
                   .collect(Collectors.toList());
    }

    /**
     * Converts a MongoDB ObjectId ({@link BsonValue}) to its hexadecimal representation as a string.
     */
    public static String toHexId(final BsonValue bsonValue) {
        return requireNonNull(bsonValue).asObjectId().getValue().toHexString();
    }

    /**
     * Converts a MongoDB {@link InsertOneResult} into a JSON object ({@link JsObj}) representing the result of a single
     * document insertion operation. The resulting JSON object contains information about the inserted document's ID and
     * the acknowledgment status.
     */
    public static String toHexId(final InsertOneResult result) {
        return toHexId(result.getInsertedId());
    }

    /**
     * Converts a MongoDB {@link UpdateResult} into a JSON object ({@link JsObj}) representing the result of an update
     * operation. The resulting JSON object contains information about the upserted ID, matched count, modified count,
     * acknowledgment status, and the type of the result.
     */
    public static Optional<String> toHexId(final UpdateResult it) {
        var upsertedId = Objects.requireNonNull(it).getUpsertedId();
        if (upsertedId == null) return Optional.empty();
        return Optional.of(toHexId(upsertedId));
    }

    /**
     * Converts a MongoDB {@link FindIterable} into a JSON array ({@link JsArray}). This function is useful when
     * querying multiple documents and converting the result to a JSON array.
     */
    public static JsArray toJsArray(final FindIterable<JsObj> iterable) {
        return JsArray.ofIterable(iterable);
    }

    /**
     * Converts a MongoDB {@link FindIterable} into a List of ({@link JsObj}). This function is useful when querying
     * multiple documents and converting the result to a List.
     */
    public static List<JsObj> toListOfJsObj(final FindIterable<JsObj> iterable) {
        List<JsObj> result = new ArrayList<>();
        for (JsObj obj : requireNonNull(iterable)) {
            result.add(obj);
        }
        return result;
    }

    /**
     * Converts a MongoDB {@link InsertManyResult} into a JSON array ({@link JsArray}) of hexadecimal IDs. This function
     * is used to represent the IDs of inserted documents in JSON format.
     */
    public static List<String> toListOfHexIds(final InsertManyResult result) {
        var map = requireNonNull(result).getInsertedIds();
        var list = new ArrayList<String>();
        for (var e : map.entrySet()) {
            list.add(toHexId(e.getValue()));
        }
        return list;
    }

    /**
     * Converts a MongoDB {@link AggregateIterable} into a JSON array ({@link JsArray}). This function is useful when
     * performing aggregation operations and converting the results to a JSON array.
     */
    public static JsArray toJsArray(final AggregateIterable<JsObj> aggregateIterable) {
        return JsArray.ofIterable(requireNonNull(aggregateIterable));
    }

    public static List<JsObj> toListOfJsObj(final AggregateIterable<JsObj> aggregateIterable) {
        List<JsObj> list = new ArrayList<>();
        for (JsObj obj : requireNonNull(aggregateIterable)) {
            list.add(obj);
        }
        return list;
    }

    /**
     * Converts a hexadecimal string ID into a JSON object ({@link JsObj}) with the format {"_id": {"$oid": "id"}}. This
     * function is used to represent MongoDB ObjectIds in JSON format.
     */
    public static JsObj toObjId(final String id) {
        Objects.requireNonNull(id);
        return JsObj.of(ID, JsObj.of(OID, JsStr.of(id)));
    }

    /**
     * Converts a MongoDB {@link InsertOneResult} into a JSON object ({@link JsObj}). This function is used to represent
     * the result of an insert operation for a single document in JSON format.
     */
    public static JsObj toJsObj(final InsertOneResult result) {
        Objects.requireNonNull(result);
        return JsObj.of(INSERTED_ID_FIELD, JsStr.of(toHexId(result)),
                        WAS_ACKNOWLEDGED_FIELD, JsBool.of(result.wasAcknowledged()),
                        TYPE_FIELD, JsStr.of(result.getClass().getSimpleName())
                       );
    }

    /**
     * Converts a MongoDB {@link DeleteResult} into a JSON object ({@link JsObj}). This function is used to represent
     * the result of a delete operation in JSON format.
     */
    public static JsObj toJsObj(final DeleteResult result) {
        Objects.requireNonNull(result);
        return JsObj.of(DELETED_COUNT_FIELD, JsLong.of(result.getDeletedCount()),
                        WAS_ACKNOWLEDGED_FIELD, JsBool.of(result.wasAcknowledged())
                       );
    }

    /**
     * Converts a MongoDB {@link UpdateResult} into a JSON object ({@link JsObj}). This function is used to represent
     * the result of an update operation in JSON format.
     */
    public static JsObj toJsObj(UpdateResult result) {
        var optStr = toHexId(result);
        return JsObj.of(UPSERTED_ID_FIELD, optStr.isPresent() ? JsStr.of(optStr.get()) : JsNull.NULL,
                        MATCHED_COUNT_FIELD, JsLong.of(result.getMatchedCount()),
                        MODIFIED_COUNT_FIELD, JsLong.of(result.getModifiedCount()),
                        WAS_ACKNOWLEDGED_FIELD, JsBool.of(result.wasAcknowledged()),
                        TYPE_FIELD, JsStr.of(result.getClass().getSimpleName())
                       );
    }
}
