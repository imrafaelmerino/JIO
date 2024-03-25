package jio.api.jdbc.dao;

import java.time.Duration;
import java.util.List;
import java.util.function.LongFunction;
import jio.api.jdbc.domain.Address;
import jio.jdbc.BatchOfOneEntityBuilder;
import jio.jdbc.BatchResult;
import jio.jdbc.ClosableStatement;

public class AddressesDatabaseOps {

  /**
   * customer ID -> list of address -> BatchResult
   */
  public static final LongFunction<ClosableStatement<List<Address>, BatchResult>> insertMany = customerID -> BatchOfOneEntityBuilder.<Address>of("INSERT INTO address (street, customer_id) VALUES ( ?, ?) RETURNING id;",
                                                                                                                                                 address -> (paramPosition,
                                                                                                                                                             preparedStatement) -> {
                                                                                                                                                   preparedStatement.setString(paramPosition++,
                                                                                                                                                                               address.street());
                                                                                                                                                   preparedStatement.setLong(paramPosition++,
                                                                                                                                                                             customerID);
                                                                                                                                                   return paramPosition;
                                                                                                                                                 },
                                                                                                                                                 Duration.ofSeconds(1000)
  )
                                                                                                                                    .withEventLabel("batch customer addresses")
                                                                                                                                    .buildClosable();

}
