package com.radek.bookstore.service;

import com.radek.bookstore.model.Rating;
import com.radek.bookstore.model.dto.RatingDto;

import java.util.List;
import java.util.Optional;

public interface RatingService {
    List<Rating> getBookRatings(String bookId);
    Optional<Rating> getBookRating(String bookId, String userId);
    void saveRating(RatingDto ratingDto, String bookId, String userId);
}
