package com.radek.bookstore.service;

import com.radek.bookstore.generators.BookGenerator;
import com.radek.bookstore.generators.RatingGenerator;
import com.radek.bookstore.generators.UserGenerator;
import com.radek.bookstore.model.Book;
import com.radek.bookstore.model.Rating;
import com.radek.bookstore.model.User;
import com.radek.bookstore.model.dto.RatingDto;
import com.radek.bookstore.model.exception.BookStoreServiceException;
import com.radek.bookstore.repository.BookRepository;
import com.radek.bookstore.repository.RatingRepository;
import com.radek.bookstore.repository.UserRepository;
import com.radek.bookstore.service.impl.RatingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.NonTransientDataAccessException;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    RatingRepository ratingRepository;

    @Mock
    BookRepository bookRepository;

    @Mock
    UserRepository userRepository;

    RatingService ratingService;

    @BeforeEach
    void setup() {
        ratingService = new RatingServiceImpl(ratingRepository, bookRepository, userRepository);
    }

    @Test
    void shouldGetBookRatingsMethodReturnCollectionOfRatingsWhenIdOfExistingBookPassed() {
        String bookId = "testBookId";
        List<Rating> ratings = generateTestListOfRatings();
        when(ratingRepository.findByBookId(bookId)).thenReturn(ratings);

        List<Rating> result = ratingService.getBookRatings(bookId);

        assertEquals(ratings, result);

        verify(ratingRepository).findByBookId(bookId);
    }

    @Test
    void shouldGetBookRatingsMethodThrowBookStoreServiceExceptionWhenNonTransientDataAccessExceptionOccur(){
        String bookId = "TestBookIdError";
        doThrow(new NonTransientDataAccessException(""){}).when(ratingRepository).findByBookId(bookId);

        assertThrows(BookStoreServiceException.class, () -> ratingService.getBookRatings(bookId));
        verify(ratingRepository).findByBookId(bookId);
    }

    @Test
    void shouldGetBookRatingMethodReturnNonEmptyOptionalWhenBookHasRatingOfGivenUser () {
        String bookId = "someBookId";
        String userId = "someUserId";
        Rating rating = RatingGenerator.generateRating("someRatingId");
        when(ratingRepository.findByBookIdAndUserId(bookId, userId)).thenReturn(Optional.of(rating));

        Optional<Rating> result = ratingService.getBookRating(bookId, userId);

        assertEquals(rating, result.get());

        verify(ratingRepository).findByBookIdAndUserId(bookId, userId);
    }

    @Test
    void shouldGetBookRatingMethodReturnEmptyOptionalWhenBookHasNotRatingOfGivenUser () {
        String bookId = "someBookId";
        String userId = "someUserId";
        when(ratingRepository.findByBookIdAndUserId(bookId, userId)).thenReturn(Optional.empty());

        Optional<Rating> result = ratingService.getBookRating(bookId, userId);

        assertEquals(Optional.empty(), result);

        verify(ratingRepository).findByBookIdAndUserId(bookId, userId);
    }

    @Test
    void shouldGetBookRatingMethodThrowBookStoreServiceExceptionWhenNonTransientDataAccessExceptionOccur(){
        String bookId = "TestBookIdError";
        String userId = "TestUserIdError";
        doThrow(new NonTransientDataAccessException(""){}).when(ratingRepository).findByBookIdAndUserId(bookId, userId);

        assertThrows(BookStoreServiceException.class, () -> ratingService.getBookRating(bookId, userId));
        verify(ratingRepository).findByBookIdAndUserId(bookId, userId);
    }

    @Test
    void shouldSaveRatingMethodChangeRatingVoteWhenNewUserRatingDiffersFromCurrentRating() {
        Rating currentRating = RatingGenerator.generateRatingWithVote("someRatingId", 4);
        Rating updatedRating = RatingGenerator.generateRatingWithVote("someRatingId", 3);
        RatingDto newRatingDto = new RatingDto(3);
        String bookId = "someBookId";
        String userId = "someUserId";
        when(ratingRepository.findByBookIdAndUserId(bookId, userId)).thenReturn(Optional.of(currentRating));
        when(ratingRepository.save(any(Rating.class))).thenReturn(updatedRating);

        Optional<Collection<Rating>> result = ratingService.saveRating(newRatingDto, bookId, userId);

        Rating resultRating = result.get().iterator().next();

        assertEquals(3, resultRating.getVote());

        verify(ratingRepository).findByBookIdAndUserId(bookId, userId);
        verify(ratingRepository).save(any(Rating.class));
    }

    @Test
    void shouldSaveRatingMethodReturnOptionalEmptyWhenNewUserVoteIsTheSameAsCurrentRatingVote() {
        Rating currentRating = RatingGenerator.generateRatingWithVote("someRatingId", 4);
        RatingDto newRatingDto = new RatingDto(4);
        String bookId = "someBookId";
        String userId = "someUserId";
        when(ratingRepository.findByBookIdAndUserId(bookId, userId)).thenReturn(Optional.of(currentRating));

        Optional<Collection<Rating>> result = ratingService.saveRating(newRatingDto, bookId, userId);

        assertEquals(Optional.empty(), result);

        verify(ratingRepository).findByBookIdAndUserId(bookId, userId);
    }

    @Test
    void shouldSaveRatingMethodReturnOptionalWithAllBooksRatingsWhenCurrentRatingDoesNotExists() {
        RatingDto newRatingDto = new RatingDto(5);
        RatingGenerator.generateRatingWithVote("newRatingId", 5);
        String bookId = "someBookId";
        Book book = BookGenerator.generateBookWithId(LocalDateTime.now(), bookId);
        String userId = "someUserId";
        User user = UserGenerator.generateUser(userId);

        when(ratingRepository.findByBookIdAndUserId(bookId, userId)).thenReturn(Optional.empty());
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.save(book)).thenReturn(book);

        Optional<Collection<Rating>> result = ratingService.saveRating(newRatingDto, bookId, userId);
        Rating savedRating = result.get().iterator().next();

        assertTrue(result.isPresent());
        assertEquals(5, savedRating.getVote());

        verify(ratingRepository).findByBookIdAndUserId(bookId, userId);
        verify(bookRepository).findById(bookId);
        verify(userRepository).findById(userId);
        verify(bookRepository).save(book);
    }

    @Test
    void shouldSaveRatingMethodThrowBookStoreServiceExceptionWhenNonTransientDataAccessExceptionOccurByUpdatingCurrentRating(){
        Rating currentRating = RatingGenerator.generateRatingWithVote("someRatingId", 4);
        RatingDto ratingDto = new RatingDto(5);
        String bookId = "TestBookIdError";
        String userId = "TestUserIdError";
        when(ratingRepository.findByBookIdAndUserId(bookId, userId)).thenReturn(Optional.of(currentRating));
        doThrow(new NonTransientDataAccessException(""){}).when(ratingRepository).save(any(Rating.class));

        assertThrows(BookStoreServiceException.class, () -> ratingService.saveRating(ratingDto, bookId, userId));

        verify(ratingRepository).findByBookIdAndUserId(bookId, userId);
        verify(ratingRepository).save(any(Rating.class));
    }

    @Test
    void shouldSaveRatingMethodThrowBookStoreServiceExceptionWhenNonTransientDataAccessExceptionOccurBySavingNewRating(){
        RatingDto ratingDto = new RatingDto(5);
        String bookId = "TestBookIdError";
        Book book = BookGenerator.generateBookWithId(LocalDateTime.now(), bookId);
        String userId = "TestUserIdError";
        User user = UserGenerator.generateUser(userId);
        when(ratingRepository.findByBookIdAndUserId(bookId, userId)).thenReturn(Optional.empty());
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        doThrow(new NonTransientDataAccessException(""){}).when(bookRepository).save(book);

        assertThrows(BookStoreServiceException.class, () -> ratingService.saveRating(ratingDto, bookId, userId));

        verify(ratingRepository).findByBookIdAndUserId(bookId, userId);
        verify(bookRepository).findById(bookId);
        verify(userRepository).findById(userId);
        verify(bookRepository).save(book);
    }

    private List<Rating> generateTestListOfRatings() {
        Rating rating1 = RatingGenerator.generateRating("rating1");
        Rating rating2 = RatingGenerator.generateRating("rating2");
        Rating rating3 = RatingGenerator.generateRating("rating3");

        return Arrays.asList(rating1, rating2, rating3);
    }
}
