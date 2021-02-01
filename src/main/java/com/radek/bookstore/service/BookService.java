package com.radek.bookstore.service;

import com.radek.bookstore.model.Book;
import com.radek.bookstore.model.dto.BookDto;
import com.radek.bookstore.model.json.BookJson;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.Set;

public interface BookService {
    Page<Book> listAllBooks(Integer pageNumber, Integer pageSize);
    Optional<BookJson> findBook(String id);
    boolean existsByBookId(String id);
    Set<Book> saveBook(BookDto bookDto);
    Optional<Book> findBookByTitle(BookDto bookDto);
    Page<Book> findBookByKeyword(String keyword, Integer pageNumber, Integer pageSize);
}
