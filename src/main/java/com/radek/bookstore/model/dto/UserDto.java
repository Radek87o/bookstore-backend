package com.radek.bookstore.model.dto;

import com.radek.bookstore.utils.CustomRegexPatterns;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Builder
public class UserDto {

    private final String username;

    @NotEmpty
    @Email
    @Size(max = 50)
    private final String email;

    @Pattern(regexp = CustomRegexPatterns.PASSWORD_REGEX)
    private final String password;

    @NotEmpty
    @Size(min = 3, max = 30)
    private final String firstName;

    @NotEmpty
    @Size(min = 3, max = 30)
    private final String lastName;

    private final AddressDto address;
}
