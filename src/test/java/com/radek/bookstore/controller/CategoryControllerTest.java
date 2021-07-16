package com.radek.bookstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radek.bookstore.generators.BookGenerator;
import com.radek.bookstore.model.Book;
import com.radek.bookstore.model.Category;
import com.radek.bookstore.model.dto.CategoryDto;
import com.radek.bookstore.model.exception.BookStoreServiceException;
import com.radek.bookstore.model.response.CategoryWrapper;
import com.radek.bookstore.security.filter.JwtAccessDeniedHandler;
import com.radek.bookstore.security.filter.JwtAuthenticationEntryPoint;
import com.radek.bookstore.security.utility.JwtTokenProvider;
import com.radek.bookstore.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(value = CategoryController.class)
@WithMockUser(username = "user", roles = "USER")
class CategoryControllerTest {

    @MockBean
    CategoryService categoryService;

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

    @Test
    void shouldGetAllCategoriesMethodReturnOkStatusWithCollectionOfAllCategoriesExistingInDb() throws Exception {
        List<Category> categories = getTestListOfCategories();

        when(categoryService.getAllCategories()).thenReturn(categories);

        String url = "/api/categories";

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(categories)));

        verify(categoryService).getAllCategories();
    }

    @Test
    void shouldGetAllCategoriesMethodReturnInternalServerErrorWhenSomethingWentWrongOnServer() throws Exception {
        when(categoryService.getAllCategories()).thenThrow(new BookStoreServiceException("An error occurred during retrieving categories from db"));

        String url = "/api/categories";

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(categoryService).getAllCategories();
    }

    @Test
    void shouldGetCategoryByIdMethodReturnCategoryWrapperWithStatusOkWhenCategoryExists() throws Exception {
        CategoryWrapper categoryWrapper = getTestCategoryWrapper();

        when(categoryService.existByCategoryId(categoryWrapper.getId())).thenReturn(true);
        when(categoryService.findByCategoryId(categoryWrapper.getId(), 0, 24)).thenReturn(categoryWrapper);

        String url = String.format("/api/categories/%s", categoryWrapper.getId());

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(categoryWrapper)));

        verify(categoryService).existByCategoryId(categoryWrapper.getId());
        verify(categoryService).findByCategoryId(categoryWrapper.getId(), 0, 24);
    }

    @Test
    void shouldGetCategoryByIdMethodReturnCategoryWrapperWithStatusOkWhenCategoryExistsAndPageableParamsPassed() throws Exception {
        CategoryWrapper categoryWrapper = getTestCategoryWrapper();

        when(categoryService.existByCategoryId(categoryWrapper.getId())).thenReturn(true);
        when(categoryService.findByCategoryId(categoryWrapper.getId(), 0, 5)).thenReturn(categoryWrapper);

        String url = String.format("/api/categories/%s", categoryWrapper.getId());

        mockMvc.perform(get(url)
                .param("page", "0")
                .param("size", "5")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(categoryWrapper)));

        verify(categoryService).existByCategoryId(categoryWrapper.getId());
        verify(categoryService).findByCategoryId(categoryWrapper.getId(), 0, 5);
    }

    @Test
    void shouldGetCategoryByIdMethodReturnNotFoundStatusWhenCategoryDoesNotExists() throws Exception {
        String categoryId = "nonExistingCategoryId";
        String message = String.format("Cannot find category with id: {}", categoryId);

        when(categoryService.existByCategoryId(categoryId)).thenReturn(false);

        String url = String.format("/api/categories/%s", categoryId);

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(message));

        verify(categoryService).existByCategoryId(categoryId);
        verify(categoryService, never()).findByCategoryId(categoryId, 0, 24);
    }

    @Test
    void shouldGetCategoryByIdMethodReturnReturnInternalServerErrorWhenSomethingWentWrongOnServer() throws Exception {
        String categoryId = "someCategoryId";
        when(categoryService.existByCategoryId(categoryId)).thenReturn(true);
        when(categoryService.findByCategoryId(categoryId, 0, 24))
                .thenThrow(new BookStoreServiceException("An error occurred during retrieving category by id"));

        String url = String.format("/api/categories/%s", categoryId);

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(categoryService).existByCategoryId(categoryId);
        verify(categoryService).findByCategoryId(categoryId, 0, 24);
    }

    private List<Category> getTestListOfCategories() {
        Book book1 = BookGenerator.generateBook(LocalDateTime.of(LocalDate.of(2021, 1, 17), LocalTime.now()));
        Book book2 = BookGenerator.generateBook(LocalDateTime.of(LocalDate.of(2021, 1, 12), LocalTime.now()));
        Book book3 = BookGenerator.generateBook(LocalDateTime.of(LocalDate.of(2021, 1, 14), LocalTime.now()));

        Category fantasy = new Category(new CategoryDto("Fantasy"));
        Category history = new Category(new CategoryDto("Historia"));
        Category literature = new Category(new CategoryDto("Literatura Piękna"));

        List<Category> categories = List.of(fantasy, history, literature);
        categories.forEach(category -> category.setBooks(new HashSet<>(Arrays.asList(book1, book2, book3))));

        return categories;
    }

    private CategoryWrapper getTestCategoryWrapper() {
        Set<Book> bookSet = BookGenerator.generateExemplarySetOfBooksWithLastUpdateDate();
        Page<Book> books = new PageImpl<>(new ArrayList<>(bookSet));

        return CategoryWrapper.builder()
                .id("someCategoryId")
                .name("Literatura Piękna")
                .books(books)
                .build();
    }
}
