package com.radek.bookstore.model.dto;

import com.radek.bookstore.utils.CustomRegexPatterns;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@RequiredArgsConstructor
public class ResetPasswordDto {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = CustomRegexPatterns.PASSWORD_REGEX)
    private String password;
}
