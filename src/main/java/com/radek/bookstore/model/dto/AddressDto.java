package com.radek.bookstore.model.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AddressDto {
    private final String street;
    private final String locationNumber;
    private final String city;
    private final String zipCode;
}
