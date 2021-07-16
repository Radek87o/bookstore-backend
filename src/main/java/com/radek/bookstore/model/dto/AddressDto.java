package com.radek.bookstore.model.dto;

import com.radek.bookstore.utils.CustomRegexPatterns;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Getter
@Builder
public class AddressDto {

    @NotEmpty
    private final String street;

    @NotEmpty
    private final String locationNumber;

    @NotEmpty
    private final String city;

    @NotEmpty
    @Pattern(regexp = CustomRegexPatterns.ZIP_CODE_REGEX)
    private final String zipCode;
}
