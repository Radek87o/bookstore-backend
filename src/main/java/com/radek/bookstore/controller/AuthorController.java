package com.radek.bookstore.controller;

import com.radek.bookstore.model.json.AuthorWrapper;
import com.radek.bookstore.service.AuthorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@CrossOrigin
@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private static final Logger log = LoggerFactory.getLogger(AuthorController.class);

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findAuthorById(@PathVariable String id,
                                            @RequestParam(name = "page", required = false) Integer page,
                                            @RequestParam(name = "size", required = false) Integer size) {
        if(Objects.isNull(page)) {
            page=0;
        }
        if(Objects.isNull(size)) {
            size=24;
        }
        if(!authorService.existByAuthorId(id)) {
            String message = String.format("Cannot find author with id: {}", id);
            return determineErrorStatus(message, HttpStatus.NOT_FOUND);
        }
        AuthorWrapper authorWrapper = authorService.findByAuthorId(id, page, size);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(authorWrapper, headers, HttpStatus.OK);
    }

    private ResponseEntity<String> determineErrorStatus(String message, HttpStatus httpStatus) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        log.info(message);
        return new ResponseEntity<>(message, headers, httpStatus);
    }
}
