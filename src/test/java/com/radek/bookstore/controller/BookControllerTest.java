package com.radek.bookstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radek.bookstore.generators.BookGenerator;
import com.radek.bookstore.model.Author;
import com.radek.bookstore.model.Book;
import com.radek.bookstore.model.dto.AuthorDto;
import com.radek.bookstore.model.dto.BookDto;
import com.radek.bookstore.model.json.BookJson;
import com.radek.bookstore.model.mapper.BookJsonMapper;
import com.radek.bookstore.service.BookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest(BookController.class)
class BookControllerTest {

    @MockBean
    private BookService bookService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    BookJsonMapper bookJsonMapper = new BookJsonMapper();

    @Test
    void shouldGetBooksPageMethodReturnedBooksPageWhenParamsOfPageAndSizeNotPassed() throws Exception {
        Page<Book> books = getTestBooksCollection();

        when(bookService.listAllBooks(0,24)).thenReturn(books);

        String url = "/api/books";

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(books)));

        verify(bookService).listAllBooks(0,24);
    }

    @Test
    void shouldGetBooksPageMethodReturnCorrectBooksPageWhenParamsPassed() throws Exception {
        Page<Book> books = getTestBooksCollection();

        when(bookService.listAllBooks(0,5)).thenReturn(books);

        String url = "/api/books";

        mockMvc.perform(get(url)
                .param("page", "0")
                .param("size", "5")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(books)));

        verify(bookService).listAllBooks(0,5);
    }

    @Test
    void shouldGetBookMethodReturnBookWhenDbContainsBookWithPassedId() throws Exception {
        String bookId = "test_existing-book";
        Book searchedBook = BookGenerator.generateBookWithId(LocalDateTime.now(), bookId);
        BookJson bookJson = bookJsonMapper.map(searchedBook, BookJson.class);
        when(bookService.findBook(bookId)).thenReturn(Optional.of(bookJson));

        String url = String.format("/api/books/%s", bookId);

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(bookJson)));

        verify(bookService).findBook(bookId);
    }

    @ParameterizedTest
    @MethodSource("setOfNonExistingIds")
    void shouldGetBookMethodReturnNotFoundStatusWhenNullPassedAsBookId(String id) throws Exception {
        when(bookService.findBook(id)).thenReturn(Optional.empty());

        String url = String.format("/api/books/%s", id);

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(bookService).findBook(id);
    }

    private static Stream<Arguments> setOfNonExistingIds() {
        return Stream.of(
                Arguments.of("aacweszasdqwadewrvsas"),
                Arguments.of("null"),
                Arguments.of("nonExistingId")
        );
    }

    @Test
    void shouldSaveBookMethodReturnCollectionOfAllAuthorBooksWhenBookIsValid() throws Exception {
        BookDto bookDto = BookGenerator.generateBookDto();
        Set<Book> bookSet = Collections.singleton(new Book(bookDto));
        when(bookService.saveBook(any(BookDto.class))).thenReturn(bookSet);

        String url = "/api/books/";

        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(bookDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(bookSet)));

        verify(bookService).saveBook(any(BookDto.class));
    }

    @ParameterizedTest
    @MethodSource("setOfInvalidBookDtos")
    void shouldSaveBookMethodReturnBadRequestWhenBookDtoIsInvalid(BookDto bookDto) throws Exception {
        String url = "/api/books/";

        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(bookDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).saveBook(bookDto);
    }

    private static Stream<Arguments> setOfInvalidBookDtos() {
        String tooShortDescription = BookGenerator.DESCRIPTION;
        String tooLongDescription = new StringBuilder()
                .append(BookGenerator.FIVE_PARAGRAPH_DESCRIPTION)
                .append(BookGenerator.FIVE_PARAGRAPH_DESCRIPTION)
                .append(BookGenerator.FIVE_PARAGRAPH_DESCRIPTION)
                .append(BookGenerator.FIVE_PARAGRAPH_DESCRIPTION)
                .append(BookGenerator.FIVE_PARAGRAPH_DESCRIPTION)
                .append(BookGenerator.FIVE_PARAGRAPH_DESCRIPTION)
                .toString();

        String tooLongName = "JulianTuwimLokomotywaPolskaDwudziestolecieMiędzywojenne";

        return Stream.of(
                Arguments.of(BookGenerator.generateBookDtoWithTitle("")),
                Arguments.of(BookGenerator.generateBookDtoWithTitle(null)),
                Arguments.of(BookGenerator.generateBookDtoWithTitle("a")),
                Arguments.of(BookGenerator.generateBookDtoWithSubtitle("b")),
                Arguments.of(BookGenerator.generateBookDtoWithSubtitle("")),
                Arguments.of(BookGenerator.generateBookDtoWithDescription(null)),
                Arguments.of(BookGenerator.generateBookDtoWithDescription(tooShortDescription)),
                Arguments.of(BookGenerator.generateBookDtoWithDescription(tooLongDescription)),
                Arguments.of(BookGenerator.generateBookDtoWithImageUrl(null)),
                Arguments.of(BookGenerator.generateBookDtoWithIssueYear(null)),
                Arguments.of(BookGenerator.generateBookDtoWithIssueYear(1999)),
                Arguments.of(BookGenerator.generateBookDtoWithIssueYear(2023)),
                Arguments.of(BookGenerator.generateBookDtoWithPages(0)),
                Arguments.of(BookGenerator.generateBookDtoWithPages(10000)),
                Arguments.of(BookGenerator.generateBookDtoWithAuthor(null)),
                Arguments.of(BookGenerator.generateBookDtoWithAuthor(AuthorDto.builder().firstName("OnlyFirstName").build())),
                Arguments.of(BookGenerator.generateBookDtoWithAuthor(AuthorDto.builder().lastName("OnlyLastName").build())),
                Arguments.of(BookGenerator.generateBookDtoWithAuthor(AuthorDto.builder().firstName("J").lastName("Tuwim").build())),
                Arguments.of(BookGenerator.generateBookDtoWithAuthor(AuthorDto.builder().firstName("Julian").lastName("T").build())),
                Arguments.of(BookGenerator.generateBookDtoWithAuthor(AuthorDto.builder()
                                                                                .firstName(tooLongName)
                                                                                .lastName("Tuwim")
                                                                                .build())),
                Arguments.of(BookGenerator.generateBookDtoWithAuthor(AuthorDto.builder()
                                                                                .firstName("Julian")
                                                                                .lastName(tooLongName)
                                                                                .build())),
                Arguments.of(BookGenerator.generateBookDtoWithBasePrice(null)),
                Arguments.of(BookGenerator.generateBookDtoWithBasePrice(BigDecimal.valueOf(1000L))),
                Arguments.of(BookGenerator.generateBookDtoWithPromoPrice(BigDecimal.valueOf(1000L))),
                Arguments.of(BookGenerator.generateBookDtoWithUnitsInStock(-1)),
                Arguments.of(BookGenerator.generateBookDtoWithUnitsInStock(10000)),
                Arguments.of(BookGenerator.generateBookDtoWithCategories(null)),
                Arguments.of(BookGenerator.generateBookDtoWithCategories(Collections.EMPTY_SET))
        );
    }

    @Test
    void shouldSaveBookMethodReturnBadRequestStatusWhenBookDtoIsNull() throws Exception {
        String url = "/api/books/";
        String message = "Book cannot be null";

        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(null))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(message));

        verify(bookService, never()).saveBook(null);
    }

    @ParameterizedTest
    @MethodSource("setOfInvalidImageUrls")
    void shouldSaveBookMethodReturnBadRequestWhenBookDtoHasInvalidImageUrl(String invalidUrl, BookDto bookDto) throws Exception {
        String url = "/api/books/";
        String message = String.format("Incorrect format of image url: %s", invalidUrl);;

        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(bookDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(message));

        verify(bookService, never()).saveBook(bookDto);
    }

    private static Stream<Arguments> setOfInvalidImageUrls() {
        String invalidUrl1 = "";
        String invalidUrl2 = "https:://cdn-lubimyczytac.pl/upload/books/4942000/4942761/848896-352x500.jpg";
        String invalidUrl3 = "https://cdn-lubimyczytac/upload/books/4942000/4942761/848896-352x500";
        String invalidUrl4 = "cokolwiek";

        return Stream.of(
                Arguments.of(invalidUrl1, BookGenerator.generateBookDtoWithImageUrl(invalidUrl1)),
                Arguments.of(invalidUrl2, BookGenerator.generateBookDtoWithImageUrl(invalidUrl2)),
                Arguments.of(invalidUrl3, BookGenerator.generateBookDtoWithImageUrl(invalidUrl3)),
                Arguments.of(invalidUrl4, BookGenerator.generateBookDtoWithImageUrl(invalidUrl4))
        );
    }

    @Test
    void shouldSaveBookMethodReturnBadRequestWhenPromoPriceIsGreaterThanBasePrice() throws Exception {
        String url = "/api/books/";
        String message = "Promo price cannot be greater than base price";
        BookDto bookDto = BookGenerator.generateBookDtoWithBasePriceAndPromoPrice(BigDecimal.valueOf(23.50), BigDecimal.valueOf(24L));

        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(bookDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(message));

        verify(bookService, never()).saveBook(bookDto);
    }

    @Test
    void shouldSaveBookMethodReturnBadRequestWhenBookWithTheSameAuthorAndTitleAlreadyExists() throws Exception {
        AuthorDto authorDto = AuthorDto.builder().firstName("Julian").lastName("Tuwim").build();
        BookDto bookDto = BookGenerator.generateBookDto("Lokomotywa", authorDto.getFirstName(), authorDto.getLastName());
        Book bookToSave = new Book(bookDto);
        bookToSave.setAuthor(new Author(authorDto));

        when(bookService.findBookByTitle(any(BookDto.class))).thenReturn(Optional.of(bookToSave));

        String url = "/api/books/";
        String message = String.format("Book with title %s and author name: %s %s already exists",
                bookToSave.getTitle(), bookToSave.getAuthor().getFirstName(), bookToSave.getAuthor().getLastName());

        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(bookDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(message));

        verify(bookService, never()).saveBook(bookDto);
    }

    private Page<Book> getTestBooksCollection() {
        Book book1 = BookGenerator.generateBook(LocalDateTime.of(LocalDate.of(2020, 1, 21), LocalTime.now()));
        Book book2 = BookGenerator.generateBook(LocalDateTime.of(LocalDate.of(2020, 1, 11), LocalTime.now()));
        Book book3 = BookGenerator.generateBook(LocalDateTime.of(LocalDate.of(2020, 1, 15), LocalTime.now()));
        Book book4 = BookGenerator.generateBook(LocalDateTime.of(LocalDate.of(2020, 1, 18), LocalTime.now()));
        Book book5 = BookGenerator.generateBook(LocalDateTime.of(LocalDate.of(2020, 1, 20), LocalTime.now()));
        return new PageImpl<>(List.of(book1, book2, book3, book4, book5));
    }
}