/**
 * Provides classes and utilities for working with MongoDB using the Java Input/Output (JIO) library. This package
 * contains various classes for performing MongoDB operations such as querying, inserting, updating, and aggregating
 * data, as well as handling MongoDB-specific exceptions and configuration.
 * <p>
 * Classes in this package are designed to work seamlessly with the MongoDB Java driver, providing a convenient and
 * type-safe way to interact with MongoDB databases. Additionally, each MongoDB operation performed using the classes in
 * this package creates a corresponding MongoDBEvent, which is sent to the Java Flight Recorder (JFR) system. Recording
 * of JFR events is activated by default but can be disabled using the respective methods.
 * </p>
 * <p>
 * Here's an example of how to create a mongo client:
 * <pre>
 * {@code
 * // Import required libraries
 * import jio.mongodb.DeleteMany;
 * import jio.mongodb.DeleteOne;
 * import jio.mongodb.Find;
 * import jio.mongodb.InsertMany;
 * import jio.mongodb.InsertOne;
 * import jio.mongodb.UpdateMany;
 * import jio.mongodb.UpdateOne;
 *
 *
 * // Create a MongoClient
 * MongoClient client = MongoClientBuilder.DEFAULT.build("mongodb://localhost:27017/");
 *
 * // Create a DatabaseBuilder and CollectionBuilder
 * DatabaseBuilder database = DatabaseBuilder.of(mongoClient, "test");
 * CollectionBuilder collectionSupplier = CollectionBuilder.of(database, "Data");
 *
 * // Use the classes for MongoDB operations as needed
 * // ...
 * }
 * </pre>
 * <p>
 * The core classes and interfaces in this package include:
 * <ul>
 * <li>{@link jio.mongodb.CollectionBuilder}: A supplier for MongoDB collections, providing thread-safe
 * access to MongoDB collections.</li>
 * <li>{@link jio.mongodb.DatabaseBuilder}: A supplier for MongoDB databases, providing thread-safe
 * access to MongoDB databases.</li>
 * <li>{@link jio.mongodb.Find}: A class for querying data from a MongoDB collection using find operations.</li>
 * <li>{@link jio.mongodb.InsertOne}: A class for inserting a single document into a MongoDB collection.</li>
 * <li>{@link jio.mongodb.InsertMany}: A class for inserting multiple documents into a MongoDB collection.</li>
 * <li>{@link jio.mongodb.UpdateOne}: A class for updating a single document in a MongoDB collection.</li>
 * <li>{@link jio.mongodb.UpdateMany}: A class for updating multiple documents in a MongoDB collection.</li>
 * <li>{@link jio.mongodb.DeleteOne}: A class for deleting a single document from a MongoDB collection.</li>
 * <li>{@link jio.mongodb.DeleteMany}: A class for deleting multiple documents from a MongoDB collection.</li>
 * <li>{@link jio.mongodb.FindOne}: A class for querying a single document from a MongoDB collection.</li>
 * <li>{@link jio.mongodb.FindAll}: A class for querying all documents from a MongoDB collection.</li>
 * <li>{@link jio.mongodb.Count}: A class for counting the number of documents in a MongoDB collection
 * that match a specified filter.</li>
 * <li>{@link jio.mongodb.Aggregate}: A class for performing aggregation operations on a MongoDB collection.</li>
 * <li>{@link jio.mongodb.Watcher}: A class for setting up a change stream on a MongoDB collection to monitor
 * changes.</li>
 * <li>{@link jio.mongodb.TxBuilder}: A class for creating and managing transactions in MongoDB.</li>
 * <li>{@link jio.mongodb.MongoClientBuilder}: A builder class for creating customized MongoDB client instances.</li>
 * <li>{@link jio.mongodb.ClientSessionBuilder}: A builder class for creating MongoDB client sessions, used in
 * transaction operations.</li>
 * <li>{@link jio.mongodb.MongoExceptionFun}: A utility class containing predicates for MongoDB-specific exception handling.</li>
 * </ul>
 * <p>
 * The classes in this package follow the {@link jio.mongodb.MongoLambda} interface, which allows for asynchronous
 * execution
 * and transactional operations with MongoDB. Each operation creates a corresponding MongoDBEvent, which is sent to the
 * JFR
 * system for performance monitoring and analysis.
 * </p>
 */
package jio.mongodb;
