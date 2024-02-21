package jio.api.jdbc;

import org.flywaydb.core.Flyway;

public class DatabaseMigration {

  private final Flyway flyway;

  private DatabaseMigration(String user,
                            String password,
                            String url) {
    flyway = Flyway.configure()
                   .dataSource(url,
                               user,
                               password)
                   .locations("classpath:db/migration")
                   .baselineOnMigrate(true)
                   .load();
  }

  public static DatabaseMigration of(String user,
                                     String password,
                                     String url) {
    return new DatabaseMigration(user,
                                 password,
                                 url);
  }

  public void migrate() {
    flyway.clean();
    flyway.migrate();
  }

}
