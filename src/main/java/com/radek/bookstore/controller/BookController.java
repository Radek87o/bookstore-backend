package com.radek.bookstore.controller;

import com.radek.bookstore.model.Book;
import com.radek.bookstore.model.json.BookJson;
import com.radek.bookstore.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

    @CrossOrigin
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<Page<Book>> getBooksPage(@RequestParam(name = "page", required = false) Integer page,
                                       @RequestParam(name = "size", required = false) Integer size){
        if(Objects.isNull(page)) {
            page=0;
        }
        if(Objects.isNull(size)) {
            size=24;
        }
        return new ResponseEntity<>(bookService.listAllBooks(page, size), HttpStatus.OK);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<BookJson> getBook(@PathVariable("id") String bookId) {
        if(Objects.isNull(bookId)) {
            throw new  ResponseStatusException(HttpStatus.BAD_REQUEST, "Book id cannot be null");
        }
        BookJson requestedBook = bookService.findBook(bookId);
        if(Objects.isNull(requestedBook)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Cannot find book with id: %s", bookId));
        }
        return new ResponseEntity<>(requestedBook, HttpStatus.OK);
    }
}
