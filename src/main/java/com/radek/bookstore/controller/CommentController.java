package com.radek.bookstore.controller;

import com.radek.bookstore.model.Comment;
import com.radek.bookstore.model.dto.CommentDto;
import com.radek.bookstore.model.json.CommentJson;
import com.radek.bookstore.service.BookService;
import com.radek.bookstore.service.CommentService;
import com.radek.bookstore.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Objects;

@CrossOrigin
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final static Logger log = LoggerFactory.getLogger(CommentController.class);

    private final CommentService commentService;
    private final BookService bookService;
    private final UserService userService;

    public CommentController(CommentService commentService, BookService bookService, UserService userService) {
        this.commentService = commentService;
        this.bookService = bookService;
        this.userService = userService;
    }

    @GetMapping(path = "/{bookId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getCommentsByBookId(@PathVariable("bookId") String bookId,
                                                              @RequestParam(required = false) Integer pageNumber,
                                                              @RequestParam(required = false) Integer pageSize) {
        if(!bookService.existsByBookId(bookId)){
            String message = String.format("Cannot find book with id: %s", bookId);
            log.info(message);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(message, responseHeaders, HttpStatus.BAD_REQUEST);
        }
        pageNumber = Objects.isNull(pageNumber) ? 0 : pageNumber;
        pageSize = Objects.isNull(pageSize) ? 10 : pageSize;
        Page<CommentJson> bookComments = commentService.getCommentsByBookId(bookId, pageNumber, pageSize);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(bookComments, responseHeaders, HttpStatus.OK);
    }

    @PostMapping(path = "/{bookId}/user/{userId}")
    public ResponseEntity<?> saveComment(@Valid @RequestBody CommentDto commentDto,
                                               @PathVariable("bookId") String bookId,
                                               @PathVariable("userId") String userId){
        if(!bookService.existsByBookId(bookId)) {
            return determineErrorResponseException(String.format("Book with id: %s cannot be found", bookId), HttpStatus.NOT_FOUND);
        }
        else if(!userService.existByUserId(userId)){
            return determineErrorResponseException(String.format("User with id: %s cannot be found", userId), HttpStatus.NOT_FOUND);
        }
        Collection<Comment> comments = commentService.saveComment(commentDto, bookId, userId);
        return new ResponseEntity<>(comments, HttpStatus.CREATED);
    }

    private ResponseEntity<String> determineErrorResponseException(String message, HttpStatus httpStatus) {
        log.info(message);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(message, headers, httpStatus);
    }
}
