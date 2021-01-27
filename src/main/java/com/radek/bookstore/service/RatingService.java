package com.radek.bookstore.service;

import com.radek.bookstore.model.Rating;
import com.radek.bookstore.model.dto.RatingDto;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RatingService {
    List<Rating> getBookRatings(String bookId);
    Optional<Rating> getBookRating(String bookId, String userId);
    Optional<Collection<Rating>> saveRating(RatingDto ratingDto, String bookId, String userId);
}
