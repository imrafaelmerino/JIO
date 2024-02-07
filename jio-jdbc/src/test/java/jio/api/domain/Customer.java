package jio.api.domain;

import java.util.List;

public record Customer(String name,
                       Email email,
                       List<Address> addresses) {

}