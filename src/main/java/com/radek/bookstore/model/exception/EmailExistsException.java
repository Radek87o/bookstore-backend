package com.radek.bookstore.model.exception;

public class EmailExistsException extends Exception {

    public EmailExistsException(String message) {
        super(message);
    }
}
