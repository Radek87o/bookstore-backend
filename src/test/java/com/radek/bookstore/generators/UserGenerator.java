package com.radek.bookstore.generators;

import com.radek.bookstore.model.Role;
import com.radek.bookstore.model.User;
import com.radek.bookstore.model.dto.UserDto;

import static com.radek.bookstore.generators.AddressGenerator.generateAddressDto;

public class UserGenerator {

    private static final String NAME_REGEX_PATTERN = "[A-Z]{6,}";

    public static User generateUser(String userId) {
        UserDto userDto = UserDto.builder()
                .firstName(RegexWordGenerator.getRandomRegexWord(NAME_REGEX_PATTERN))
                .lastName(RegexWordGenerator.getRandomRegexWord(NAME_REGEX_PATTERN))
                .email(RegexWordGenerator.getRandomRegexWord(NAME_REGEX_PATTERN)+"@gmail.com")
                .address(generateAddressDto())
                .build();
        User user = new User(userDto);
        user.setId(userId);
        user.setRole("admin");
        user.setPassword("SomePassword88$$");
        user.setAuthorities(Role.ROLE_ADMIN.getAuthorities());
        return user;
    }

    public static User generateUser(String userId, String email) {
        UserDto userDto = UserDto.builder()
                .firstName(RegexWordGenerator.getRandomRegexWord(NAME_REGEX_PATTERN))
                .lastName(RegexWordGenerator.getRandomRegexWord(NAME_REGEX_PATTERN))
                .email(email)
                .address(generateAddressDto())
                .build();
        User user = new User(userDto);
        user.setId(userId);
        user.setRole("admin");
        user.setPassword("SomePassword88$$");
        user.setAuthorities(Role.ROLE_ADMIN.getAuthorities());
        return user;
    }
}
