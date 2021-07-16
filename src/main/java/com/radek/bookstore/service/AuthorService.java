package com.radek.bookstore.service;

import com.radek.bookstore.model.response.AuthorWrapper;

public interface AuthorService {

    AuthorWrapper findByAuthorId(String id, Integer page, Integer size);
    Boolean existByAuthorId(String id);
}
