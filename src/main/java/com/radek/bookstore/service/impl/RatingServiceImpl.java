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
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
        if(Objects.isNull(bookId)) {
            log.info("Given bookId is null");
            throw new BookStoreServiceException("Cannot find ratings due to null book id");
        }
        return ratingRepository.findByBookId(bookId);
    }

    @Override
    public Optional<Rating> getBookRating(String bookId, String userId) {
        return ratingRepository.findByBookIdAndUserId(bookId, userId);
    }

    @Override
    @Transactional
    public void saveRating(RatingDto ratingDto, String bookId, String userId) {
        Optional<Rating> ratingOptional = ratingRepository.findByBookIdAndUserId(bookId, userId);
        if(ratingOptional.isPresent()) {
            Rating currentRating = ratingOptional.get();
            if(currentRating.getVote()!=ratingDto.getVote()) {
                currentRating.setVote(ratingDto.getVote());
                ratingRepository.save(currentRating);
                log.info("Succesfully changed rating to: {} for book with id: {} by user with id: {}", currentRating.getVote(), bookId, userId);
            } else {
                return;
            }
        } else {
            Book book = bookRepository.findById(bookId).get();
            User user = userRepository.findById(userId).get();
            Rating rating = new Rating(ratingDto);
            rating.setUser(user);
            book.addRating(rating);
            bookRepository.save(book);
            log.info("Succesfully added new rating: {} for book with id: {} by user with id: {}", rating.getVote(), bookId, userId);
        }
    }
}
