package com.radek.bookstore.model.exception;

public class BookStoreServiceException extends RuntimeException {

    public BookStoreServiceException(String message) {
        super(message);
    }

    public BookStoreServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
