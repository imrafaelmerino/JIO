package jio.api.jdbc.entities;

public record EmailEntity(String email,
                          Long customerId,
                          Long id) {

}
