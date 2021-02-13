package com.radek.bookstore.service;

import com.radek.bookstore.model.json.AuthorWrapper;

public interface AuthorService {

    AuthorWrapper findByAuthorId(String id, Integer page, Integer size);
    Boolean existByAuthorId(String id);
}
