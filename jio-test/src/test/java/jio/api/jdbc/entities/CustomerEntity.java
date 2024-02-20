package jio.api.jdbc.entities;

import java.util.List;

public record CustomerEntity(String name,
                             EmailEntity email,
                             List<AddressEntity> addresses,
                             Long id) {

}