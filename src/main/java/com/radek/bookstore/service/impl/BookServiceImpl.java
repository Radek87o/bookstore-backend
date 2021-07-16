package com.radek.bookstore.service.impl;

import com.radek.bookstore.model.Author;
import com.radek.bookstore.model.Book;
import com.radek.bookstore.model.dto.AuthorDto;
import com.radek.bookstore.model.dto.BookDto;
import com.radek.bookstore.model.exception.BookStoreServiceException;
import com.radek.bookstore.model.response.BookJson;
import com.radek.bookstore.model.mapper.BookJsonMapper;
import com.radek.bookstore.repository.AuthorRepository;
import com.radek.bookstore.repository.BookRepository;
import com.radek.bookstore.repository.RatingRepository;
import com.radek.bookstore.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.RoundingMode;
import java.util.*;

@Service
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final RatingRepository ratingRepository;
    private final BookJsonMapper bookJsonMapper;

    public BookServiceImpl(BookRepository bookRepository,
                           AuthorRepository authorRepository,
                           RatingRepository ratingRepository, BookJsonMapper bookJsonMapper) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.ratingRepository = ratingRepository;
        this.bookJsonMapper = bookJsonMapper;
    }

    @Override
    public Page<Book> listAllBooks(Integer pageNumber, Integer pageSize) {
        try {
            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("lastUpdateDate").descending());
            return bookRepository.findAll(pageable);
        } catch (NonTransientDataAccessException exc) {
            String message = "An error occurred during retrieving page of books.";
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }

    @Override
    public Optional<BookJson> findBook(String id) {
        try {
            Optional<Book> bookOptional = bookRepository.findById(id);
            if(bookOptional.isEmpty()) {
                log.info("Requested book with id: {} is not found", id);
                return Optional.empty();
            }
            Book book = bookOptional.get();
            BookJson bookJson = bookJsonMapper.map(book, BookJson.class);
            bookJson.setDescription(extractDescriptionParagraphs(book.getDescription()));
            return Optional.of(bookJson);
        } catch (NonTransientDataAccessException exc) {
            String message = "An error occurred during retrieving book by id.";
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }

    @Override
    public boolean existsByBookId(String id) {
        try {
            return bookRepository.existsById(id);
        } catch (NonTransientDataAccessException exc) {
            String message = "An error occurred during checking whether book exists in database.";
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }

    @Override
    public Set<Book> saveBook(BookDto bookDto, String bookId) {
        try{
            Author author = retrieveAuthor(bookDto.getAuthor());
            roundPrices(bookDto);
            Book book = new Book(bookDto);
            bookDto.getCategories().forEach(book::addCategory);
            if(bookId!=null  && bookRepository.existsById(bookId)) {
                book.setId(bookId);
            }
            author.addBook(book);
            Author savedAuthor = authorRepository.save(author);
            return savedAuthor.getBooks();
        } catch (NonTransientDataAccessException exc) {
            String message = "An error occurred during saving book to database.";
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }

    @Override
    public Optional<Book> findBookByTitle(BookDto bookDto) {
        try {
            List<Book> booksByTitle = bookRepository.findByTitle(bookDto.getTitle());
            if(booksByTitle.isEmpty()) {
                return Optional.empty();
            }
            Optional<Book> optionalBook = booksByTitle.stream().filter(book -> isBookAuthorsMatching(book.getAuthor(), bookDto.getAuthor())).findFirst();
            return optionalBook;
        } catch (NonTransientDataAccessException exc) {
            String message = "An error occurred during attempt to find book by title.";
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }

    @Override
    public Page<Book> findBookByKeyword(String keyword, Integer pageNumber, Integer pageSize) {
        try {
            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            return bookRepository.findBookByKeyword(keyword, pageable);
        } catch (NonTransientDataAccessException exc) {
            String message = "An error occurred during attempt to find book by keyword.";
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }

    @Override
    public Page<Book> findBooksWithPromo(Integer page, Integer size) {
        try {
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastUpdateDate"));
            return bookRepository.findBooksWithPromo(pageRequest);
        } catch (NonTransientDataAccessException exc) {
            String message = "An error occurred during attempt to find books with promo.";
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }

    @Override
    public Book updateBookActivationStatus(String bookId, boolean activationStatus) {
        try {
            Book book = bookRepository.findById(bookId).get();
            book.setActive(activationStatus);
            return bookRepository.save(book);
        } catch (NonTransientDataAccessException exc) {
            String message = "An error occurred during attempt to change book status.";
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }

    @Override
    @Transactional
    public void deleteBookById(String bookId) {
        try {
            ratingRepository.deleteByBookId(bookId);
            bookRepository.deleteById(bookId);
        } catch (NonTransientDataAccessException exc) {
            String message = "An error occurred during attempt to delete book.";
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }

    @Override
    public Page<Book> findActiveBooks(Integer page, Integer size) {
        try {
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastUpdateDate"));
            return bookRepository.findActiveBooks(pageRequest);
        } catch (NonTransientDataAccessException exc) {
            String message = "An error occurred during attempt to find active books.";
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }

    private boolean isBookAuthorsMatching(Author author, AuthorDto authorDto) {
        boolean firstNameMatching = author.getFirstName().trim().equalsIgnoreCase(authorDto.getFirstName().trim());
        boolean lastNameMatching = author.getLastName().trim().equalsIgnoreCase(authorDto.getLastName().trim());
        if(firstNameMatching && lastNameMatching){
            return true;
        }
        return false;
    }

    private void roundPrices(BookDto bookDto) {
        if(bookDto.getBasePrice().scale()>2) {
            bookDto.setBasePrice(bookDto.getBasePrice().setScale(2, RoundingMode.CEILING));
        }
        if(Objects.nonNull(bookDto.getPromoPrice())) {
            if(bookDto.getPromoPrice().scale()>2) {
                bookDto.setPromoPrice(bookDto.getPromoPrice().setScale(2, RoundingMode.CEILING));
            }
        }
    }

    private List<String> extractDescriptionParagraphs(String description) {
        List<String> descriptionParagraphs = new ArrayList<>();
        String[] paragraphs = Objects.nonNull(description) ? description.split("\n") : new String[0];
        for (String paragraph: paragraphs) {
            if(paragraph.trim().length()>0) {
                descriptionParagraphs.add(paragraph);
            }
        }
        return descriptionParagraphs;
    }

    private Author retrieveAuthor(AuthorDto authorDto) {
        Optional<Author> authorOptional
                = authorRepository.findByFirstNameAndLastNameIgnoreCase(authorDto.getFirstName(), authorDto.getLastName());
        if(authorOptional.isPresent()) {
            return authorOptional.get();
        }
        return new Author(authorDto);
    }
}
