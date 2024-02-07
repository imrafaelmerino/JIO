package jio.api;


import java.util.List;
import jio.api.dao.CustomerDatabaseOps;
import jio.api.domain.Address;
import jio.api.domain.Customer;
import jio.api.domain.Email;
import jio.api.entities.CustomerEntity;
import jio.jdbc.TxBuilder.TX_ISOLATION;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestTxs extends BaseTest {


  @Test
  public void testInsertCustomer() {

    var customerID =
        new InsertCustomerAndContactPointsTransitionally(datasourceBuilder,
                                                         TX_ISOLATION.TRANSACTION_READ_UNCOMMITTED)
            .apply(new Customer("Rafael",
                                new Email("imrafaelmerino@gmail.com"),
                                List.of(new Address("Elm's Street"),
                                        new Address("Square Center")
                                       )
                   )
                  )
            .join();

    Assertions.assertTrue(customerID > 0,
                          "customerId must be > 0");

    CustomerDatabaseOps customerDatabaseOps =
        CustomerDatabaseOps.of(datasourceBuilder);

    CustomerEntity customerEntity =
        customerDatabaseOps.findCustomerAndContactPoints.apply(customerID)
                                                        .join();

    System.out.println(customerEntity);


  }


}
