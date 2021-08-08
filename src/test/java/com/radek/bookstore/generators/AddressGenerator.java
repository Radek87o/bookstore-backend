package com.radek.bookstore.generators;

import com.radek.bookstore.model.Address;
import com.radek.bookstore.model.dto.AddressDto;

import static com.radek.bookstore.generators.OrderGenerator.generateOrder;

public class AddressGenerator {

    public static Address generateBaseAddress() {
        return new Address(generateBaseAddressDto());
    }

    public static AddressDto generateAddressDto() {
        return generateBaseAddressDto();
    }

    public static Address generateAddressWithCity(String city) {
        Address address = new Address(generateBaseAddressDto());
        address.setCity(city);
        return address;
    }

    public static Address generateAddressWithStreet(String street) {
        Address address = new Address(generateBaseAddressDto());
        address.setStreet(street);
        return address;
    }

    public static Address generateAddressWithLocationNumber(String locationNumber) {
        Address address = new Address(generateBaseAddressDto());
        address.setLocationNumber(locationNumber);
        return address;
    }

    public static Address generateAddressWithZipCode(String zipCode) {
        Address address = new Address(generateBaseAddressDto());
        address.setZipCode(zipCode);
        return address;
    }

    private static AddressDto generateBaseAddressDto() {
        return AddressDto.builder()
                .city("Warszawa")
                .locationNumber("45a")
                .street("Puławska")
                .zipCode("02-282")
                .build();
    }
}
