package com.radek.bookstore.controller;

import com.radek.bookstore.model.Rating;
import com.radek.bookstore.model.dto.RatingDto;
import com.radek.bookstore.service.BookService;
import com.radek.bookstore.service.RatingService;
import com.radek.bookstore.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final static Logger log = LoggerFactory.getLogger(RatingController.class);

    private final RatingService ratingService;
    private final BookService bookService;
    private final UserService userService;

    public RatingController(RatingService ratingService, BookService bookService, UserService userService) {
        this.ratingService = ratingService;
        this.bookService = bookService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<Rating>> getBooksRating(@RequestParam String bookId) {
        if(Objects.isNull(bookId)) {
            log.info("Cannot retrieve books rating due to given book id is null");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Given book id cannot be null");
        }
        List<Rating> bookRatings = ratingService.getBookRatings(bookId);
        return new ResponseEntity<>(bookRatings, HttpStatus.OK);
    }

    @GetMapping(path = "/{bookId}/user/{userId}")
    public ResponseEntity<Rating> getSingleRating(@PathVariable("bookId") String bookId, @PathVariable("userId") String userId) {
        if(Objects.isNull(bookId)) {
            log.info("Cannot retrieve rating due to given book id is null");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Given book id cannot be null");
        }
        if(Objects.isNull(bookId)) {
            log.info("Cannot retrieve rating due to given user id is null");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Given user id cannot be null");
        }
        Optional<Rating> bookRatingOptional = ratingService.getBookRating(bookId, userId);
        Rating bookRating = bookRatingOptional.isEmpty()
                ? new Rating(new RatingDto(0))
                : bookRatingOptional.get();
        return new ResponseEntity<>(bookRating, HttpStatus.OK);
    }

    @PostMapping(path = "/{bookId}/user/{userId}")
    public ResponseEntity<Void> saveRating(@RequestBody RatingDto ratingDto, @PathVariable("bookId") String bookId, @PathVariable("userId") String userId){
        if(Objects.isNull(ratingDto)) {
            determineResponseException("Rating cannot be null", HttpStatus.BAD_REQUEST);
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
        ratingService.saveRating(ratingDto, bookId, userId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    //TODO - przenieś do osobnej klasy do obsługi wyjątków
    private void determineResponseException(String message, HttpStatus httpStatus) {
        log.info(message);
        throw new ResponseStatusException(httpStatus, message);
    }
}
