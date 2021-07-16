package com.radek.bookstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radek.bookstore.generators.BookGenerator;
import com.radek.bookstore.model.Author;
import com.radek.bookstore.model.Book;
import com.radek.bookstore.model.dto.AuthorDto;
import com.radek.bookstore.model.dto.BookDto;
import com.radek.bookstore.model.exception.BookStoreServiceException;
import com.radek.bookstore.model.response.BookJson;
import com.radek.bookstore.model.mapper.BookJsonMapper;
import com.radek.bookstore.security.filter.JwtAccessDeniedHandler;
import com.radek.bookstore.security.filter.JwtAuthenticationEntryPoint;
import com.radek.bookstore.security.utility.JwtTokenProvider;
import com.radek.bookstore.service.BookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest(value = BookController.class)
@WithMockUser(username = "user", roles = "ADMIN")
@AutoConfigureMockMvc(addFilters = false)
class BookControllerTest {

    @MockBean
    private BookService bookService;

    @MockBean
    JwtTokenProvider jwtTokenProvider;

    @MockBean
    JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @MockBean
    JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    @Qualifier("userDetailsService")
    UserDetailsService userDetailsService;

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
    void shouldGetBooksPageMethodReturnInternalServerErrorWhenSomethingWentWrongOnServer() throws Exception {
        when(bookService.listAllBooks(0,24)).thenThrow(new BookStoreServiceException("An error occurred during retrieving page of books."));

        String url = "/api/books";

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(bookService).listAllBooks(0,24);
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
    void shouldGetBookMethodReturnInternalServerErrorWhenSomethingWentWrongOnServer() throws Exception {
        String bookId = "test_existing-book";
        when(bookService.findBook(bookId)).thenThrow(new BookStoreServiceException("An error occurred during retrieving book by id."));

        String url = String.format("/api/books/%s", bookId);

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(bookService).findBook(bookId);
    }

    @Test
    void shouldSaveBookMethodReturnCollectionOfAllAuthorBooksWhenBookIsValid() throws Exception {
        BookDto bookDto = BookGenerator.generateBookDto();
        Set<Book> bookSet = Collections.singleton(new Book(bookDto));
        when(bookService.saveBook(any(BookDto.class), any())).thenReturn(bookSet);

        String url = "/api/books/";

        mockMvc.perform(post(url)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(bookDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(bookSet)));

        verify(bookService).saveBook(any(BookDto.class), any());
    }

    @ParameterizedTest
    @MethodSource("setOfInvalidBookDtos")
    void shouldSaveBookMethodReturnBadRequestWhenBookDtoIsInvalid(BookDto bookDto) throws Exception {
        String url = "/api/books/";

        mockMvc.perform(post(url)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(bookDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).saveBook(bookDto, null);
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
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(null))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(message));

        verify(bookService, never()).saveBook(null, null);
    }

    @ParameterizedTest
    @MethodSource("setOfInvalidImageUrls")
    void shouldSaveBookMethodReturnBadRequestWhenBookDtoHasInvalidImageUrl(String invalidUrl, BookDto bookDto) throws Exception {
        String url = "/api/books/";
        String message = String.format("Incorrect format of image url: %s", invalidUrl);;

        mockMvc.perform(post(url)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(bookDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(message));

        verify(bookService, never()).saveBook(bookDto, null);
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
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(bookDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(message));

        verify(bookService, never()).saveBook(bookDto, null);
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
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(bookDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(message));

        verify(bookService).findBookByTitle(any(BookDto.class));
        verify(bookService, never()).saveBook(bookDto, null);
    }

    @Test
    void shouldSaveBookMethodReturnInternalServerErrorWhenSomethingWentWrongOnServer() throws Exception {
        BookDto bookDto = BookGenerator.generateBookDto();
        when(bookService.findBookByTitle(any(BookDto.class))).thenReturn(Optional.empty());
        when(bookService.saveBook(any(BookDto.class), any())).thenThrow(new BookStoreServiceException("An error occurred during saving book to database."));

        String url = "/api/books";

        mockMvc.perform(post(url)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(bookDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(bookService).findBookByTitle(any(BookDto.class));
        verify(bookService).saveBook(any(BookDto.class), any());
    }

    @Test
    void shouldSearchBookByKeywordMethodReturnOkStatusWithNonEmptyPageOfBooksWhenNonNullKeywordPassed() throws Exception {
        Page<Book> books = getTestBooksCollection();
        String keyword = "someKeyword";

        when(bookService.findBookByKeyword(keyword, 0, 24)).thenReturn(books);
        String url = "/api/books/search";

        mockMvc.perform(get(url)
                .param("keyword", keyword)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(books)));

        verify(bookService).findBookByKeyword(keyword, 0, 24);
    }

    @Test
    void shouldSearchBookByKeywordMethodReturnOkStatusWithNonEmptyPageOfBooksWhenNonNullKeywordWithPageableParamsPassed() throws Exception {
        Page<Book> books = getTestBooksCollection();
        String keyword = "someKeyword";

        when(bookService.findBookByKeyword(keyword, 0, 5)).thenReturn(books);
        String url = "/api/books/search";

        mockMvc.perform(get(url)
                .param("keyword", keyword)
                .param("page", "0")
                .param("size", "5")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(books)));

        verify(bookService).findBookByKeyword(keyword, 0, 5);
    }

    @Test
    void shouldSearchBookByKeywordMethodReturnOkStatusWithEmptyPageWhenAnyBookContainsKeyword() throws Exception {
        String keyword = "someKeyword";

        when(bookService.findBookByKeyword(keyword, 0, 5)).thenReturn(Page.empty());
        String url = "/api/books/search";

        mockMvc.perform(get(url)
                .param("keyword", keyword)
                .param("page", "0")
                .param("size", "5")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(Page.empty())));

        verify(bookService).findBookByKeyword(keyword, 0, 5);
    }

