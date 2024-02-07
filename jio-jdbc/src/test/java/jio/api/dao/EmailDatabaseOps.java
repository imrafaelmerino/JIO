package jio.api.dao;

import java.time.Duration;
import java.util.function.LongFunction;
import jio.api.domain.Email;
import jio.jdbc.ClosableStatement;
import jio.jdbc.InsertOneStmBuilder;

public final class EmailDatabaseOps {

  /**
   * customer ID -> email -> email ID
   */
  public static final LongFunction<ClosableStatement<Email, Long>> insertOne =
      customerID ->
          InsertOneStmBuilder.<Email, Long>of("INSERT INTO email (email_address, customer_id) VALUES (?, ?) RETURNING id;",
                                              email -> (paramPosition, preparedStatement) -> {
                                                preparedStatement.setString(paramPosition++,
                                                                            email.address());
                                                preparedStatement.setLong(paramPosition++,
                                                                          customerID);
                                                return paramPosition;
                                              },
                                              email -> resultSet -> resultSet.getLong("id"),
                                              Duration.ofSeconds(10000)
                                             )
                             .buildClosable();

}
