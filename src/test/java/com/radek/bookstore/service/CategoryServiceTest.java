package com.radek.bookstore.service;

import com.radek.bookstore.generators.BookGenerator;
import com.radek.bookstore.model.Book;
import com.radek.bookstore.model.Category;
import com.radek.bookstore.model.dto.CategoryDto;
import com.radek.bookstore.model.exception.BookStoreServiceException;
import com.radek.bookstore.model.response.CategoryWrapper;
import com.radek.bookstore.repository.CategoryRepository;
import com.radek.bookstore.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Stream;

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

    @Test
    void shouldFindByCategoryIdMethodReturnCategoryWithCorrectlySortedBooks() {
        String categoryId = "someCategoryId";
        Category category = new Category(new CategoryDto("Literatura Piękna"));
        category.setId(categoryId);

        category.setBooks(BookGenerator.generateExemplarySetOfBooksWithLastUpdateDate());

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        CategoryWrapper result = categoryService.findByCategoryId(categoryId, 0, 5);

        assertEquals(categoryId, result.getId());
        assertEquals("Literatura Piękna", result.getName());
        assertEquals(3, result.getBooks().getContent().size());
        assertEquals("Zbrodnia i kara", result.getBooks().getContent().get(0).getTitle());
        assertEquals("Idiota", result.getBooks().getContent().get(1).getTitle());
        assertEquals("Bracia Karamazow", result.getBooks().getContent().get(2).getTitle());

        verify(categoryRepository).findById(categoryId);
    }

    @ParameterizedTest
    @MethodSource("setOfCategoriesBooksWithBooksMissing")
    void shouldFindByCategoryIdMethodReturnCategoryWithEmptyListOfBooksWhenSetOfCategoryBooksIsNull(Category category) {
        String categoryId = category.getId();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        CategoryWrapper result = categoryService.findByCategoryId(categoryId, 0, 5);

        assertEquals(0, result.getBooks().getContent().size());
        assertEquals(0, result.getBooks().getTotalPages());

        verify(categoryRepository).findById(categoryId);
    }

    private static Stream<Arguments> setOfCategoriesBooksWithBooksMissing() {
        Category category1 = new Category(new CategoryDto("Literatura Piękna"));
        Category category2 = new Category(new CategoryDto("Fantasy"));

        category1.setId("catId1");
        category2.setId("catId2");

        category1.setBooks(null);
        category2.setBooks(new HashSet<>());

        return Stream.of(
                Arguments.of(category1),
                Arguments.of(category2)
        );
    }

    @Test
    void shouldFindByCategoryIdMethodThrowBookStoreServiceExceptionWhenNonTransientDataAccessExceptionOccurs() {
        String categoryId = "someCategoryId";
        doThrow(new NonTransientDataAccessException(""){}).when(categoryRepository).findById(categoryId);

        assertThrows(BookStoreServiceException.class, () -> categoryService.findByCategoryId(categoryId, 0 , 5));
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    void shouldExistByIdMethodReturnTrueWhenCategoryExists() {
        String categoryId = "existingCategoryId";
        when(categoryRepository.existsById(categoryId)).thenReturn(true);

        Boolean result = categoryService.existByCategoryId(categoryId);

        assertTrue(result);

        verify(categoryRepository).existsById(categoryId);
    }

    @Test
    void shouldExistByIdMethodReturnFalseWhenCategoryDoesNotExist() {
        String categoryId = "nonExistingCategoryId";
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        Boolean result = categoryService.existByCategoryId(categoryId);

        assertFalse(result);

        verify(categoryRepository).existsById(categoryId);
    }

    @Test
    void shouldExistByIdMethodThrowBookStoreServiceExceptionWhenNonTransientDataAccessExceptionOccur() {
        String categoryId = "someCategoryId";
        doThrow(new NonTransientDataAccessException(""){}).when(categoryRepository).existsById(categoryId);

        assertThrows(BookStoreServiceException.class, () -> categoryService.existByCategoryId(categoryId));
        verify(categoryRepository).existsById(categoryId);
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
