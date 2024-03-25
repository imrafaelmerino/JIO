package jio.api.jdbc;

import java.nio.charset.StandardCharsets;
import jio.jdbc.DatasourceBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;

public class BaseTest {

  static PostgreSQLContainer<?> postgresContainer;
  static DatasourceBuilder datasourceBuilder;

  @BeforeAll
  static void beforeAll() {

    PostgreSQLContainer<?> postgresContainer = PostgresContainerCreation.INSTANCE.get();

    datasourceBuilder = new DatasourceBuilder(postgresContainer.getUsername(),
                                              postgresContainer.getPassword()
                                                               .getBytes(StandardCharsets.UTF_8),
                                              postgresContainer.getJdbcUrl());

    DatabaseMigration.of(postgresContainer.getUsername(),
                         postgresContainer.getPassword(),
                         postgresContainer.getJdbcUrl())
                     .migrate();

  }

  @AfterAll
  static void afterAll() {
    if (postgresContainer != null) {
      postgresContainer.stop();
    }
  }
}
