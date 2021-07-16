package com.radek.bookstore.controller;

import com.radek.bookstore.model.Comment;
import com.radek.bookstore.model.dto.CommentDto;
import com.radek.bookstore.model.response.CommentJson;
import com.radek.bookstore.service.BookService;
import com.radek.bookstore.service.CommentService;
import com.radek.bookstore.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
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
                                                              @RequestParam(required = false) Integer pageNumber) {
        if(!bookService.existsByBookId(bookId)){
            String message = String.format("Cannot find book with id: %s", bookId);
            log.info(message);
            return ResponseHelper.createBadRequestResponse(message);
        }
        pageNumber = Objects.isNull(pageNumber) ? 0 : pageNumber;
        Page<CommentJson> bookComments = commentService.getCommentsByBookId(bookId, pageNumber);
        return ResponseHelper.createOkResponse(bookComments);
    }

    @PostMapping(path = "/{bookId}/user/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveComment(@Valid @RequestBody CommentDto commentDto,
                                               @PathVariable("bookId") String bookId,
                                               @PathVariable("userId") String userId){
        if(!bookService.existsByBookId(bookId)) {
            String message = String.format("Book with id: %s cannot be found", bookId);
            log.info(message);
            return ResponseHelper.createNotFoundResponse(message);
        }
        else if(!userService.existByUserId(userId)){
            String message = String.format("User with id: %s cannot be found", userId);
            log.info(message);
            return ResponseHelper.createNotFoundResponse(message);
        }
        Collection<Comment> comments = commentService.saveComment(commentDto, bookId, userId);
        return ResponseHelper.createCreatedResponse(comments);
    }
}
