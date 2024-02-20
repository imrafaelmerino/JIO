package jio.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.sql.DataSource;

/**
 * A builder class for creating and configuring a {@link javax.sql.DataSource} using HikariCP. The creation of the
 * DataSource is lazy, and a double-check idiom is used to ensure thread safety during creation.
 */
public final class DatasourceBuilder implements Supplier<DataSource> {

  private final String user;
  private final byte[] sec;
  private final String url;
  private final int DEFAULT_MAX_POOL_SIZE = 20;

  private static final VarHandle DATASOURCE;

  private HikariDataSource getDatasourceAcquire() {
    return (HikariDataSource) DATASOURCE.getAcquire(this);
  }

  private void setDatasourceRelease(HikariDataSource value) {
    DATASOURCE.setRelease(this,
                          value);
    assert (dataSource != null);
  }

  static {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      DATASOURCE = lookup.findVarHandle(DatasourceBuilder.class,
                                        "dataSource",
                                        HikariDataSource.class);
    } catch (ReflectiveOperationException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private volatile HikariDataSource dataSource;
  private int connectionTimeout;
  private int maxPoolSize = DEFAULT_MAX_POOL_SIZE;
  private Consumer<HikariConfig> addProps;

  /**
   * Constructs a {@code DatasourceBuilder} with the specified user, password, and JDBC URL.
   *
   * @param user The username for database connection.
   * @param sec  The password for database connection as a byte array.
   * @param url  The JDBC URL for connecting to the database.
   * @throws IllegalArgumentException If the password is empty or the URL is empty.
   */
  public DatasourceBuilder(final String user,
                           final byte[] sec,
                           final String url
  ) {
    this.user = Objects.requireNonNull(user);
    this.sec = Objects.requireNonNull(sec);
    this.url = Objects.requireNonNull(url);
    if (sec.length == 0) {
      throw new IllegalArgumentException("password is empty");
    }
    if (url.trim()
           .isEmpty()) {
      throw new IllegalArgumentException("url is empty");
    }
    Runtime.getRuntime()
           .addShutdownHook(new Thread(() -> {
             HikariDataSource ref = getDatasourceAcquire();
             if (ref != null) {
               ref.close();
             }
           }));

  }

  /**
   * Sets the connection timeout for the DataSource.
   *
   * @param connectionTimeout The connection timeout in milliseconds.
   * @return This {@code DatasourceBuilder} instance for method chaining.
   * @throws IllegalArgumentException If the connection timeout is less than or equal to 0.
   */
  public DatasourceBuilder setConnectionTimeout(final int connectionTimeout) {
    if (maxPoolSize <= 0) {
      throw new IllegalArgumentException("connectionTimeout <= 0");
    }
    this.connectionTimeout = connectionTimeout;
    return this;
  }

  /**
   * Sets the maximum pool size for the DataSource.
   *
   * @param maxPoolSize The maximum pool size.
   * @return This {@code DatasourceBuilder} instance for method chaining.
   * @throws IllegalArgumentException If the maximum pool size is less than or equal to 0.
   */
  public DatasourceBuilder setMaxPoolSize(final int maxPoolSize) {
    if (maxPoolSize <= 0) {
      throw new IllegalArgumentException("maxPoolSize <= 0");
    }
    this.maxPoolSize = maxPoolSize;
    return this;
  }

  /**
   * Sets additional properties for configuring the HikariConfig.
   *
   * @param addProps The consumer function to apply additional properties to the HikariConfig.
   * @return This {@code DatasourceBuilder} instance for method chaining.
   */
  public DatasourceBuilder setAddProps(final Consumer<HikariConfig> addProps) {
    this.addProps = Objects.requireNonNull(addProps);
    return this;
  }

  /**
   * Gets the DataSource. The creation of the DataSource is lazy, and a double-check idiom is used for thread safety.
   *
   * @return The configured DataSource.
   */
  @Override
  public DataSource get() {
    HikariDataSource localRef = getDatasourceAcquire();
    if (localRef == null) {
      synchronized (this) {
        localRef = getDatasourceAcquire();
        if (localRef == null) {
          localRef = new HikariDataSource(getHikariConfig());
          setDatasourceRelease(localRef);
        }
      }
    }
    return localRef;

  }

  private HikariConfig getHikariConfig() {
    var config = new HikariConfig();
    config.setJdbcUrl(url);
    config.setUsername(user);
    config.setPassword(new String(sec,
                                  StandardCharsets.UTF_8));
    config.setConnectionTimeout(connectionTimeout);
    config.setMaximumPoolSize(maxPoolSize);
    if (addProps != null) {
      addProps.accept(config);
    }
    return config;
  }

}