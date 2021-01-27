package com.radek.bookstore.service;

import com.radek.bookstore.generators.BookGenerator;
import com.radek.bookstore.model.Author;
import com.radek.bookstore.model.Book;
import com.radek.bookstore.model.Category;
import com.radek.bookstore.model.dto.AuthorDto;
import com.radek.bookstore.model.dto.BookDto;
import com.radek.bookstore.model.dto.CategoryDto;
import com.radek.bookstore.model.exception.BookStoreServiceException;
import com.radek.bookstore.model.json.BookJson;
import com.radek.bookstore.model.mapper.BookJsonMapper;
import com.radek.bookstore.repository.AuthorRepository;
import com.radek.bookstore.repository.BookRepository;
import com.radek.bookstore.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    BookRepository bookRepository;

    @Mock
    AuthorRepository authorRepository;

    BookJsonMapper bookJsonMapper = new BookJsonMapper();

    BookService bookService;

    @BeforeEach
    void setup(){
        bookService = new BookServiceImpl(bookRepository, authorRepository, bookJsonMapper);
    }

    @Test
    void shouldListAllBooks() {
        Page<Book> booksPage = generateExampleListOfBooks();

        when(bookRepository.findAll(PageRequest.of(0,5, Sort.by("createdDate").descending()))).thenReturn(booksPage);
        Page<Book> resultZero = bookService.listAllBooks(0, 5);

        assertEquals(booksPage, resultZero);

        verify(bookRepository).findAll(PageRequest.of(0,5, Sort.by("createdDate").descending()));
    }

    @Test
    void shouldListAllBooksMethodThrowBookStoreServiceExceptionWhenNonTransientDataAccessExceptionOccur(){
        PageRequest pageRequest = PageRequest.of(0, 5, Sort.by("createdDate").descending());
        doThrow(new NonTransientDataAccessException(""){}).when(bookRepository).findAll(pageRequest);

        assertThrows(BookStoreServiceException.class, () -> bookService.listAllBooks(0, 5));

        verify(bookRepository).findAll(pageRequest);
    }

    @Test
    void shouldFindBookWhenIdOfExistingBookPassed() {
        LocalDateTime createdTimestamp = LocalDateTime.of(LocalDate.of(2021, 1, 17), LocalTime.now());
        String bookId = "TestBookId9999";
        Book book = BookGenerator.generateBookWithId(createdTimestamp, bookId);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        BookJson result = bookService.findBook(bookId).get();

        assertEquals(book.getAuthor().getFirstName(), result.getAuthor().getFirstName());
        assertEquals(book.getAuthor().getLastName(), result.getAuthor().getLastName());
        assertEquals(book.getBasePrice(), result.getBasePrice());
        assertEquals(book.getImageUrl(), result.getImageUrl());
        assertEquals(book.getCreatedDate(), result.getCreatedDate());
        assertEquals(book.getTitle(), result.getTitle());

        verify(bookRepository).findById(bookId);
    }

    @Test
    void shouldFindBookMethodReturnNullWhenBookDoesNotExistInDatabase(){
        String bookId = "TestBookIdNull";

        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());
        Optional<BookJson> result = bookService.findBook(bookId);

        assertEquals(Optional.empty(), result);

        verify(bookRepository).findById(bookId);
    }

    @ParameterizedTest
    @MethodSource("setOfDescriptionsAndDescriptionParagraphsSizeResults")
    void shouldFindBookMethodCorrectlySplitDescriptionIntoParagraphs(String description, Integer numberOfParagraphs) {
        LocalDateTime createdTimestamp = LocalDateTime.of(LocalDate.of(2021, 1, 17), LocalTime.now());
        String bookId = "TestBookIdParams";
        Book book = BookGenerator.generateBookWithDescription(createdTimestamp, bookId, description);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        BookJson result = bookService.findBook(bookId).get();

        assertEquals(numberOfParagraphs, result.getDescription().size());

        verify(bookRepository).findById(bookId);
    }

    private static Stream<Arguments> setOfDescriptionsAndDescriptionParagraphsSizeResults() {
        return Stream.of(
                Arguments.of(BookGenerator.DESCRIPTION, 2),
                Arguments.of(BookGenerator.THREE_PARAGRAPH_DESCRIPTION, 3),
                Arguments.of(BookGenerator.FOUR_PARAGRAPH_DESCRIPTION, 4),
                Arguments.of(BookGenerator.FIVE_PARAGRAPH_DESCRIPTION, 5),
                Arguments.of(null, 0)
        );
    }

    @Test
    void shouldFindBookMethodThrowBookStoreServiceExceptionWhenNonTransientDataAccessExceptionOccur(){
        String bookId = "TestBookIdError";
        doThrow(new NonTransientDataAccessException(""){}).when(bookRepository).findById(bookId);

        assertThrows(BookStoreServiceException.class, () -> bookService.findBook(bookId));
        verify(bookRepository).findById(bookId);
    }

    @Test
    void shouldExistByIdMethodReturnFalseWhenBookWithGivenIdDoesNotExist() {
        String bookId = "TestNonExistingBookId";
        when(bookRepository.existsById(bookId)).thenReturn(false);

        boolean result = bookService.existsByBookId(bookId);

        assertFalse(result);
        verify(bookRepository).existsById(bookId);
    }

    @Test
    void shouldExistByIdMethodReturnTrueWhenBookWithGivenIdExists() {
        String bookId = "TestNonExistingBookId";
        when(bookRepository.existsById(bookId)).thenReturn(true);

        boolean result = bookService.existsByBookId(bookId);

        assertTrue(result);
        verify(bookRepository).existsById(bookId);
    }

    @Test
    void shouldExistByIdMethodThrowBookStoreServiceExceptionWhenNonTransientDataAccessExceptionOccur(){
        String bookId = "TestBookIdError";
        doThrow(new NonTransientDataAccessException(""){}).when(bookRepository).existsById(bookId);

        assertThrows(BookStoreServiceException.class, () -> bookService.existsByBookId(bookId));
        verify(bookRepository).existsById(bookId);
    }

    @Test
    void shouldSaveBookAsTheFirstOneOfTheGivenAuthor() {
        BookDto bookDto = BookGenerator.generateBookDto("Lokomotywa", "Julian", "Tuwim");
        Author author = new Author(bookDto.getAuthor());
        Book book = new Book(bookDto);
        author.addBook(book);
        lenient().when(authorRepository.save(any(Author.class))).thenReturn(author);

        Set<Book> result = bookService.saveBook(bookDto);

        Book expectedBook = author.getBooks().iterator().next();
        Book returnedBook = result.iterator().next();

        assertEquals(expectedBook, returnedBook);
        verify(authorRepository).save(any(Author.class));
    }

    @Test
    void shouldSaveBookAsTheNextOneOfTheGivenAuthor() {
        Book currentBook = BookGenerator.generateBook(LocalDateTime.now());
        currentBook.setTitle("Akademia Pana kleksa");
        Author author = new Author(AuthorDto.builder()
                .firstName("Jan")
                .lastName("Brzechwa")
                .build());
        author.setBooks(new HashSet<>(Collections.singleton(currentBook)));
        BookDto bookDto = BookGenerator.generateBookDto("Podróże Pana Kleksa", "Jan", "Brzechwa");
        Book newBook = new Book(bookDto);
        author.addBook(newBook);
        lenient().when(authorRepository.save(any(Author.class))).thenReturn(author);

        Set<Book> result = bookService.saveBook(bookDto);

        assertEquals(author.getBooks().size(), result.size());
        verify(authorRepository).save(any(Author.class));
    }

    @ParameterizedTest
    @MethodSource("setOfCategoriesContainingBook")
    void shouldSaveBookWithTheCorrectlyAssignedCategories(Set<Category> passedCategories, Set<Category> expectedCategories) {
        BookDto bookDto = BookGenerator.generateBookDto("Lokomotywa", "Julian", "Tuwim");
        bookDto.setCategories(passedCategories);
        Author author = new Author(bookDto.getAuthor());
        Book book = new Book(bookDto);
        bookDto.getCategories().forEach(category -> book.addCategory(category));
        author.addBook(book);
        lenient().when(authorRepository.save(any(Author.class))).thenReturn(author);

        Set<Book> result = bookService.saveBook(bookDto);
        Book resultBook = result.iterator().next();

        assertEquals(expectedCategories, resultBook.getCategories());
        verify(authorRepository).save(any(Author.class));
    }

    private static Stream<Arguments> setOfCategoriesContainingBook() {
        Set<Category> categories1 = Collections.singleton(new Category(CategoryDto.builder().name("Dla dzieci i młodzieży").build()));
        Set<Category> categories2 = Collections.EMPTY_SET;
        Set<Category> categories3 = new HashSet<>(Arrays.asList(
                new Category(CategoryDto.builder().name("Dla dzieci i młodzieży").build()),
                new Category(CategoryDto.builder().name("Fantasy").build()),
                new Category(CategoryDto.builder().name("Literatura piękna").build())));
        return Stream.of(
                Arguments.of(categories1, categories1),
                Arguments.of(categories2, null),
                Arguments.of(categories3, categories3)
        );
    }

    @Test
    void shouldSaveMethodThrowBookstoreServiceExceptionWhenNonNonTransientDataAccessExceptionOccur() {
        BookDto bookDto = BookGenerator.generateBookDto("Lokomotywa", "Julian", "Tuwim");

        doThrow(new NonTransientDataAccessException(""){}).when(authorRepository).save(any(Author.class));

        assertThrows(BookStoreServiceException.class, () -> bookService.saveBook(bookDto));

        verify(authorRepository).save(any(Author.class));
    }

    @Test
    void shouldFindByTitleReturnNonEmptyOptionalWhenBookWithGivenTitleAndAuthorExistsInDb() {
        BookDto bookDto = BookGenerator.generateBookDto("Lokomotywa", "Julian", "Tuwim");
        Book book = new Book(bookDto);
        book.setAuthor(new Author(bookDto.getAuthor()));

        when(bookRepository.findByTitle(bookDto.getTitle())).thenReturn(Arrays.asList(book));

        Optional<Book> result = bookService.findBookByTitle(bookDto);

        assertNotNull(result.get());

        verify(bookRepository).findByTitle(bookDto.getTitle());
    }

    @Test
    void shouldFindByTitleMethodReturnEmptyOptionalWhenBookWithGivenTitleButDifferentAuthorExistsInDb() {
        BookDto bookDto = BookGenerator.generateBookDto("Lokomotywa", "Julian", "Tuwim");

        BookDto bookDtoWithTheSameTitle = BookGenerator.generateBookDto("Lokomotywa", "Jan", "Brzechwa");
        Book bookWithTheSameTitle = new Book(bookDto);
        bookWithTheSameTitle.setAuthor(new Author(bookDtoWithTheSameTitle.getAuthor()));

        when(bookRepository.findByTitle(bookDto.getTitle())).thenReturn(Arrays.asList(bookWithTheSameTitle));

        Optional<Book> result = bookService.findBookByTitle(bookDto);

        assertEquals(Optional.empty(), result);

        verify(bookRepository).findByTitle(bookDto.getTitle());
    }

    @Test
    void shouldFindByTitleMethodReturnEmptyOptionalWhenBookWithGivenTitleDoesNotExistsInDb() {
        BookDto bookDto = BookGenerator.generateBookDto("Lokomotywa", "Julian", "Tuwim");

        when(bookRepository.findByTitle(bookDto.getTitle())).thenReturn(Collections.EMPTY_LIST);

        Optional<Book> result = bookService.findBookByTitle(bookDto);

        assertEquals(Optional.empty(), result);

        verify(bookRepository).findByTitle(bookDto.getTitle());
    }

    @Test
    void shouldFindByTitleMethodThrowBookstoreServiceExceptionWhenNonNonTransientDataAccessExceptionOccur() {
        BookDto bookDto = BookGenerator.generateBookDto("Lokomotywa", "Julian", "Tuwim");

        doThrow(new NonTransientDataAccessException(""){}).when(bookRepository).findByTitle(bookDto.getTitle());

        assertThrows(BookStoreServiceException.class, () -> bookService.findBookByTitle(bookDto));

        verify(bookRepository).findByTitle(bookDto.getTitle());
    }

    private Page<Book> generateExampleListOfBooks() {
        Book book1 = BookGenerator.generateBook(LocalDateTime.of(LocalDate.of(2021, 1, 17), LocalTime.now()));
        Book book2 = BookGenerator.generateBook(LocalDateTime.of(LocalDate.of(2021, 1, 12), LocalTime.now()));
        Book book3 = BookGenerator.generateBook(LocalDateTime.of(LocalDate.of(2021, 1, 14), LocalTime.now()));
        Book book4 = BookGenerator.generateBook(LocalDateTime.of(LocalDate.of(2021, 1, 15), LocalTime.now()));
        Book book5 = BookGenerator.generateBook(LocalDateTime.of(LocalDate.of(2021, 1, 10), LocalTime.now()));

        return new PageImpl<>(Arrays.asList(book1, book4, book3, book2, book5));
    }

}