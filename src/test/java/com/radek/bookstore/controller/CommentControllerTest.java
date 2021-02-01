package com.radek.bookstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radek.bookstore.generators.BookGenerator;
import com.radek.bookstore.generators.CommentGenerator;
import com.radek.bookstore.model.Comment;
import com.radek.bookstore.model.dto.CommentDto;
import com.radek.bookstore.model.json.CommentJson;
import com.radek.bookstore.service.BookService;
import com.radek.bookstore.service.CommentService;
import com.radek.bookstore.service.UserService;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @MockBean
    CommentService commentService;

    @MockBean
    BookService bookService;

    @MockBean
    UserService userService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void shouldGetCommentsByBookIdMethodReturnCommentsPageWhenParamsNotPassed() throws Exception {
        String bookId="testBookId";
        Page<CommentJson> commentsJson = getPageOfTestCommentsJson();

        when(bookService.existsByBookId(bookId)).thenReturn(true);
        when(commentService.getCommentsByBookId(bookId, 0, 10)).thenReturn(commentsJson);

        String url = String.format("/api/comments/%s", bookId);

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(commentsJson)));

        verify(commentService).getCommentsByBookId(bookId, 0, 10);
    }

    @Test
    void shouldGetCommentsByBookIdMethodReturnCommentsPageWhenParamsPassed() throws Exception {
        String bookId="testBookId";
        Page<CommentJson> commentsJson = getPageOfTestCommentsJson();

        when(bookService.existsByBookId(bookId)).thenReturn(true);

        when(commentService.getCommentsByBookId(bookId, 0, 3)).thenReturn(commentsJson);

        String url = String.format("/api/comments/%s", bookId);

        mockMvc.perform(get(url)
                .param("pageNumber", "0")
                .param("pageSize", "3")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(commentsJson)));

        verify(commentService).getCommentsByBookId(bookId, 0, 3);
    }

    @Test
    void shouldGetCommentsByBookIdMethodReturnBadRequestWhenBookDoesNotExists() throws Exception {
        String bookId="nonExistingBookId";
        String message="Cannot find book with id: "+bookId;
        String url = String.format("/api/comments/%s", bookId);

        when(bookService.existsByBookId(bookId)).thenReturn(false);

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(message));

        verify(commentService, never()).getCommentsByBookId(bookId, 0, 10);
    }

    @Test
    void shouldSaveCommentMethodReturnAllBookCommentsWhenBookAndUserExists() throws Exception {
        String bookId="testBookId";
        String userId = "testUserId";
        String url = String.format("/api/comments/%s/user/%s", bookId, userId);
        CommentDto commentDto = new CommentDto("Some Content");
        Comment comment = new Comment(commentDto);

        when(bookService.existsByBookId(bookId)).thenReturn(true);
        when(userService.existByUserId(userId)).thenReturn(true);
        when(commentService.saveComment(any(CommentDto.class), anyString(), anyString())).thenReturn(Collections.singleton(comment));

        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(commentDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(Collections.singleton(comment))));

        verify(commentService).saveComment(any(CommentDto.class), anyString(), anyString());
    }

    @Test
    void shouldSaveCommentMethodReturnNotFoundWhenBookDoesNotExists() throws Exception {
        String bookId="testBookId";
        String userId = "testUserId";
        String url = String.format("/api/comments/%s/user/%s", bookId, userId);
        CommentDto commentDto = new CommentDto("Some Content");
        String message = String.format("Book with id: %s cannot be found", bookId);

        when(bookService.existsByBookId(bookId)).thenReturn(false);
        when(userService.existByUserId(userId)).thenReturn(true);

        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(commentDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(message));

        verify(commentService, never()).saveComment(commentDto, bookId, userId);
    }

    @Test
    void shouldSaveCommentMethodReturnNotFoundWhenUserDoesNotExists() throws Exception {
        String bookId="testBookId";
        String userId = "testUserId";
        String url = String.format("/api/comments/%s/user/%s", bookId, userId);
        CommentDto commentDto = new CommentDto("Some Content");
        String message = String.format("User with id: %s cannot be found", userId);

        when(bookService.existsByBookId(bookId)).thenReturn(true);
        when(userService.existByUserId(userId)).thenReturn(false);

        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(commentDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(message));

        verify(commentService, never()).saveComment(commentDto, bookId, userId);
    }

    @ParameterizedTest
    @MethodSource("setOfIncorrectCommentDtos")
    void shouldSaveCommentMethodReturnBadRequestWhenCommentDtoIsNotValid(CommentDto commentDto) throws Exception{
        String bookId="testBookId";
        String userId = "testUserId";
        String url = String.format("/api/comments/%s/user/%s", bookId, userId);
        when(bookService.existsByBookId(bookId)).thenReturn(true);
        when(userService.existByUserId(userId)).thenReturn(true);

        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(commentDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(commentService, never()).saveComment(commentDto, bookId, userId);
    }

    private static Stream<Arguments> setOfIncorrectCommentDtos() {
        return Stream.of(
                Arguments.of(new CommentDto(null)),
                Arguments.of(new CommentDto("ab")),
                Arguments.of(new CommentDto(BookGenerator.FIVE_PARAGRAPH_DESCRIPTION))
        );
    }

    private Page<CommentJson> getPageOfTestCommentsJson() {
        CommentJson comment1 = CommentGenerator.generateCommentJson("commentId1");
        CommentJson comment2 = CommentGenerator.generateCommentJson("commentId2");
        CommentJson comment3 = CommentGenerator.generateCommentJson("commentId3");

        return new PageImpl<>(Arrays.asList(comment3, comment2, comment1));
    }
}