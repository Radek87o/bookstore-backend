package com.radek.bookstore.controller;

import ch.qos.logback.core.joran.conditional.ThenAction;
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
        if(bookByTitle.isPresent()) {
            String message = String.format("Book with title %s and author name: %s %s already exists",
                    bookToSave.getTitle(), bookToSave.getAuthor().getFirstName(), bookToSave.getAuthor().getLastName());
            log.info(message);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(message, headers, HttpStatus.BAD_REQUEST);
        }
        Collection<Book> books = bookService.saveBook(bookToSave, null);
        log.info("Book with title {} successfully saved", bookToSave.getTitle());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(books, headers, HttpStatus.CREATED);
    }

    @GetMapping(path = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<Book>> searchBookByKeyword(@RequestParam(value = "keyword", required = false) String keyword,
                                                          @RequestParam(name = "page", required = false) Integer page,
                                                          @RequestParam(name = "size", required = false) Integer size) {
        if(Objects.isNull(page)) {
            page=0;
        }
        if(Objects.isNull(size)) {
            size=24;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Page<Book> bookByKeyword = bookService.findBookByKeyword(keyword, page, size);
        return new ResponseEntity<>(bookByKeyword, headers, HttpStatus.OK);
    }

    @GetMapping(path = "/promos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<Book>> findBooksWithPromo(@RequestParam(name = "page", required = false) Integer page,
                                                         @RequestParam(name = "size", required = false) Integer size) {
        if(Objects.isNull(page)) {
            page=0;
        }
        if(Objects.isNull(size)) {
            size=24;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Page<Book> booksWithPromo = bookService.findBooksWithPromo(page, size);
        return new ResponseEntity<>(booksWithPromo, headers, HttpStatus.OK);
    }

    @GetMapping(path = "/activation/{bookId}/deactivate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deactivateBook(@PathVariable("bookId") String bookId) {
        return updateBookActivationStatus(bookId, false);
    }

    @GetMapping(path = "/activation/{bookId}/activate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> activateBook(@PathVariable("bookId") String bookId) {
        return updateBookActivationStatus(bookId, true);
    }

    @PutMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateBook(@PathVariable("id") String bookId, @Valid @RequestBody(required = false) BookDto bookToUpdate) {
        if(!bookService.existsByBookId(bookId)) {
            String message = String.format("Attempt to update non existing book with id: %s", bookId);
            log.info(message);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(message, headers, HttpStatus.NOT_FOUND);
        }
        if(Objects.isNull(bookToUpdate)) {
            String message = "Book cannot be null";
            log.info(message);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(message, headers, HttpStatus.BAD_REQUEST);
        }
        if(!UrlCustomValidator.urlValidator(bookToUpdate.getImageUrl())){
            String message = String.format("Incorrect format of image url: %s", bookToUpdate.getImageUrl());
            log.info(message);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(message, headers, HttpStatus.BAD_REQUEST);
        }
        if(Objects.nonNull(bookToUpdate.getPromoPrice()) && bookToUpdate.getPromoPrice().compareTo(bookToUpdate.getBasePrice())>0) {
            String message = "Promo price cannot be greater than base price";
            log.info(message);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(message, headers, HttpStatus.BAD_REQUEST);
        }
        Collection<Book> books = bookService.saveBook(bookToUpdate, bookId);
        log.info("Book with title {} successfully updated", bookToUpdate.getTitle());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(books, headers, HttpStatus.OK);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable("id") String bookId) {
        if(!bookService.existsByBookId(bookId)) {
            String message = String.format("Attempt to remove non existing book with id: %s", bookId);
            log.info(message);
            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        }
        bookService.deleteBookById(bookId);
        log.info("Successfully deleted book with id: {}", bookId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private ResponseEntity<?> updateBookActivationStatus(String bookId, boolean activationStatus) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if(!bookService.existsByBookId(bookId)) {
            String message = String.format("Book with id: {} does not exists", bookId);
            log.info(message);
            return new ResponseEntity<>(message, headers, HttpStatus.NOT_FOUND);
        }
        Book book = bookService.updateBookActivationStatus(bookId, activationStatus);
        return new ResponseEntity<>(book, headers, HttpStatus.OK);
    }

}
