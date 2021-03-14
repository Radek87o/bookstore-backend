package com.radek.bookstore.controller;

import com.radek.bookstore.model.Rating;
import com.radek.bookstore.model.dto.RatingDto;
import com.radek.bookstore.service.BookService;
import com.radek.bookstore.service.RatingService;
import com.radek.bookstore.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
            log.info(message);
            return ResponseHelper.createNotFoundResponse(message);
        }
        List<Rating> bookRatings = ratingService.getBookRatings(bookId);
        return ResponseHelper.createOkResponse(bookRatings);
    }

    @GetMapping(path = "/{bookId}/user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getSingleRating(@PathVariable("bookId") String bookId, @PathVariable("userId") String userId) {
        if(!bookService.existsByBookId(bookId)) {
            String message = String.format("Cannot retrieve rating due to given book with id: %s does not exist", bookId);
            log.info(message);
            return ResponseHelper.createNotFoundResponse(message);
        }
        if(!userService.existByUserId(userId)) {
            String message = String.format("Cannot retrieve rating due to given user with id: %s does not exist", userId);
            log.info(message);
            return ResponseHelper.createNotFoundResponse(message);
        }
        Optional<Rating> bookRatingOptional = ratingService.getBookRating(bookId, userId);
        Rating bookRating = bookRatingOptional.isEmpty()
                ? new Rating(new RatingDto(0))
                : bookRatingOptional.get();
        return ResponseHelper.createOkResponse(bookRating);
    }

    @PostMapping(path = "/{bookId}/user/{userId}")
    public ResponseEntity<?> saveRating(@Valid @RequestBody RatingDto ratingDto, @PathVariable("bookId") String bookId, @PathVariable("userId") String userId){
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
        Optional<Collection<Rating>> bookRatings = ratingService.saveRating(ratingDto, bookId, userId);
        if(bookRatings.isEmpty()) {
            String message = String.format("Passed rating of value: %d does not vary from current rating", ratingDto.getVote());
            log.info(message);
            return ResponseHelper.createOkResponse(message);
        }
        return ResponseHelper.createCreatedResponse(bookRatings);
    }
}
