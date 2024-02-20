package jio.mongodb;

import com.mongodb.ClientSessionOptions;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A builder class for creating MongoDB client sessions with specified options. This class simplifies the process of
 * creating client sessions and provides a convenient way to start and configure sessions with a MongoDB client.
 * Typically, this class is used internally by the `TxBuilder` to build sessions to manage transactions.
 * <p><b>Note:</b> MongoDB sessions are not multithreading. Only one thread should operate within a MongoDB session at
 * a time to avoid errors like "Only servers in a sharded cluster can start a new transaction at the active transaction
 * number."
 *
 * @see MongoClient
 * @see ClientSession
 * @see ClientSessionOptions
 * @see TxBuilder
 */
public final class ClientSessionBuilder implements Supplier<ClientSession> {

  final MongoClient client;
  final ClientSessionOptions options;

  /**
   * Constructs a new `ClientSessionSupplier` with the specified MongoDB client and session options.
   *
   * @param client  The MongoDB client used for creating sessions.
   * @param options The options to be used for the client session.
   */
  ClientSessionBuilder(final MongoClient client,
                       final ClientSessionOptions options
  ) {
    this.client = Objects.requireNonNull(client);
    this.options = Objects.requireNonNull(options);
  }

  /**
   * Creates a new `ClientSessionBuilder` instance with the specified MongoDB client and session options.
   *
   * @param client  The MongoDB client used for creating sessions.
   * @param options The options to be used for the client session.
   * @return A new `ClientSessionBuilder` instance.
   */
  public static ClientSessionBuilder of(final MongoClient client,
                                        final ClientSessionOptions options
  ) {
    return new ClientSessionBuilder(client,
                                    options);
  }

  /**
   * Creates a new `ClientSessionBuilder` instance with the specified MongoDB client and default session options.
   *
   * @param client The MongoDB client used for creating sessions.
   * @return A new `ClientSessionBuilder` instance with default session options.
   */
  public static ClientSessionBuilder of(final MongoClient client) {
    return new ClientSessionBuilder(client,
                                    ClientSessionOptions.builder()
                                                        .build());
  }

  @Override
  public ClientSession get() {
    return client.startSession(options);
  }
}
