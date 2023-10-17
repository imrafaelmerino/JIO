/**
 * Provides classes and utilities for working with MongoDB using the Java Input/Output (JIO) library. This package
 * contains various classes for performing MongoDB operations such as querying, inserting, updating, and aggregating
 * data, as well as handling MongoDB-specific exceptions and configuration.
 * <p>
 * Classes in this package are designed to work seamlessly with the MongoDB Java driver, providing a convenient and
 * type-safe way to interact with MongoDB databases. Additionally, each MongoDB operation performed using the classes in
 * this package creates a corresponding MongoDBEvent, which is sent to the Java Flight Recorder (JFR) system. Recording
 * of JFR events is activated by default but can be disabled using the
 * {@link jio.mongodb.Aggregate#withoutRecordedEvents()} method.
 * </p>
 * <p>
 * To work with JSON objects from the json-values library, it's necessary to register the codecs from mongo-values.
 * Here's an example of how to create a MongoDB client with JSON codec support:
 * <pre>
 * {@code
 *
 * import mongovalues.JsValuesRegistry;
 * import jio.mongodb.DatabaseSupplier;
 * import jio.mongodb.CollectionSupplier;
 *
 * ConnectionString connString = new ConnectionString("mongodb://localhost:27017/");
 *
 * MongoClientSettings settings = MongoClientSettings.builder()
 *                                                   .applyConnectionString(connString)
 *                                                   .codecRegistry(JsValuesRegistry.INSTANCE)
 *                                                   .build();
 *
 * MongoClient mongoClient = MongoClients.create(settings);
 * DatabaseSupplier database = new DatabaseSupplier(mongoClient, "test");
 * CollectionSupplier collectionSupplier = new CollectionSupplier(database, "Data");
 *
 * }
 * </pre>
 * <p>
 * The core classes and interfaces in this package include:
 * <ul>
 *     <li>{@link jio.mongodb.CollectionSupplier}: A supplier for MongoDB collections, providing thread-safe
 *     access to MongoDB collections.</li>
 *     <li>{@link jio.mongodb.Find}: A class for querying data from a MongoDB collection using find operations.</li>
 *     <li>{@link jio.mongodb.InsertOne}: A class for inserting a single document into a MongoDB collection.</li>
 *     <li>{@link jio.mongodb.InsertMany}: A class for inserting multiple documents into a MongoDB collection.</li>
 *     <li>{@link jio.mongodb.UpdateOne}: A class for updating a single document in a MongoDB collection.</li>
 *     <li>{@link jio.mongodb.UpdateMany}: A class for updating multiple documents in a MongoDB collection.</li>
 *     <li>{@link jio.mongodb.DeleteOne}: A class for deleting a single document from a MongoDB collection.</li>
 *     <li>{@link jio.mongodb.DeleteMany}: A class for deleting multiple documents from a MongoDB collection.</li>
 *     <li>{@link jio.mongodb.Count}: A class for counting the number of documents in a MongoDB collection
 *     that match a specified filter.</li>
 *     <li>{@link jio.mongodb.Aggregate}: A class for performing aggregation operations on a MongoDB collection.</li>
 *     <li>{@link jio.mongodb.Watcher}: A class for setting up a change stream on a MongoDB collection to monitor changes.</li>
 *     <li>{@link jio.mongodb.MongoExceptions}: A utility class containing predicates for MongoDB-specific exception handling.</li>
 * </ul>
 * <p>
 * Additionally, this package provides various utility classes and converters to facilitate data conversion
 * between JSON values and BSON, which is the native format used by MongoDB.
 * </p>
 * <p>
 * The MongoDB-related classes in this package are designed to work seamlessly with the JIO library,
 * providing functional programming and asynchronous IO capabilities for MongoDB operations. Each operation creates
 * a corresponding MongoDBEvent, which is sent to the JFR system for performance monitoring and analysis.
 * </p>
 */
package jio.mongodb;
