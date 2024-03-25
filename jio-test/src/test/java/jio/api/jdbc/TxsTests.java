package jio.api.jdbc;

import java.time.Duration;
import java.util.List;
import jio.Result;
import jio.api.jdbc.dao.CustomerDatabaseOps;
import jio.api.jdbc.domain.Address;
import jio.api.jdbc.domain.Customer;
import jio.api.jdbc.domain.Email;
import jio.api.jdbc.entities.CustomerEntity;
import jio.jdbc.TxBuilder.TX_ISOLATION;
import jio.test.junit.Debugger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.postgresql.util.PSQLException;

public class TxsTests extends BaseTest {

  @RegisterExtension
  static Debugger debugger = Debugger.of(Duration.ofSeconds(10));

  @Test
  public void testInsertCustomer() throws Exception {

    var customerID =
        new InsertCustomerAndContactPoints(datasourceBuilder,
                                           TX_ISOLATION.TRANSACTION_READ_UNCOMMITTED)
            .apply(new Customer("Rafael",
                                new Email("imrafaelmerino@gmail.com"),
                                List.of(new Address("Elm's Street"),
                                        new Address("Square Center")
                                       )
                   )
                  )
            .compute()
            .getOutputOrThrow();

    Assertions.assertTrue(customerID > 0,
                          "customerId must be > 0");

    CustomerDatabaseOps customerDatabaseOps = CustomerDatabaseOps.of(datasourceBuilder);

    Result<CustomerEntity> customerResult =
        customerDatabaseOps.findCustomerAndContactPoints.apply(customerID)
                                                        .compute();

    Assertions.assertTrue(customerResult.isSuccess(customer -> customer.addresses()
                                                                       .size() == 2)
                         );

    Result<Integer> countCustomerResult = customerDatabaseOps.countCustomer.compute();

    Assertions.assertTrue(countCustomerResult.isSuccess(r -> r == 1));

  }

  @Test
  public void testInsertCustomerFailure() throws Exception {

    var insert = new InsertCustomerAndContactPointsWithFailure(datasourceBuilder,
                                                               TX_ISOLATION.TRANSACTION_READ_UNCOMMITTED)
        .apply(new Customer("Rafael",
                            new Email("imrafaelmerino@gmail.com"),
                            List.of(new Address("Elm's Street"),
                                    new Address("Square Center")
                                   )
               )
              );

    Assertions.assertTrue(insert.compute()
                                .isFailure(exc -> exc instanceof PSQLException));

    var customerDatabaseOps = CustomerDatabaseOps.of(datasourceBuilder);

    Assertions.assertTrue(customerDatabaseOps.countCustomer.compute()
                                                           .isSuccess(n -> n == 0));

  }

}
