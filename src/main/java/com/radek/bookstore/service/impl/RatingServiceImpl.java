package com.radek.bookstore.service.impl;

import com.radek.bookstore.model.Book;
import com.radek.bookstore.model.Rating;
import com.radek.bookstore.model.User;
import com.radek.bookstore.model.dto.RatingDto;
import com.radek.bookstore.model.exception.BookStoreServiceException;
import com.radek.bookstore.repository.BookRepository;
import com.radek.bookstore.repository.RatingRepository;
import com.radek.bookstore.repository.UserRepository;
import com.radek.bookstore.service.RatingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
public class RatingServiceImpl implements RatingService {

    private final static Logger log = LoggerFactory.getLogger(RatingServiceImpl.class);

    private final RatingRepository ratingRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public RatingServiceImpl(RatingRepository ratingRepository, BookRepository bookRepository, UserRepository userRepository) {
        this.ratingRepository = ratingRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Rating> getBookRatings(String bookId) {
        try {
            return ratingRepository.findByBookId(bookId);
        } catch (NonTransientDataAccessException exc) {
            String message = "An error occurred during retrieving book ratings";
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }

    @Override
    public Optional<Rating> getBookRating(String bookId, String userId) {
        try {
            return ratingRepository.findByBookIdAndUserId(bookId, userId);
        } catch (NonTransientDataAccessException exc) {
            String message = "An error occurred during retrieving single book rating";
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }

    @Override
    @Transactional
    public Optional<Collection<Rating>> saveRating(RatingDto ratingDto, String bookId, String userId) {
        try {
            Optional<Rating> ratingOptional = ratingRepository.findByBookIdAndUserId(bookId, userId);
            if(ratingOptional.isPresent()) {
                Rating currentRating = ratingOptional.get();
                if(currentRating.getVote()!=ratingDto.getVote()) {
                    return saveUpdatedRating(currentRating, ratingDto, bookId, userId);
                } else {
                    return Optional.empty();
                }
            } else {
                return saveNewRating(ratingDto, bookId, userId);
            }
        } catch (NonTransientDataAccessException exc) {
            String message = "An error occurred during saving book rating";
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }

    private Optional<Collection<Rating>> saveNewRating(RatingDto ratingDto, String bookId, String userId) {
        Book book = bookRepository.findById(bookId).get();
        User user = userRepository.findById(userId).get();
        Rating rating = new Rating(ratingDto);
        rating.setUser(user);
        book.addRating(rating);
        Book savedBook = bookRepository.save(book);
        log.info("Succesfully added new rating: {} for book with id: {} by user with id: {}", rating.getVote(), bookId, userId);
        return Optional.of(savedBook.getRatings());
    }

    private Optional<Collection<Rating>> saveUpdatedRating(Rating currentRating, RatingDto updatedRating, String bookId, String userId) {
        currentRating.setVote(updatedRating.getVote());
        Rating newRating = ratingRepository.save(currentRating);
        log.info("Succesfully changed newRating to: {} for book with id: {} by user with id: {}", currentRating.getVote(), bookId, userId);
        return Optional.of(Collections.singleton(newRating));
    }
}
