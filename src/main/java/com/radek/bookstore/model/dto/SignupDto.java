package com.radek.bookstore.model.dto;

import com.radek.bookstore.utils.CustomRegexPatterns;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;


@Data
public class SignupDto {

    @Size(min = 3, max = 30)
    private String username;

    @NotEmpty
    @Email
    @Size(max = 50)
    private String email;

    @NotEmpty
    @Pattern(regexp = CustomRegexPatterns.PASSWORD_REGEX)
    private String password;

    @NotEmpty
    @Size(min = 3, max = 30)
    private String firstName;

    @NotEmpty
    @Size(min = 3, max = 30)
    private String lastName;

    private AddressDto address;
}
