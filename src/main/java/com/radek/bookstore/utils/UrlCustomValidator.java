package com.radek.bookstore.utils;

import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.stereotype.Component;

@Component
public class UrlCustomValidator {

    public static boolean urlValidator(String url) {
        String[] customSchemes = { "http", "https" };
        UrlValidator defaultValidator = new UrlValidator(customSchemes);
        return defaultValidator.isValid(url);
    }
}
