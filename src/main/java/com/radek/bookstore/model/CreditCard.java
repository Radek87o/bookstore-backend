package com.radek.bookstore.model;

import com.radek.bookstore.utils.CustomRegexPatterns;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Embeddable;
import javax.validation.constraints.*;

@Getter
@Setter
@Embeddable
public class CreditCard {

    @NotNull
    @Pattern(regexp = CustomRegexPatterns.CREDIT_CARD_NUMBER_REGEX)
    private String cardNumber;

    @NotNull
    @Pattern(regexp = CustomRegexPatterns.SECURITY_CODE_REGEX)
    private String securityCode;

    @NotNull
    @Min(value = 2021)
    private Integer expirationYear;

    @NotNull
    @Min(value = 1)
    @Max(value = 12)
    private Integer expirationMonth;
}
