package com.radek.bookstore.controller;

import com.radek.bookstore.model.Book;
import com.radek.bookstore.model.Comment;
import com.radek.bookstore.model.User;
import com.radek.bookstore.model.dto.CommentDto;
import com.radek.bookstore.model.json.CommentJson;
import com.radek.bookstore.service.BookService;
import com.radek.bookstore.service.CommentService;
import com.radek.bookstore.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    @GetMapping(path = "/{bookId}")
    public ResponseEntity<Page<CommentJson>> getCommentsByBookId(@PathVariable("bookId") String bookId,
                                                              @RequestParam(required = false) Integer pageNumber,
                                                              @RequestParam(required = false) Integer pageSize) {
        if(Objects.isNull(bookId)) {
            log.info("Book id cannot be null");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book id cannot be null");
        }
        if(!bookService.existsByBookId(bookId)){
            String message = "Cannot find book with id: ";
            log.info(message+"{}", bookId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(message+"%", bookId));
        }
        pageNumber = Objects.isNull(pageNumber) ? 0 : pageNumber;
        pageSize = Objects.isNull(pageSize) ? 10 : pageSize;
        Page<CommentJson> bookComments = commentService.getCommentsByBookId(bookId, pageNumber, pageSize);
        return new ResponseEntity<>(bookComments, HttpStatus.OK);
    }

    @PostMapping(path = "/{bookId}/user/{userId}")
    public ResponseEntity<Void> saveComment(@RequestBody CommentDto commentDto,
                                               @PathVariable("bookId") String bookId,
                                               @PathVariable("userId") String userId){
        if(Objects.isNull(commentDto)) {
            determineResponseException("Comment cannot be null", HttpStatus.BAD_REQUEST);
        }
        else if(Objects.isNull(bookId)) {
            determineResponseException("Book id cannot be null", HttpStatus.BAD_REQUEST);
        }
        else if(Objects.isNull(userId)) {
            determineResponseException("User id cannot be null", HttpStatus.BAD_REQUEST);
        }
        else if(!bookService.existsByBookId(bookId)) {
            determineResponseException(String.format("Book with id: %s cannot be found", bookId), HttpStatus.NOT_FOUND);
        }
        else if(!userService.existByUserId(userId)){
            determineResponseException(String.format("User with id: %s cannot be found", userId), HttpStatus.NOT_FOUND);
        }
        commentService.saveComment(commentDto, bookId, userId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    private void determineResponseException(String message, HttpStatus httpStatus) {
        log.info(message);
        throw new ResponseStatusException(httpStatus, message);
    }
}
