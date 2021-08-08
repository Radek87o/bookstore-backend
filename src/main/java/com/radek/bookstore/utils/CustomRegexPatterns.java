package com.radek.bookstore.utils;

public class CustomRegexPatterns {

    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()])[A-Za-z\\d@$!%*?&#^()]{8,}$";
    public static final String ZIP_CODE_REGEX = "^(\\d{2}-\\d{3})$";
    public static final String CREDIT_CARD_NUMBER_REGEX = "^[1-9][0-9]{15}$";
    public static final String SECURITY_CODE_REGEX = "^[1-9][0-9]{2}$";
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-z]{2,4}$";
}
