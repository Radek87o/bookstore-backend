package com.radek.bookstore.controller;

import com.radek.bookstore.generators.BookGenerator;
import com.radek.bookstore.generators.CommentGenerator;
import com.radek.bookstore.generators.RatingGenerator;
import com.radek.bookstore.generators.UserGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ResponseHelperTest {

    @ParameterizedTest
    @MethodSource("setOfResponseBodies")
    void shouldCreateOkResponseMethodReturnResponseWithCorrectResponseParams(Object body) {
        ResponseEntity<?> expected = createExpectedResponse(body, MediaType.APPLICATION_JSON, HttpStatus.OK);
        ResponseEntity<?> result = ResponseHelper.createOkResponse(body);
        assertEquals(expected.getStatusCode(), result.getStatusCode());
        assertEquals(expected.getBody(), result.getBody());
        assertEquals(expected.getHeaders(), result.getHeaders());
    }

    private static Stream<Arguments> setOfResponseBodies() {
        return Stream.of(
            Arguments.of(BookGenerator.generateBook(LocalDateTime.now())),
            Arguments.of(CommentGenerator.generateCommentWithCommentId("someCommentId")),
            Arguments.of(RatingGenerator.generateRating("someRatingId")),
            Arguments.of(UserGenerator.generateUser("someUserId")),
            Arguments.of("Any example of string")
        );
    }

    @Test
    void shouldCreateOkResponseMethodThrowIllegalArgumentExceptionWhenNullPassedAsBody() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                          () -> ResponseHelper.createOkResponse(null));
        assertEquals("Response body cannot be null", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("setOfResponseBodies")
    void shouldCreateCreatedResponseMethodReturnResponseWithCorrectResponseParams(Object body) {
        ResponseEntity<?> expected = createExpectedResponse(body, MediaType.APPLICATION_JSON, HttpStatus.CREATED);
        ResponseEntity<?> result = ResponseHelper.createCreatedResponse(body);
        assertEquals(expected.getStatusCode(), result.getStatusCode());
        assertEquals(expected.getBody(), result.getBody());
        assertEquals(expected.getHeaders(), result.getHeaders());
    }

    @Test
    void shouldCreateCreatedResponseMethodThrowIllegalArgumentExceptionWhenNullPassedAsBody() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ResponseHelper.createCreatedResponse(null));
        assertEquals("Response body cannot be null", exception.getMessage());
    }

    @Test
    void shouldCreateNotFoundResponseMethodReturnResponseWithCorrectResponseParams() {
        String errorMessage = "Object not found";
        ResponseEntity<?> expected = createExpectedResponse(errorMessage, MediaType.APPLICATION_JSON, HttpStatus.NOT_FOUND);
        ResponseEntity<?> result = ResponseHelper.createNotFoundResponse(errorMessage);
        assertEquals(expected.getStatusCode(), result.getStatusCode());
        assertEquals(expected.getBody(), result.getBody());
        assertEquals(expected.getHeaders(), result.getHeaders());
    }

    @Test
    void shouldCreateNotFoundResponseMethodThrowIllegalArgumentExceptionWhenNullPassedAsMessage() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ResponseHelper.createNotFoundResponse(null));
        assertEquals("Error message cannot be null", exception.getMessage());
    }

    @Test
    void shouldCreateBadRequestResponseMethodReturnResponseWithCorrectResponseParams() {
        String errorMessage = "Bad request";
        ResponseEntity<?> expected = createExpectedResponse(errorMessage, MediaType.APPLICATION_JSON, HttpStatus.BAD_REQUEST);
        ResponseEntity<?> result = ResponseHelper.createBadRequestResponse(errorMessage);
        assertEquals(expected.getStatusCode(), result.getStatusCode());
        assertEquals(expected.getBody(), result.getBody());
        assertEquals(expected.getHeaders(), result.getHeaders());
    }

    @Test
    void shouldCreateBadRequestResponseMethodThrowIllegalArgumentExceptionWhenNullPassedAsMessage() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ResponseHelper.createBadRequestResponse(null));
        assertEquals("Error message cannot be null", exception.getMessage());
    }

    private ResponseEntity<?> createExpectedResponse(Object body, MediaType mediaType, HttpStatus status) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(mediaType);
        return new ResponseEntity<>(body, responseHeaders, status);
    }
}
