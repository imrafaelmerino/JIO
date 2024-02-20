package jio.api.jdbc;

import java.util.function.Supplier;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

final class PostgresContainerCreation implements Supplier<PostgreSQLContainer<?>> {

  public static final PostgresContainerCreation INSTANCE = new PostgresContainerCreation();

  private static final String DOCKER_IMAGE_NAME = "postgres:15";
  private volatile PostgreSQLContainer<?> postgres;

  private PostgresContainerCreation() {

  }

  @Override
  public PostgreSQLContainer<?> get() {

    postgres = new PostgreSQLContainer<>(DOCKER_IMAGE_NAME);
    postgres.start();

    var url = postgres.getJdbcUrl();

    Wait.forLogMessage("JDBC URL: %s".formatted(url),
                       1);
    Wait.forListeningPort()
        .waitUntilReady(postgres);

    return postgres;

  }

}
