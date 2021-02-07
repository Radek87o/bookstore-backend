package com.radek.bookstore.repository;

import com.radek.bookstore.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, String> {

    @Transactional
    List<Comment> findByBookId(String bookId);
}
