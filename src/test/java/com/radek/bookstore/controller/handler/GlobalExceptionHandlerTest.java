package com.radek.bookstore.controller.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    GlobalExceptionHandler handler;
    WebRequest request;

    @BeforeEach
    void setup() {
        handler = new GlobalExceptionHandler();
        request = Mockito.mock(WebRequest.class);
    }

    @ParameterizedTest
    @MethodSource("statusExceptionsArguments")
    void shouldReturnJsonResponseWithCorrectStatusAndBodyWhenResponseStatusExceptionIsThrown(ResponseStatusException exception) {
        ResponseEntity<Object> response = handler.handleUnexpectedException(exception, request);
        String stringBody = response.getBody().toString();
        assertEquals(exception.getReason(), extractMessageFromResponseBody(stringBody));
        assertEquals(exception.getStatus(), response.getStatusCode());
    }

    private static Stream<Arguments> statusExceptionsArguments() {
        return Stream.of(
                Arguments.of(new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Book with id: %s cannot be found", "bookId"))),
                Arguments.of(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book cannot be null"),
                Arguments.of(new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("User with id: %s cannot be found", "userId"))),
                Arguments.of(new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Cannot retrieve rating due to given book with id: %s does not exist", "bookId"))))
        );
    }

    @Test
    void shouldReturnJsonResponseWithCorrectStatusAndBodyWhenUnexpectedErrorOccur() {
        ResponseEntity<Object> response = handler.handleUnexpectedException(new NullPointerException(), request);
        String stringBody = response.getBody().toString();
        assertEquals("An unexpected error occurred", extractMessageFromResponseBody(stringBody));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    private String extractMessageFromResponseBody(String body) {
        int startIndex = body.indexOf("message=");
        int endIndex = body.indexOf(", path=");
        return body.substring(startIndex, endIndex).replaceFirst("message=", "");
    }
}
