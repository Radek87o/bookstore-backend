package com.radek.bookstore.utils;

public class CustomRegexPatterns {

    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()])[A-Za-z\\d@$!%*?&#^()]{8,}$";
    public static final String ZIP_CODE_REGEX = "^(\\d{2}-\\d{3})$";
}
