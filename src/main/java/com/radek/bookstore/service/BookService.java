package com.radek.bookstore.service;

import com.radek.bookstore.model.Book;
import com.radek.bookstore.model.json.BookJson;
import org.springframework.data.domain.Page;

public interface BookService {
    Page<Book> listAllBooks(Integer pageNumber, Integer pageSize);
    BookJson findBook(String id);
    boolean existsByBookId(String id);
}
