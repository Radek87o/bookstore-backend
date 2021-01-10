package com.radek.bookstore.model.dto;

import com.radek.bookstore.model.Address;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDto {

    private final String firstName;
    private final String lastName;
    private final String email;
    private final Address address;
}
