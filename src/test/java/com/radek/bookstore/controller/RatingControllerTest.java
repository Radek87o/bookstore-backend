package com.radek.bookstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radek.bookstore.generators.RatingGenerator;
import com.radek.bookstore.model.Rating;
import com.radek.bookstore.model.dto.RatingDto;
import com.radek.bookstore.model.exception.BookStoreServiceException;
import com.radek.bookstore.service.BookService;
import com.radek.bookstore.service.RatingService;
import com.radek.bookstore.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(RatingController.class)
class RatingControllerTest {

    @MockBean
    private BookService bookService;

    @MockBean
    private UserService userService;

    @MockBean
    private RatingService ratingService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void shouldGetBooksRatingMethodReturnListOfRatingsWhenBookExists() throws Exception {
        List<Rating> bookRatings = getListOfTestBookRatings();
        String bookId = "testBookId";

        when(bookService.existsByBookId(bookId)).thenReturn(true);
        when(ratingService.getBookRatings(bookId)).thenReturn(bookRatings);

        String url = String.format("/api/ratings/%s", bookId);

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(bookRatings)));

        verify(bookService).existsByBookId(bookId);
        verify(ratingService).getBookRatings(bookId);
    }

    @Test
    void shouldGetBooksRatingMethodReturnNotFoundWhenBookDoesNotExist() throws Exception {
        String bookId = "nonExistingBookId";
        String message = String.format("Cannot retrieve rating due to given book with id: %s does not exist", bookId);

        when(bookService.existsByBookId(bookId)).thenReturn(false);

        String url = String.format("/api/ratings/%s", bookId);

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(message));

        verify(bookService).existsByBookId(bookId);
        verify(ratingService, never()).getBookRatings(bookId);
    }

    @Test
    void shouldGetBooksRatingMethodReturnInternalServerErrorWhenSomethingWentWrongOnServer() throws Exception {
        String bookId = "someBookId";

        when(bookService.existsByBookId(bookId)).thenReturn(true);
        when(ratingService.getBookRatings(bookId))
                .thenThrow(new BookStoreServiceException("An error occurred during retrieving book ratings"));

        String url = String.format("/api/ratings/%s", bookId);

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(bookService).existsByBookId(bookId);
        verify(ratingService).getBookRatings(bookId);
    }

    @Test
    void shouldGetSingleRatingMethodReturnCurrentUserRatingWhenUserAlreadyRatedGivenBook() throws Exception {
        String bookId = "testBookId";
        String userId = "testUserId";
        Rating rating = RatingGenerator.generateRating("testRating");

        when(bookService.existsByBookId(bookId)).thenReturn(true);
        when(userService.existByUserId(userId)).thenReturn(true);
        when(ratingService.getBookRating(bookId, userId)).thenReturn(Optional.of(rating));

        String url = String.format("/api/ratings/%s/user/%s", bookId, userId);

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(rating)));

        verify(bookService).existsByBookId(bookId);
        verify(userService).existByUserId(userId);
        verify(ratingService).getBookRating(bookId, userId);
    }

    @Test
    void shouldGetSingleRatingMethodReturnNewRatingWhenUserHasNotAlreadyRatedGivenBook() throws Exception {
        String bookId = "testBookId";
        String userId = "testUserId";
        Rating rating = new Rating(new RatingDto(0));

        when(bookService.existsByBookId(bookId)).thenReturn(true);
        when(userService.existByUserId(userId)).thenReturn(true);
        when(ratingService.getBookRating(bookId, userId)).thenReturn(Optional.empty());

        String url = String.format("/api/ratings/%s/user/%s", bookId, userId);

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(rating)));

        verify(bookService).existsByBookId(bookId);
        verify(userService).existByUserId(userId);
        verify(ratingService).getBookRating(bookId, userId);
    }

    @Test
    void shouldGetSingleRatingMethodReturnNotFoundWhenBookWithGivenBookIdDoesNotExist() throws Exception {
        String bookId = "testBookId";
        String userId = "testUserId";
        String message = String.format("Cannot retrieve rating due to given book with id: %s does not exist", bookId);
        when(bookService.existsByBookId(bookId)).thenReturn(false);

        String url = String.format("/api/ratings/%s/user/%s", bookId, userId);

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(message));

        verify(bookService).existsByBookId(bookId);
        verify(bookService).existsByBookId(bookId);
    }

    @Test
    void shouldGetSingleRatingMethodReturnNotFoundWhenUserWithGivenUserIdDoesNotExist() throws Exception {
        String bookId = "testBookId";
        String userId = "testUserId";
        String message = String.format("Cannot retrieve rating due to given user with id: %s does not exist", userId);
        when(bookService.existsByBookId(bookId)).thenReturn(true);
        when(userService.existByUserId(userId)).thenReturn(false);

        String url = String.format("/api/ratings/%s/user/%s", bookId, userId);

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(message));

        verify(bookService).existsByBookId(bookId);
        verify(userService).existByUserId(userId);
        verify(userService).existByUserId(userId);
    }

    @Test
    void shouldGetSingleRatingMethodReturnInternalServerErrorWhenSomethingWentWrongOnServer() throws Exception {
        String bookId = "testBookId";
        String userId = "testUserId";

        when(bookService.existsByBookId(bookId)).thenReturn(true);
        when(userService.existByUserId(userId)).thenReturn(true);
        when(ratingService.getBookRating(bookId, userId))
                .thenThrow(new BookStoreServiceException("An error occurred during retrieving single book rating"));

        String url = String.format("/api/ratings/%s/user/%s", bookId, userId);

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(bookService).existsByBookId(bookId);
        verify(userService).existByUserId(userId);
        verify(ratingService).getBookRating(bookId, userId);
    }

    @Test
    void shouldSaveRatingMethodReturnCreatedStatusWithCollectionOfBookRatingsWhenNewRatingWasSaved() throws Exception {
        List<Rating> bookRatings = getListOfTestBookRatings();
        String bookId = "testBookId";
        String userId = "testUserId";
        RatingDto ratingDto = new RatingDto(3);

        when(bookService.existsByBookId(bookId)).thenReturn(true);
        when(userService.existByUserId(userId)).thenReturn(true);
        when(ratingService.saveRating(any(RatingDto.class), anyString(), anyString())).thenReturn(Optional.of(bookRatings));

        String url = String.format("/api/ratings/%s/user/%s", bookId, userId);

        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(ratingDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(bookRatings)));

        verify(bookService).existsByBookId(bookId);
        verify(userService).existByUserId(userId);
        verify(ratingService).saveRating(any(RatingDto.class), anyString(), anyString());
    }

    @Test
    void shouldSaveRatingMethodReturnOkStatusWithEmptyCollectionOfBookRatingsWhenRatingDoesNotChanged() throws Exception {
        String bookId = "testBookId";
        String userId = "testUserId";
        RatingDto ratingDto = new RatingDto(3);
        String message = String.format("Passed rating of value: %d does not vary from current rating", ratingDto.getVote());

        when(bookService.existsByBookId(bookId)).thenReturn(true);
        when(userService.existByUserId(userId)).thenReturn(true);
        when(ratingService.saveRating(any(RatingDto.class), anyString(), anyString())).thenReturn(Optional.empty());

        String url = String.format("/api/ratings/%s/user/%s", bookId, userId);

        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(ratingDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(message));

        verify(bookService).existsByBookId(bookId);
        verify(userService).existByUserId(userId);
        verify(ratingService).saveRating(any(RatingDto.class), anyString(), anyString());
    }

    @Test
    void shouldSaveRatingMethodReturnNotFoundStatusWhenBookDoesNotExist() throws Exception {
        String bookId = "testBookId";
        String userId = "testUserId";
        RatingDto ratingDto = new RatingDto(3);
        String message = String.format("Book with id: %s cannot be found", bookId);

        when(bookService.existsByBookId(bookId)).thenReturn(false);

        String url = String.format("/api/ratings/%s/user/%s", bookId, userId);

        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(ratingDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(message));

        verify(bookService).existsByBookId(bookId);
        verify(ratingService, never()).saveRating(ratingDto, bookId, userId);
    }

    @Test
    void shouldSaveRatingMethodReturnNotFoundStatusWhenUserDoesNotExist() throws Exception {
        String bookId = "testBookId";
        String userId = "testUserId";
        RatingDto ratingDto = new RatingDto(3);
        String message = String.format("User with id: %s cannot be found", userId);

        when(bookService.existsByBookId(bookId)).thenReturn(true);
        when(userService.existByUserId(userId)).thenReturn(false);

        String url = String.format("/api/ratings/%s/user/%s", bookId, userId);

        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(ratingDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(message));

        verify(bookService).existsByBookId(bookId);
        verify(userService).existByUserId(userId);
        verify(ratingService, never()).saveRating(ratingDto, bookId, userId);
    }

    @ParameterizedTest
    @MethodSource("setOfInvalidRatingDtos")
    void shouldSaveRatingMethodReturnBadRequestStatusWhenPassedRatingDtoIsInvalid(RatingDto ratingDto) throws Exception {
        String bookId = "testBookId";
        String userId = "testUserId";

        String url = String.format("/api/ratings/%s/user/%s", bookId, userId);

        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(ratingDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(ratingService, never()).saveRating(ratingDto, bookId, userId);
    }

    private static Stream<Arguments> setOfInvalidRatingDtos() {
        return Stream.of(
                Arguments.of(new RatingDto(null)),
                Arguments.of(new RatingDto(-1)),
                Arguments.of(new RatingDto(6))
        );
    }

    @Test
    void shouldSaveRatingMethodReturnInternalServerErrorWhenSomethingWentWrongOnServer() throws Exception {
        String bookId = "testBookId";
        String userId = "testUserId";
        RatingDto ratingDto = new RatingDto(3);

        when(bookService.existsByBookId(bookId)).thenReturn(true);
        when(userService.existByUserId(userId)).thenReturn(true);
        when(ratingService.saveRating(any(RatingDto.class), anyString(), anyString()))
                .thenThrow(new BookStoreServiceException("An error occurred during saving book rating"));

        String url = String.format("/api/ratings/%s/user/%s", bookId, userId);

        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(ratingDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());;

        verify(bookService).existsByBookId(bookId);
        verify(userService).existByUserId(userId);
        verify(ratingService).saveRating(any(RatingDto.class), anyString(), anyString());
    }

    private List<Rating> getListOfTestBookRatings() {
        Rating rating1 = RatingGenerator.generateRating("ratingId1");
        Rating rating2 = RatingGenerator.generateRating("ratingId2");
        Rating rating3 = RatingGenerator.generateRating("ratingId3");

        return Arrays.asList(rating1, rating2, rating3);
    }
}
