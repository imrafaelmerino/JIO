package jio.api.dao;

import java.time.Duration;
import java.util.List;
import java.util.function.LongFunction;
import jio.api.domain.Address;
import jio.jdbc.BatchResult;
import jio.jdbc.BatchStmBuilder;
import jio.jdbc.ClosableStatement;
import jio.jdbc.InsertOneEntityBuilder;

public class AddressesDatabaseOps {

  /**
   * customer ID -> address -> address ID
   */
  public static final LongFunction<ClosableStatement<Address, Long>> insertOne =
      customerID ->
          InsertOneEntityBuilder.<Address, Long>of("INSERT INTO address (street, customer_id) VALUES ( ?, ?) RETURNING id;",
                                                address -> (paramPosition, preparedStatement) -> {
                                                  preparedStatement.setString(paramPosition++,
                                                                              address.street());
                                                  preparedStatement.setLong(paramPosition++,
                                                                            customerID);
                                                  return paramPosition;
                                                },
                                                address -> resultSet -> resultSet.getLong("id"),
                                                   Duration.ofSeconds(1000)
                                                  )
                                .buildClosable();

  public static final LongFunction<ClosableStatement<List<Address>, BatchResult>> insertMany =
      customerID ->
          BatchStmBuilder.<Address>of("INSERT INTO address (street, customer_id) VALUES ( ?, ?) RETURNING id;",
                                      address -> (paramPosition, preparedStatement) -> {
                                        preparedStatement.setString(paramPosition++,
                                                                    address.street());
                                        preparedStatement.setLong(paramPosition++,
                                                                  customerID);
                                        return paramPosition;
                                      },
                                      Duration.ofSeconds(1000)
                                     )
                         .buildClosable();


}
