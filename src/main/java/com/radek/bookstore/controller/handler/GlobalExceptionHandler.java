package com.radek.bookstore.controller.handler;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.radek.bookstore.model.exception.*;
import com.radek.bookstore.model.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.NoResultException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public static final String ACCOUNT_LOCKED = "Your account has been locked. Please contact administration";
    public static final String METHOD_IS_NOT_ALLOWED = "This request method is not allowed on this endpoint. Please send a %s request";
    public static final String INCORRECT_CREDENTIALS = "Username or password incorrect. Please try again.";
    public static final String ACCOUNT_DISABLED = "Your account has been disabled. If this is an error, please contact administration";
    public static final String NOT_ENOUGH_PERMISSION = "You do not have enough permission to access this resource";

    @ExceptionHandler(value = DisabledException.class)
    public ResponseEntity<HttpResponse> accountDisabledException(){
        return createHttpResponse(BAD_REQUEST, ACCOUNT_DISABLED);
    }

    @ExceptionHandler(value = BadCredentialsException.class)
    public ResponseEntity<HttpResponse> badCredentialsException(){
        return createHttpResponse(BAD_REQUEST, INCORRECT_CREDENTIALS);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<HttpResponse> accessDeniedException(){
        return createHttpResponse(FORBIDDEN, NOT_ENOUGH_PERMISSION);
    }

    @ExceptionHandler(value = LockedException.class)
    public ResponseEntity<HttpResponse> lockedException(){
        return createHttpResponse(UNAUTHORIZED, ACCOUNT_LOCKED);
    }

    @ExceptionHandler(value = TokenExpiredException.class)
    public ResponseEntity<HttpResponse> tokenExpiredException(TokenExpiredException exc){
        return createHttpResponse(UNAUTHORIZED, exc.getMessage());
    }

    @ExceptionHandler(value = EmailExistsException.class)
    public ResponseEntity<HttpResponse> emailExists(EmailExistsException exc){
        return createHttpResponse(BAD_REQUEST, exc.getMessage());
    }

    @ExceptionHandler(value = EmailNotFoundException.class)
    public ResponseEntity<HttpResponse> emailExists(EmailNotFoundException exc){
        return createHttpResponse(BAD_REQUEST, exc.getMessage());
    }

    @ExceptionHandler(value = UsernameExistsException.class)
    public ResponseEntity<HttpResponse> usernameExists(UsernameExistsException exc){
        return createHttpResponse(BAD_REQUEST, exc.getMessage());
    }

    @ExceptionHandler(value = UserNotFoundException.class)
    public ResponseEntity<HttpResponse> userNotFound(UserNotFoundException exc){
        return createHttpResponse(BAD_REQUEST, exc.getMessage());
    }

    @ExceptionHandler(value = PasswordNotExistsException.class)
    public ResponseEntity<HttpResponse> emptyPassword(PasswordNotExistsException exc){
        return createHttpResponse(BAD_REQUEST, exc.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException exc, HttpHeaders headers, HttpStatus status, WebRequest request) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpMethod httpSupportedMethod = Objects.requireNonNull(exc.getSupportedHttpMethods()).iterator().next();
        ResponseEntity<HttpResponse> httpResponse = createHttpResponse(METHOD_NOT_ALLOWED, String.format(METHOD_IS_NOT_ALLOWED, httpSupportedMethod.name()));
        return new ResponseEntity<Object>(httpResponse, headers, METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(value = NoResultException.class)
    public ResponseEntity<HttpResponse> noResultException(NoResultException exc){
        log.error(exc.getMessage());
        return createHttpResponse(NOT_FOUND, exc.getMessage());
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> handleUnexpectedException(Exception e, WebRequest request) {
        log.error("Handling {} due to {}", e.getClass().getSimpleName(), e.getMessage());
        e.printStackTrace();
        return createJsonResponse(e, request);
    }

    private ResponseEntity<HttpResponse> createHttpResponse(HttpStatus httpStatus, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpResponse httpResponse = new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase(), message);
        return new ResponseEntity<>(httpResponse, headers, httpStatus);
    }

    private ResponseEntity<Object> createJsonResponse(Exception e, WebRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (e instanceof ResponseStatusException) {
            return new ResponseEntity<>(createExceptionBody(((ResponseStatusException) e).getStatus(), ((ResponseStatusException) e).getReason(), request.getDescription(false)), headers, ((ResponseStatusException) e).getStatus());
        }
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(createExceptionBody(status, "An unexpected error occurred", request.getDescription(false)), headers, status);
    }

    private Map<String, Object> createExceptionBody(HttpStatus status, String message, String path) {
        Map<String, Object> exceptionBody = new LinkedHashMap<>();
        exceptionBody.put("timestamp", LocalDateTime.now());
        exceptionBody.put("status", status.value());
        exceptionBody.put("error", status.getReasonPhrase());
        exceptionBody.put("message", message);
        exceptionBody.put("path", path);
        return exceptionBody;
    }
}
