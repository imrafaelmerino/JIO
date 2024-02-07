package jio.api.jdbc;

import jio.IO;
import jio.Lambda;
import jio.PairExp;
import jio.api.jdbc.dao.AddressesDatabaseOps;
import jio.api.jdbc.dao.CustomerDatabaseOps;
import jio.api.jdbc.dao.EmailDatabaseOps;
import jio.api.jdbc.domain.Customer;
import jio.jdbc.ClosableStatement;
import jio.jdbc.DatasourceBuilder;
import jio.jdbc.TxBuilder;

public class InsertCustomerAndContactPointsTransitionally implements Lambda<Customer, Long> {


  final ClosableStatement<Customer, Long> insertContactPoints = (customer, connection) ->
      CustomerDatabaseOps.insertOne
          .then(customerID ->
                    PairExp.par(
                               EmailDatabaseOps.insertOne.apply(customerID)
                                                         .apply(customer.email(),
                                                                connection),
                               AddressesDatabaseOps.insertMany.apply(customerID)
                                                              .apply(customer.addresses(),
                                                                     connection)
                               )
                           .debugEach("customerID:%s".formatted(customerID))
                           .map(it -> customerID)
               )
          .apply(customer,
                 connection);

  private final Lambda<Customer, Long> tx;

  public InsertCustomerAndContactPointsTransitionally(DatasourceBuilder datasourceBuilder,
                                                      TxBuilder.TX_ISOLATION isolation) {
    this.tx = TxBuilder.of(datasourceBuilder,
                           isolation)
                       .build(insertContactPoints);


  }


  @Override
  public IO<Long> apply(final Customer customer) {
    return tx.apply(customer);
  }


}
