package com.radek.bookstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radek.bookstore.generators.BookGenerator;
import com.radek.bookstore.model.Book;
import com.radek.bookstore.model.exception.BookStoreServiceException;
import com.radek.bookstore.model.json.AuthorWrapper;
import com.radek.bookstore.service.AuthorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AuthorController.class)
class AuthorControllerTest {

    @MockBean
    AuthorService authorService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void shouldFindAuthorByIdMethodReturnAuthorBooksWithStatusOkWhenGivenAuthorExists() throws Exception {
        AuthorWrapper authorWrapper = generateAuthorWrapper();

        when(authorService.existByAuthorId(authorWrapper.getId())).thenReturn(true);
        when(authorService.findByAuthorId(authorWrapper.getId(), 0, 24)).thenReturn(authorWrapper);

        String url="/api/authors/"+authorWrapper.getId();

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(authorWrapper)));

        verify(authorService).findByAuthorId(authorWrapper.getId(), 0, 24);
    }

    @Test
    void shouldFindAuthorByIdMethodReturnAuthorBooksWithStatusOkWhenGivenAuthorExistsAndParamsPassed() throws Exception {
        AuthorWrapper authorWrapper = generateAuthorWrapper();

        when(authorService.existByAuthorId(authorWrapper.getId())).thenReturn(true);
        when(authorService.findByAuthorId(authorWrapper.getId(), 0, 5)).thenReturn(authorWrapper);

        String url="/api/authors/"+authorWrapper.getId();

        mockMvc.perform(get(url)
                .param("page","0")
                .param("size", "5")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(authorWrapper)));

        verify(authorService).findByAuthorId(authorWrapper.getId(), 0, 5);
    }

    @Test
    void shouldFindAuthorByIdMethodReturnAuthorBooksWithStatusNotFoundWhenGivenAuthorNotExists() throws Exception {
        AuthorWrapper authorWrapper = generateAuthorWrapper();
        when(authorService.existByAuthorId(authorWrapper.getId())).thenReturn(false);

        String url="/api/authors/"+authorWrapper.getId();
        String message = String.format("Cannot find author with id: {}", authorWrapper.getId());

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(message));

        verify(authorService).existByAuthorId(authorWrapper.getId());
        verify(authorService, never()).findByAuthorId(authorWrapper.getId(), 0, 24);
    }

    @Test
    void shouldFindAuthorByIdMethodReturnInternalServerErrorWhenSomethingWentWrongOnServer() throws Exception {
        String authorId = "someAuthorId";
        when(authorService.existByAuthorId(authorId)).thenReturn(true);
        when(authorService.findByAuthorId(authorId, 0, 24)).thenThrow(new BookStoreServiceException("An error occurred during retrieving author by id"));

        String url="/api/authors/"+authorId;

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(authorService).existByAuthorId(authorId);
        verify(authorService).findByAuthorId(authorId, 0, 24);
    }

    private AuthorWrapper generateAuthorWrapper() {
        Set<Book> books = BookGenerator.generateExemplarySetOfBooksWithLastUpdateDate();
        Page<Book> booksPage = new PageImpl<>(new ArrayList<>(books));

        return AuthorWrapper.builder()
                .id("existingAutorId")
                .firstName("Fiodor")
                .lastName("Dostojewski")
                .books(booksPage)
                .build();
    }
}
