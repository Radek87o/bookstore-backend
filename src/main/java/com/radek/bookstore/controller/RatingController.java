package com.radek.bookstore.controller;

import com.radek.bookstore.model.Rating;
import com.radek.bookstore.model.dto.RatingDto;
import com.radek.bookstore.service.BookService;
import com.radek.bookstore.service.RatingService;
import com.radek.bookstore.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
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

    @GetMapping(path = "/{bookId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getBooksRating(@PathVariable("bookId") String bookId) {
        if(!bookService.existsByBookId(bookId)) {
            String message = String.format("Cannot retrieve rating due to given book with id: %s does not exist", bookId);
            return determineErrorResponseException(message, HttpStatus.NOT_FOUND);
        }
        List<Rating> bookRatings = ratingService.getBookRatings(bookId);
        return new ResponseEntity<>(bookRatings, HttpStatus.OK);
    }

    @GetMapping(path = "/{bookId}/user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getSingleRating(@PathVariable("bookId") String bookId, @PathVariable("userId") String userId) {
        if(!bookService.existsByBookId(bookId)) {
            String message = String.format("Cannot retrieve rating due to given book with id: %s does not exist", bookId);
            return determineErrorResponseException(message, HttpStatus.NOT_FOUND);
        }
        if(!userService.existByUserId(userId)) {
            String message = String.format("Cannot retrieve rating due to given user with id: %s does not exist", userId);
            return determineErrorResponseException(message, HttpStatus.NOT_FOUND);
        }
        Optional<Rating> bookRatingOptional = ratingService.getBookRating(bookId, userId);
        Rating bookRating = bookRatingOptional.isEmpty()
                ? new Rating(new RatingDto(0))
                : bookRatingOptional.get();
        return new ResponseEntity<>(bookRating, HttpStatus.OK);
    }

    @PostMapping(path = "/{bookId}/user/{userId}")
    public ResponseEntity<?> saveRating(@Valid @RequestBody RatingDto ratingDto, @PathVariable("bookId") String bookId, @PathVariable("userId") String userId){
        if(!bookService.existsByBookId(bookId)) {
            String message = String.format("Book with id: %s cannot be found", bookId);
            return determineErrorResponseException(message, HttpStatus.NOT_FOUND);
        }
        else if(!userService.existByUserId(userId)){
            String message = String.format("User with id: %s cannot be found", userId);
            return determineErrorResponseException(message, HttpStatus.NOT_FOUND);
        }
        Optional<Collection<Rating>> bookRatings = ratingService.saveRating(ratingDto, bookId, userId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if(bookRatings.isEmpty()) {
            String message = String.format("Passed rating of value: %d does not vary from current rating", ratingDto.getVote());
            log.info(message);
            return new ResponseEntity<>(message, headers, HttpStatus.OK);
        }
        return new ResponseEntity<>(bookRatings.get(), headers, HttpStatus.CREATED);
    }

    //TODO - przenieś do osobnej klasy do obsługi wyjątków
    private ResponseEntity<String> determineErrorResponseException(String message, HttpStatus httpStatus) {
        log.info(message);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(message, headers, httpStatus);
    }
}
