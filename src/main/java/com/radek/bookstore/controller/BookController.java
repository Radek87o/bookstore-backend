package com.radek.bookstore.controller;

import com.radek.bookstore.model.Book;
import com.radek.bookstore.model.dto.BookDto;
import com.radek.bookstore.model.json.BookJson;
import com.radek.bookstore.service.BookService;
import com.radek.bookstore.utils.UrlCustomValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final static Logger log = LoggerFactory.getLogger(BookController.class);
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Page<Book>> getBooksPage(@RequestParam(name = "page", required = false) Integer page,
                                       @RequestParam(name = "size", required = false) Integer size){
        if(Objects.isNull(page)) {
            page=0;
        }
        if(Objects.isNull(size)) {
            size=24;
        }
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        Page<Book> books = bookService.listAllBooks(page, size);
        return new ResponseEntity<>(books, responseHeaders, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getBook(@PathVariable("id") String bookId) {
        Optional<BookJson> requestedBook = bookService.findBook(bookId);
        if(requestedBook.isEmpty()) {
            String message = String.format("Cannot find book with id: %s", bookId);
            log.error(message);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(message, headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(requestedBook.get(), HttpStatus.OK);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> saveBook(@Valid @RequestBody(required = false) BookDto bookToSave){
        if(Objects.isNull(bookToSave)) {
            String message = "Book cannot be null";
            log.error(message);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(message, headers, HttpStatus.BAD_REQUEST);
        }
        if(!UrlCustomValidator.urlValidator(bookToSave.getImageUrl())){
            String message = String.format("Incorrect format of image url: %s", bookToSave.getImageUrl());
            log.info(message);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(message, headers, HttpStatus.BAD_REQUEST);
        }
        if(Objects.nonNull(bookToSave.getPromoPrice()) && bookToSave.getPromoPrice().compareTo(bookToSave.getBasePrice())>0) {
            String message = "Promo price cannot be greater than base price";
            log.info(message);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(message, headers, HttpStatus.BAD_REQUEST);
        }
        Optional<Book> bookByTitle = bookService.findBookByTitle(bookToSave);
        if(!bookByTitle.isEmpty()) {
            String message = String.format("Book with title %s and author name: %s %s already exists",
                    bookToSave.getTitle(), bookToSave.getAuthor().getFirstName(), bookToSave.getAuthor().getLastName());
            log.info(message);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(message, headers, HttpStatus.BAD_REQUEST);
        }
        Collection<Book> books = bookService.saveBook(bookToSave);
        log.info("Book with title {} successfully saved", bookToSave.getTitle());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(books, headers, HttpStatus.CREATED);
    }
}
