package com.radek.bookstore.service;

import com.radek.bookstore.model.Comment;
import com.radek.bookstore.model.dto.CommentDto;
import com.radek.bookstore.model.response.CommentJson;
import org.springframework.data.domain.Page;

import java.util.Collection;

public interface CommentService {
    Page<CommentJson> getCommentsByBookId(String bookId, Integer pageNumber);
    Collection<Comment> saveComment(CommentDto commentDto, String bookId, String userId);
}
