package com.radek.bookstore.repository;

import com.radek.bookstore.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, String> {

    @Transactional
    List<Rating> findByBookId(String bookId);

    @Transactional
    @Query(nativeQuery = true, value = "SELECT * FROM rating r WHERE r.book_id=:bookId AND r.user_id=:userId")
    Optional<Rating> findByBookIdAndUserId(@Param("bookId") String bookId, @Param("userId") String userId);
}
