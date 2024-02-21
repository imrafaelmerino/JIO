package jio.api.jdbc;

import java.time.Duration;
import java.util.List;
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

public class TestTxs extends BaseTest {

  @RegisterExtension
  static Debugger debugger = Debugger.of(Duration.ofSeconds(10));

  @Test
  public void testInsertCustomer() throws Exception {

    var customerID = new InsertCustomerAndContactPoints(datasourceBuilder,
                                                        TX_ISOLATION.TRANSACTION_READ_UNCOMMITTED)
                                                                                                  .apply(new Customer("Rafael",
                                                                                                                      new Email("imrafaelmerino@gmail.com"),
                                                                                                                      List.of(new Address("Elm's Street"),
                                                                                                                              new Address("Square Center")
                                                                                                                      )
                                                                                                  )
                                                                                                  )
                                                                                                  .result()
                                                                                                  .call();

    Assertions.assertTrue(customerID > 0,
                          "customerId must be > 0");

    CustomerDatabaseOps customerDatabaseOps = CustomerDatabaseOps.of(datasourceBuilder);

    CustomerEntity customerEntity = customerDatabaseOps.findCustomerAndContactPoints.apply(customerID)
                                                                                    .result()
                                                                                    .call();

    Assertions.assertEquals(2,
                            customerEntity.addresses()
                                          .size());
    Assertions.assertNotNull(customerEntity.email()
                                           .id());

    int r = customerDatabaseOps.countCustomer.result()
                                             .call();
    Assertions.assertEquals(1,
                            r);

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

    Assertions.assertThrows(PSQLException.class,
                            () -> insert.result()
                                        .call());

    CustomerDatabaseOps customerDatabaseOps = CustomerDatabaseOps.of(datasourceBuilder);

    int r = customerDatabaseOps.countCustomer.result()
                                             .call();
    Assertions.assertEquals(0,
                            r);

  }

}
