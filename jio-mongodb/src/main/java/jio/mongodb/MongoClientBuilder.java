package jio.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import mongovalues.JsValuesRegistry;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * A builder class for creating MongoDB client instances with custom configurations. This class provides flexibility in
 * building MongoDB client instances and allows you to specify your own connection string and settings functions. It is
 * particularly useful when you want to configure the client for specific requirements, such as custom codec
 * registries.
 * <p>
 * The default instance, {@link #DEFAULT}, is pre-configured with the default settings and codecs to work with JSON data
 * from the json-values library.
 *
 * @see MongoClient
 * @see ConnectionString
 * @see MongoClientSettings
 * @see JsValuesRegistry
 */
public final class MongoClientBuilder {

  /**
   * Default builder with the default configuration and the codecs to work with JSON from json-values.
   */
  public static final MongoClientBuilder DEFAULT = new MongoClientBuilder(
                                                                          con -> MongoClientSettings.builder()
                                                                                                    .applyConnectionString(con)
                                                                                                    .codecRegistry(JsValuesRegistry.INSTANCE)
                                                                                                    .build()
  );

  final Function<String, ConnectionString> buildConnection;
  final Function<ConnectionString, MongoClientSettings> buildSettings;

  private MongoClientBuilder(Function<String, ConnectionString> buildConnection,
                             Function<ConnectionString, MongoClientSettings> buildSettings) {
    this.buildConnection = requireNonNull(buildConnection);
    this.buildSettings = requireNonNull(buildSettings);
  }

  private MongoClientBuilder(Function<ConnectionString, MongoClientSettings> buildSettings) {
    this(ConnectionString::new,
         buildSettings);

  }

  /**
   * Creates a new instance of {@code MongoClientBuilder} with custom connection string and settings functions. This
   * factory method is useful when you need more configuration than the default provided by the {@link #DEFAULT}
   * builder.
   *
   * @param buildConnection The function to build a connection string from a connection string URI.
   * @param buildSettings   The function to build client settings from a connection string.
   * @return A new instance of {@code MongoClientBuilder} with the custom configuration functions.
   */
  public static MongoClientBuilder of(final Function<String, ConnectionString> buildConnection,
                                      final Function<ConnectionString, MongoClientSettings> buildSettings
  ) {
    return new MongoClientBuilder(requireNonNull(buildConnection),
                                  requireNonNull(buildSettings));
  }

  /**
   * Creates a new instance of {@code MongoClientBuilder} with a custom settings function. This factory method is useful
   * when you need more configuration than the default provided by the {@link #DEFAULT} builder.
   * <p>
   * When using this factory method, don't forget to include the configuration
   * `codecRegistry(JsValuesRegistry.INSTANCE)` in your custom settings, as jio-mongodb only works with JSON data from
   * the json-values library.
   *
   * @param buildSettings The function to build client settings from a connection string.
   * @return A new instance of {@code MongoClientBuilder} with the custom settings function.
   */
  public static MongoClientBuilder of(final Function<ConnectionString, MongoClientSettings> buildSettings) {
    return new MongoClientBuilder(requireNonNull(buildSettings));
  }

  /**
   * builds the Mongo client from the specified connection
   *
   * @param connection the string connections. Something like mongodb://localhost:27017/?
   * @return a mongo client
   */
  public MongoClient build(final String connection) {
    return MongoClients.create(buildConnection.andThen(buildSettings)
                                              .apply(requireNonNull(connection))
    );
  }
}
