package com.radek.bookstore.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

public class ResponseHelper {

    public static ResponseEntity<?> createOkResponse(Object body) {
        if(Objects.isNull(body)) {
            throw new IllegalArgumentException("Response body cannot be null");
        }
        return createResponse(body, HttpStatus.OK);
    }

    public static ResponseEntity<?> createCreatedResponse(Object body) {
        if(Objects.isNull(body)) {
            throw new IllegalArgumentException("Response body cannot be null");
        }
        return createResponse(body, HttpStatus.CREATED);
    }

    public static ResponseEntity<?> createNotFoundResponse(String message) {
        if(Objects.isNull(message)) {
            throw new IllegalArgumentException("Error message cannot be null");
        }
        return createResponse(message, HttpStatus.NOT_FOUND);
    }

    public static ResponseEntity<?> createBadRequestResponse(String message) {
        if(Objects.isNull(message)) {
            throw new IllegalArgumentException("Error message cannot be null");
        }
        return createResponse(message, HttpStatus.BAD_REQUEST);
    }

    private static ResponseEntity<?> createResponse(Object body, HttpStatus httpStatus) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(body, responseHeaders, httpStatus);
    }
}
