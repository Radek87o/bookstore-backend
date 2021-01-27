package com.radek.bookstore.generators;

import com.radek.bookstore.model.Address;
import com.radek.bookstore.model.User;
import com.radek.bookstore.model.dto.AddressDto;
import com.radek.bookstore.model.dto.UserDto;

public class UserGenerator {

    private static final String NAME_REGEX_PATTERN = "[A-Z]{6,}";

    public static User generateUser(String userId) {
        UserDto userDto = UserDto.builder()
                .firstName(RegexWordGenerator.getRandomRegexWord(NAME_REGEX_PATTERN))
                .lastName(RegexWordGenerator.getRandomRegexWord(NAME_REGEX_PATTERN))
                .email(RegexWordGenerator.getRandomRegexWord(NAME_REGEX_PATTERN)+"@gmail.com")
                .address(generateBaseAddress())
                .build();
        User user = new User(userDto);
        user.setId(userId);
        return user;
    }

    private static Address generateBaseAddress() {
        AddressDto addressDto = AddressDto.builder()
                .city("Warszawa")
                .locationNumber("45a")
                .street("Puławska")
                .zipCode("02-282")
                .build();
        return new Address(addressDto);
    }
}
