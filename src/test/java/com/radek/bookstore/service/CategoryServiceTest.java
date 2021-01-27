package com.radek.bookstore.service;

import com.radek.bookstore.generators.BookGenerator;
import com.radek.bookstore.model.Book;
import com.radek.bookstore.model.Category;
import com.radek.bookstore.model.dto.CategoryDto;
import com.radek.bookstore.model.exception.BookStoreServiceException;
import com.radek.bookstore.repository.CategoryRepository;
import com.radek.bookstore.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    CategoryRepository categoryRepository;

    CategoryService categoryService;

    @BeforeEach
    void setup() {
        categoryService = new CategoryServiceImpl(categoryRepository);
    }

    @Test
    void shouldGetAllCategoriesMethodReturnAllCategoriesExistingInDb() {
        List<Category> categories = getTestListOfCategories();
        Sort sort = Sort.by(Sort.Direction.ASC, "name");

        when(categoryRepository.findAll(sort)).thenReturn(categories);

        List<Category> result = categoryService.getAllCategories();

        assertEquals(categories, result);

        verify(categoryRepository).findAll(sort);
    }

    @Test
    void shouldGetAllCategoriesMethodThrowBookStoreServiceExceptionWhenNonTransientDataAccessExceptionOccur() {
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        doThrow(new NonTransientDataAccessException(""){}).when(categoryRepository).findAll(sort);

        assertThrows(BookStoreServiceException.class, () -> categoryService.getAllCategories());
        verify(categoryRepository).findAll(sort);
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

}