package com.radek.bookstore.generators;

import com.radek.bookstore.model.CreditCard;

import static com.radek.bookstore.generators.RegexWordGenerator.getRandomRegexWord;

public class CreditCardGenerator {

    private static final String CREDIT_CARD_NUMBER_REGEX = "[1-9][0-9]{15}";
    private static final String SECURITY_CODE_REGEX = "[1-9][0-9]{2}";

    public static CreditCard generateCreditCard() {
        return generateBaseCreditCard();
    }

    public static CreditCard generateCreditCardWithCardNumber(String cardNumber) {
        CreditCard creditCard = generateBaseCreditCard();
        creditCard.setCardNumber(cardNumber);
        return creditCard;
    }

    public static CreditCard generateCreditCardWithSecurityCode(String securityCode) {
        CreditCard creditCard = generateBaseCreditCard();
        creditCard.setSecurityCode(securityCode);
        return creditCard;
    }

    public static CreditCard generateCreditCardWithExpirationMonth(Integer expMonth) {
        CreditCard creditCard = generateBaseCreditCard();
        creditCard.setExpirationMonth(expMonth);
        return creditCard;
    }

    public static CreditCard generateCreditCardWithExpirationYear(Integer expYear) {
        CreditCard creditCard = generateBaseCreditCard();
        creditCard.setExpirationMonth(expYear);
        return creditCard;
    }

    private static CreditCard generateBaseCreditCard() {
        CreditCard creditCard = new CreditCard();
        creditCard.setCardNumber(getRandomRegexWord(CREDIT_CARD_NUMBER_REGEX));
        creditCard.setSecurityCode(getRandomRegexWord(SECURITY_CODE_REGEX));
        creditCard.setExpirationMonth(10);
        creditCard.setExpirationYear(2099);
        return creditCard;
    }
}
