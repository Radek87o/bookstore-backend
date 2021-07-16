package com.radek.bookstore.controller;

import com.radek.bookstore.model.Book;
import com.radek.bookstore.model.dto.BookDto;
import com.radek.bookstore.model.response.BookJson;
import com.radek.bookstore.service.BookService;
import com.radek.bookstore.utils.UrlCustomValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<?> getBooksPage(@RequestParam(name = "page", required = false) Integer page,
                                       @RequestParam(name = "size", required = false) Integer size){
        if(Objects.isNull(page)) {
            page=0;
        }
        if(Objects.isNull(size)) {
            size=24;
        }
        Page<Book> books = bookService.listAllBooks(page, size);
        return ResponseHelper.createOkResponse(books);
    }

    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getBook(@PathVariable("id") String bookId) {
        Optional<BookJson> requestedBook = bookService.findBook(bookId);
        if(requestedBook.isEmpty()) {
            String message = String.format("Cannot find book with id: %s", bookId);
            log.error(message);
            return ResponseHelper.createNotFoundResponse(message);
        }
        return ResponseHelper.createOkResponse(requestedBook.get());
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> saveBook(@Valid @RequestBody(required = false) BookDto bookToSave){
        if(Objects.isNull(bookToSave)) {
            String message = "Book cannot be null";
            log.info(message);
            return ResponseHelper.createBadRequestResponse(message);
        }
        if(!UrlCustomValidator.urlValidator(bookToSave.getImageUrl())){
            String message = String.format("Incorrect format of image url: %s", bookToSave.getImageUrl());
            log.info(message);
            return ResponseHelper.createBadRequestResponse(message);
        }
        if(Objects.nonNull(bookToSave.getPromoPrice()) && bookToSave.getPromoPrice().compareTo(bookToSave.getBasePrice())>0) {
            String message = "Promo price cannot be greater than base price";
            log.info(message);
            return ResponseHelper.createBadRequestResponse(message);
        }
        Optional<Book> bookByTitle = bookService.findBookByTitle(bookToSave);
        if(bookByTitle.isPresent()) {
            String message = String.format("Book with title %s and author name: %s %s already exists",
                    bookToSave.getTitle(), bookToSave.getAuthor().getFirstName(), bookToSave.getAuthor().getLastName());
            log.info(message);
            return ResponseHelper.createBadRequestResponse(message);
        }
        Collection<Book> books = bookService.saveBook(bookToSave, null);
        log.info("Book with title {} successfully saved", bookToSave.getTitle());
        return ResponseHelper.createCreatedResponse(books);
    }

    @GetMapping(path = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> searchBookByKeyword(@RequestParam(value = "keyword", required = false) String keyword,
                                                          @RequestParam(name = "page", required = false) Integer page,
                                                          @RequestParam(name = "size", required = false) Integer size) {
        if(Objects.isNull(page)) {
            page=0;
        }
        if(Objects.isNull(size)) {
            size=24;
        }
        Page<Book> bookByKeyword = bookService.findBookByKeyword(keyword, page, size);
        return ResponseHelper.createOkResponse(bookByKeyword);
    }

    @GetMapping(path = "/promos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findBooksWithPromo(@RequestParam(name = "page", required = false) Integer page,
                                                         @RequestParam(name = "size", required = false) Integer size) {
        if(Objects.isNull(page)) {
            page=0;
        }
        if(Objects.isNull(size)) {
            size=24;
        }
        Page<Book> booksWithPromo = bookService.findBooksWithPromo(page, size);
        return ResponseHelper.createOkResponse(booksWithPromo);
    }

    @GetMapping(path = "/active", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findActiveBooks(@RequestParam(name = "page", required = false) Integer page,
                                                @RequestParam(name = "size", required = false) Integer size) {
        if(Objects.isNull(page)) {
            page=0;
        }
        if(Objects.isNull(size)) {
            size=24;
        }
        Page<Book> activeBooks = bookService.findActiveBooks(page, size);
        return ResponseHelper.createOkResponse(activeBooks);
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
            return ResponseHelper.createNotFoundResponse(message);
        }
        if(Objects.isNull(bookToUpdate)) {
            String message = "Book cannot be null";
            log.info(message);
            return ResponseHelper.createBadRequestResponse(message);
        }
        if(!UrlCustomValidator.urlValidator(bookToUpdate.getImageUrl())){
            String message = String.format("Incorrect format of image url: %s", bookToUpdate.getImageUrl());
            log.info(message);
            return ResponseHelper.createBadRequestResponse(message);
        }
        if(Objects.nonNull(bookToUpdate.getPromoPrice()) && bookToUpdate.getPromoPrice().compareTo(bookToUpdate.getBasePrice())>0) {
            String message = "Promo price cannot be greater than base price";
            log.info(message);
            return ResponseHelper.createBadRequestResponse(message);
        }
        Collection<Book> books = bookService.saveBook(bookToUpdate, bookId);
        log.info("Book with title {} successfully updated", bookToUpdate.getTitle());
        return ResponseHelper.createOkResponse(books);
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
        if(!bookService.existsByBookId(bookId)) {
            String message = String.format("Book with id: {} does not exists", bookId);
            log.info(message);
            return ResponseHelper.createNotFoundResponse(message);
        }
        Book book = bookService.updateBookActivationStatus(bookId, activationStatus);
        return ResponseHelper.createOkResponse(book);
    }
}
