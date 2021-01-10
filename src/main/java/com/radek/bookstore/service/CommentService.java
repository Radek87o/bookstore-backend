package com.radek.bookstore.service;

import com.radek.bookstore.model.dto.CommentDto;
import com.radek.bookstore.model.json.CommentJson;
import org.springframework.data.domain.Page;

public interface CommentService {
    Page<CommentJson> getCommentsByBookId(String bookId, Integer pageNumber, Integer pageSize);
    void saveComment(CommentDto commentDto, String bookId, String userId);
}
