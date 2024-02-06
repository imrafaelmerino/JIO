package jio.api;


import java.util.List;
import jio.IO;
import jio.PairExp;
import jio.jdbc.ClosableStatement;
import jio.jdbc.RollBackToSavePoint;

public class TestTxs {


  public void testInsertAll() {

    record Email(String email,
                 Long customerId,
                 Long id) {

    }
    record Address(String street,
                   Long customerId,
                   Long id) {

    }

    record Customer(String name,
                    Email email,
                    List<Address> addresses,
                    Long id) {

    }

    final ClosableStatement<Customer, Long> insertCustomer = null;
    final ClosableStatement<Email, Long> insertEmail = null;
    final ClosableStatement<Address, Long> insertAddress = null;
    final ClosableStatement<List<Address>, List<Long>> insertAddresses = null;

   /* ClosableStatement<Customer, Customer> insert =
        (customer, connection) ->
            insertCustomer.apply(customer,
                                 connection)
                          .then(customerId -> {
                                  IO.task(() -> connection.setSavepoint("customer"))
                                    .then(savepoint ->
                                              PairExp.par(insertEmail.apply(customer.email,
                                                                            connection),
                                                          insertAddresses.apply(customer.addresses,
                                                                                connection)
                                                         )
                                                     .mapFailure(it -> RollBackToSavePoint.of(savepoint,
                                                                                              customer)
                                                                );
                                         )
                                }
                               )
                          .map(ids -> customer);*/

  }


}