    @Test
    void shouldSearchBookByKeywordMethodReturnOkStatusWithEmptyPageWhenKeywordIsNull() throws Exception {
        when(bookService.findBookByKeyword(null, 0, 24)).thenReturn(Page.empty());
        String url = "/api/books/search";

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(Page.empty())));

        verify(bookService).findBookByKeyword(null, 0, 24);
    }

    @Test
    void shouldSearchBookByKeywordMethodReturnInternalServerErrorWhenSomethingWentWrongOnServer() throws Exception {
        String keyword = "someKeyword";
        when(bookService.findBookByKeyword(keyword, 0, 24)).thenThrow(new BookStoreServiceException("An error occurred during attempt to find book by keyword."));
        String url = "/api/books/search";

        mockMvc.perform(get(url)
                .param("keyword", keyword)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(bookService).findBookByKeyword(keyword, 0, 24);
    }

    @Test
    void shouldFindBooksWithPromoMethodReturnPageOfBooksWhenParamsNotPassed() throws Exception {
        Page<Book> books = getTestBooksCollection();

        when(bookService.findBooksWithPromo(0,24)).thenReturn(books);

        String url = "/api/books/promos";

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(books)));

        verify(bookService).findBooksWithPromo(0,24);
    }

    @Test
    void shouldFindBooksWithPromoMethodReturnCorrectBooksPageWhenParamsPassed() throws Exception {
        Page<Book> books = getTestBooksCollection();

        when(bookService.findBooksWithPromo(0,5)).thenReturn(books);

        String url = "/api/books/promos";

        mockMvc.perform(get(url)
                .param("page", "0")
                .param("size", "5")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(books)));

        verify(bookService).findBooksWithPromo(0,5);
    }

    @Test
    void shouldFindBooksWithPromoMethodReturnInternalServerErrorWhenSomethingWentWrongOnServer() throws Exception {
        when(bookService.findBooksWithPromo(0,24)).thenThrow(new BookStoreServiceException("An error occurred during attempt to find books with promo."));

        String url = "/api/books/promos";

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(bookService).findBooksWithPromo(0, 24);
    }

    @Test
    void shouldFindActiveBooksMethodReturnPageOfBooksWhenParamsNotPassed() throws Exception {
        Page<Book> books = getTestBooksCollection();

        when(bookService.findActiveBooks(0,24)).thenReturn(books);

        String url = "/api/books/active";

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(books)));

        verify(bookService).findActiveBooks(0,24);
    }

    @Test
    void shouldFindActiveBooksMethodReturnCorrectBooksPageWhenParamsPassed() throws Exception {
        Page<Book> books = getTestBooksCollection();

        when(bookService.findActiveBooks(0,5)).thenReturn(books);

        String url = "/api/books/active";

        mockMvc.perform(get(url)
                .param("page", "0")
                .param("size", "5")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(books)));

        verify(bookService).findActiveBooks(0,5);
    }

    @Test
    void shouldFindActiveBooksMethodReturnInternalServerErrorWhenSomethingWentWrongOnServer() throws Exception {
        when(bookService.findActiveBooks(0,24)).thenThrow(new BookStoreServiceException("An error occurred during attempt to find active books."));

        String url = "/api/books/active";

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(bookService).findActiveBooks(0, 24);
    }

    @Test
    void shouldActivateBookMethodReturnOkStatusWithBook() throws Exception {
        String bookId = "someBookId";
        BookDto bookDto = BookGenerator.generateBookDtoWithActiveStatus(true);
        Book book = new Book(bookDto);
        when(bookService.existsByBookId(bookId)).thenReturn(true);
        when(bookService.updateBookActivationStatus(bookId, true)).thenReturn(book);

        String url = String.format("/api/books/activation/%s/activate",bookId);

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(book)));

        verify(bookService).existsByBookId(bookId);
        verify(bookService).updateBookActivationStatus(bookId, true);
    }

    @Test
    void shouldActivateBookMethodReturnNotFoundStatusWhenBookNotExists() throws Exception {
        String bookId = "someBookId";
        when(bookService.existsByBookId(bookId)).thenReturn(false);
        String message = String.format("Book with id: {} does not exists", bookId);

        String url = String.format("/api/books/activation/%s/activate",bookId);

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(message));

        verify(bookService).existsByBookId(bookId);
        verify(bookService, never()).updateBookActivationStatus(bookId, true);
    }

    @Test
    void shouldActivateBookMethodReturnInternalServerErrorWhenSomethingWentWrongOnServer() throws Exception {
        String bookId = "someBookId";
        when(bookService.existsByBookId(bookId)).thenReturn(true);
        when(bookService.updateBookActivationStatus(bookId, true)).thenThrow(new BookStoreServiceException("An error occurred during attempt to change book status."));

        String url = String.format("/api/books/activation/%s/activate",bookId);

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(bookService).existsByBookId(bookId);
        verify(bookService).updateBookActivationStatus(bookId, true);
    }

    @Test
    void shouldDeactivateBookMethodReturnOkStatusWithBook() throws Exception {
        String bookId = "someBookId";
        BookDto bookDto = BookGenerator.generateBookDtoWithActiveStatus(false);
        Book book = new Book(bookDto);
        when(bookService.existsByBookId(bookId)).thenReturn(true);
        when(bookService.updateBookActivationStatus(bookId, false)).thenReturn(book);

        String url = String.format("/api/books/activation/%s/deactivate",bookId);

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(book)));

        verify(bookService).existsByBookId(bookId);
        verify(bookService).updateBookActivationStatus(bookId, false);
    }

    @Test
    void shouldDeactivateBookMethodReturnNotFoundStatusWhenBookNotExists() throws Exception {
        String bookId = "someBookId";
        when(bookService.existsByBookId(bookId)).thenReturn(false);
        String message = String.format("Book with id: {} does not exists", bookId);

        String url = String.format("/api/books/activation/%s/deactivate",bookId);

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(message));

        verify(bookService).existsByBookId(bookId);
        verify(bookService, never()).updateBookActivationStatus(bookId, false);
    }

    @Test
    void shouldDeactivateBookMethodReturnInternalServerErrorWhenSomethingWentWrongOnServer() throws Exception {
        String bookId = "someBookId";
        when(bookService.existsByBookId(bookId)).thenReturn(true);
        when(bookService.updateBookActivationStatus(bookId, false)).thenThrow(new BookStoreServiceException("An error occurred during attempt to change book status."));

        String url = String.format("/api/books/activation/%s/deactivate",bookId);

        mockMvc.perform(get(url)

                .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(bookService).existsByBookId(bookId);
        verify(bookService).updateBookActivationStatus(bookId, false);
    }

    @Test
    void shouldUpdateBookMethodReturnCollectionsOfBookWithOkStatusWhenBookIsValid() throws Exception {
        String bookId = "someBookId";
        BookDto bookDto = BookGenerator.generateBookDto();
        Set<Book> bookSet = Collections.singleton(new Book(bookDto));
        when(bookService.existsByBookId(bookId)).thenReturn(true);
        when(bookService.saveBook(any(BookDto.class), anyString())).thenReturn(bookSet);

        String url = "/api/books/"+bookId;

        mockMvc.perform(put(url)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(bookDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(bookSet)));

        verify(bookService).existsByBookId(bookId);
        verify(bookService).saveBook(any(BookDto.class), anyString());
    }

    @Test
    void shouldUpdateBookMethodReturnBadRequestStatusWhenBookWithGivenBookIdNotExists() throws Exception {
        String bookId = "someBookId";
        BookDto bookDto = BookGenerator.generateBookDto();
        when(bookService.existsByBookId(bookId)).thenReturn(false);

        String message = String.format("Attempt to update non existing book with id: %s", bookId);

        String url = "/api/books/"+bookId;

        mockMvc.perform(put(url)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(bookDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(message));

        verify(bookService).existsByBookId(bookId);
        verify(bookService, never()).saveBook(bookDto, bookId);
    }

    @Test
    void shouldUpdateBookMethodReturnBadRequestStatusWhenPassedBookDtoIsNull() throws Exception {
        String bookId = "someBookId";
        when(bookService.existsByBookId(bookId)).thenReturn(true);

        String url = "/api/books/"+bookId;
        String message = "Book cannot be null";

        mockMvc.perform(put(url)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(null))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(message));

        verify(bookService).existsByBookId(bookId);
        verify(bookService, never()).saveBook(null, bookId);
    }

    @ParameterizedTest
    @MethodSource("setOfInvalidImageUrls")
    void shouldUpdateBookMethodReturnBadRequestWhenPassedBookImageUrlIsInvalid(String invalidUrl, BookDto bookDto) throws Exception {
        String bookId = "someBookId";
        when(bookService.existsByBookId(bookId)).thenReturn(true);

        String url = "/api/books/"+bookId;
        String message = String.format("Incorrect format of image url: %s", invalidUrl);;

        mockMvc.perform(put(url)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(bookDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(message));

        verify(bookService).existsByBookId(bookId);
        verify(bookService, never()).saveBook(bookDto, bookId);
    }

    @Test
    void shouldUpdateBookMethodReturnBadRequestWhenPromoPriceIsGreaterThanBasePrice() throws Exception {
        String bookId = "someBookId";
        when(bookService.existsByBookId(bookId)).thenReturn(true);

        String url = "/api/books/"+bookId;
        String message = "Promo price cannot be greater than base price";
        BookDto bookDto = BookGenerator.generateBookDtoWithBasePriceAndPromoPrice(BigDecimal.valueOf(23.50), BigDecimal.valueOf(24L));

        mockMvc.perform(put(url)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(bookDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(message));

        verify(bookService).existsByBookId(bookId);
        verify(bookService, never()).saveBook(bookDto, bookId);
    }

    @ParameterizedTest
    @MethodSource("setOfInvalidBookDtos")
    void shouldUpdateBookMethodReturnBadRequestWhenBookDtoIsInvalid(BookDto bookDto) throws Exception {
        String bookId = "someBookId";

        String url = "/api/books/"+bookId;

        mockMvc.perform(put(url)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(bookDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).saveBook(bookDto, bookId);
    }

    @Test
    void shouldUpdateBookMethodReturnInternalServerErrorWhenSomethingWentWrongOnServer() throws Exception {
        String bookId = "someBookId";
        BookDto bookDto = BookGenerator.generateBookDto();
        when(bookService.existsByBookId(bookId)).thenReturn(true);
        when(bookService.saveBook(any(BookDto.class), anyString())).thenThrow(new BookStoreServiceException("An error occurred during saving book to database."));

        String url = "/api/books/"+bookId;

        mockMvc.perform(put(url)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(bookDto)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(bookService).existsByBookId(bookId);
        verify(bookService).saveBook(any(BookDto.class), anyString());
    }

    @Test
    void shouldDeleteBookMethodRemoveBookWithStatusNoContent() throws Exception {
        String bookId = "someBookId";
        when(bookService.existsByBookId(bookId)).thenReturn(true);
        doNothing().when(bookService).deleteBookById(bookId);

        String url = "/api/books/"+bookId;

        mockMvc.perform(delete(url)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(bookService).existsByBookId(bookId);
        verify(bookService).deleteBookById(bookId);
    }

    @Test
    void shouldDeleteBookMethodReturnStatusNotFoundWhenBookNotExists() throws Exception {
        String bookId = "someBookId";
        when(bookService.existsByBookId(bookId)).thenReturn(false);

        String message = String.format("Attempt to remove non existing book with id: %s", bookId);

        String url = "/api/books/"+bookId;

        mockMvc.perform(delete(url)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(message));

        verify(bookService).existsByBookId(bookId);
        verify(bookService, never()).deleteBookById(bookId);
    }

    @Test
    void shouldDeleteBookMethodReturnInternalServerErrorWhenSomethingWentWrongOnServer() throws Exception {
        String bookId = "someBookId";
        when(bookService.existsByBookId(bookId)).thenReturn(true);
        doThrow(new BookStoreServiceException("An error occurred during attempt to delete book."))
                .when(bookService).deleteBookById(bookId);

        String url = "/api/books/"+bookId;

        mockMvc.perform(delete(url)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(bookService).existsByBookId(bookId);
        verify(bookService).deleteBookById(bookId);
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
