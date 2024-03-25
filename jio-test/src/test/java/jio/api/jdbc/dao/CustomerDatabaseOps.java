package jio.api.jdbc.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jio.IO;
import jio.Lambda;
import jio.api.jdbc.domain.Customer;
import jio.api.jdbc.entities.AddressEntity;
import jio.api.jdbc.entities.CustomerEntity;
import jio.api.jdbc.entities.EmailEntity;
import jio.jdbc.ClosableStatement;
import jio.jdbc.DatasourceBuilder;
import jio.jdbc.FindEntitiesBuilder;
import jio.jdbc.FindOneEntityBuilder;
import jio.jdbc.InsertOneEntityBuilder;
import jio.jdbc.ResultSetMapper;

public final class CustomerDatabaseOps {

  final DatasourceBuilder datasourceBuilder;

  public static final ClosableStatement<Customer, Long> insertOne = InsertOneEntityBuilder.<Customer, Long>of("INSERT INTO customer (name) VALUES (?) RETURNING id;",
                                                                                                              customer -> (paramPosition,
                                                                                                                           preparedStatement) -> {
                                                                                                                preparedStatement.setString(paramPosition++,
                                                                                                                                            customer.name());
                                                                                                                return paramPosition;
                                                                                                              },
                                                                                                              customer -> resultSet -> resultSet.getLong("id"),
                                                                                                              Duration.ofSeconds(1000)
  )
                                                                                          .withEventLabel("insert one customer")

                                                                                          .buildClosable();
  public final Lambda<Long, CustomerEntity> findCustomerAndContactPoints;
  public final IO<Integer> countCustomer;

  private CustomerDatabaseOps(DatasourceBuilder datasourceBuilder) {
    this.datasourceBuilder = datasourceBuilder;

    ResultSetMapper<List<Long>> mapper = ResultSetMapper.ONE_ROW_PER_ENTITY(r -> r.getLong("id"));
    countCustomer = FindEntitiesBuilder.<Void, Long>of("SELECT * FROM customer",
                                                       id -> (paramPosition,
                                                              preparedStatement) -> {
                                                         return paramPosition;
                                                       },
                                                       mapper,
                                                       Duration.ofSeconds(1)
    )
                                       .withEventLabel("find all customers")
                                       .buildAutoClosable(datasourceBuilder)
                                       .apply(null)
                                       .map(List::size);

    findCustomerAndContactPoints = FindOneEntityBuilder.<Long, CustomerEntity>of("""
        SELECT c.id AS customer_id, c.name AS customer_name,
        a.id AS address_id, a.street, e.id AS email_id,
        e.email_address AS email_address  FROM customer c
        LEFT JOIN address a ON c.id = a.customer_id
        LEFT JOIN email e ON c.id = e.customer_id
        WHERE c.id = ?""".replace("\n",
                                  " "),
                                                                                 id -> (paramPosition,
                                                                                        preparedStatement) -> {
                                                                                   preparedStatement.setLong(paramPosition++,
                                                                                                             id);

                                                                                   return paramPosition;
                                                                                 },
                                                                                 this::mapResultSetToCustomerEntity,
                                                                                 Duration.ofSeconds(1)

    )
                                                       .withEventLabel("find one customer and contact points")
                                                       .buildAutoClosable(datasourceBuilder);

  }

  public static CustomerDatabaseOps of(DatasourceBuilder datasourceBuilder) {
    return new CustomerDatabaseOps(datasourceBuilder);
  }

  public CustomerEntity mapResultSetToCustomerEntity(ResultSet rs) throws SQLException {
    Map<Long, AddressEntity> addressesMap = new HashMap<>();

    Long customerId = null;
    String customerName = null;
    Long emailId = null;
    String emailAddress = null;

    while (rs.next()) {
      if (customerId == null) {
        customerId = rs.getLong("customer_id");
        customerName = rs.getString("customer_name");
        emailId = rs.getLong("email_id");
        emailAddress = rs.getString("email_address");
      }

      long addressId = rs.getLong("address_id");
      String addressName = rs.getString("street");

      // Create instances of AddressEntity
      AddressEntity address = new AddressEntity(addressName,
                                                customerId,
                                                addressId);
      addressesMap.put(addressId,
                       address);
    }

    if (customerId == null) {
      return null;
    }

    // Create a customer instance with associated addresses and email
    EmailEntity email = new EmailEntity(emailAddress,
                                        customerId,
                                        emailId);
    List<AddressEntity> addresses = new ArrayList<>(addressesMap.values());
    return new CustomerEntity(customerName,
                              email,
                              addresses,
                              customerId);
  }
}
