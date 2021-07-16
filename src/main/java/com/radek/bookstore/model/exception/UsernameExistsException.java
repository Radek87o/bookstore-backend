package com.radek.bookstore.model.exception;

public class UsernameExistsException extends Exception{

    public UsernameExistsException(String message) {
        super(message);
    }
}
