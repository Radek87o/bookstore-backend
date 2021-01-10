package com.radek.bookstore.repository;

import com.radek.bookstore.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;

public interface CommentRepository extends JpaRepository<Comment, String> {

    @Transactional
    Page<Comment> findByBookId(String bookId, Pageable pageable);
}
