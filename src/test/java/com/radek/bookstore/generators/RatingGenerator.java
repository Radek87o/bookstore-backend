package com.radek.bookstore.generators;

import com.radek.bookstore.model.Rating;
import com.radek.bookstore.model.dto.RatingDto;

import java.time.LocalDateTime;
import java.util.Random;

public class RatingGenerator {

    private static int next_vote = new Random().nextInt(5)+1;

    public static Rating generateRatingWithVote(String ratingId, int vote) {
        Rating rating = generateBaseRating(ratingId);
        rating.setVote(vote);
        return rating;
    }

    public static Rating generateRating(String ratingId) {
        return generateBaseRating(ratingId);
    }

    private static Rating generateBaseRating(String ratingId) {
        RatingDto ratingDto = new RatingDto(next_vote);
        Rating rating = new Rating(ratingDto);
        rating.setCreatedDate(LocalDateTime.now());
        rating.setUpdated(LocalDateTime.now());
        rating.setUser(UserGenerator.generateUser("someUserId"));
        rating.setBook(BookGenerator.generateBookWithId(LocalDateTime.now(), "someBookId"));
        return rating;
    }
}
