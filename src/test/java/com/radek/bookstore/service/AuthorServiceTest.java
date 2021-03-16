package com.radek.bookstore.service;

import com.radek.bookstore.generators.BookGenerator;
import com.radek.bookstore.model.Author;
import com.radek.bookstore.model.dto.AuthorDto;
import com.radek.bookstore.model.exception.BookStoreServiceException;
import com.radek.bookstore.model.json.AuthorWrapper;
import com.radek.bookstore.repository.AuthorRepository;
import com.radek.bookstore.service.impl.AuthorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.NonTransientDataAccessException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    @Mock
    AuthorRepository authorRepository;

    AuthorService authorService;

    @BeforeEach
    void setup() {
        authorService = new AuthorServiceImpl(authorRepository);
    }

    @Test
    void shouldFindByAuthorIdMethodReturnCorrectlySortedAuthorBooks() {
        String authorId = "someAuthorId";
        Author author = new Author(AuthorDto.builder()
                .firstName("Fiodor")
                .lastName("Dostojewski")
                .build());
        author.setId(authorId);

        author.setBooks(BookGenerator.generateExemplarySetOfBooksWithLastUpdateDate());

        when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));

        AuthorWrapper result = authorService.findByAuthorId(authorId, 0, 5);

        assertEquals(authorId, result.getId());
        assertEquals("Fiodor", result.getFirstName());
        assertEquals("Dostojewski", result.getLastName());
        assertEquals(author.getBooks().size(), result.getBooks().getTotalElements());
        assertEquals("Zbrodnia i kara", result.getBooks().getContent().get(0).getTitle());
        assertEquals("Idiota", result.getBooks().getContent().get(1).getTitle());
        assertEquals("Bracia Karamazow", result.getBooks().getContent().get(2).getTitle());

        verify(authorRepository).findById(authorId);
    }

    @ParameterizedTest
    @MethodSource("setOfAuthorBooksWithBooksMissing")
    void shouldFindByAuthorIdMethodReturnAuthorWithEmptyListOfBooksWhenSetOfCategoryBooksIsEmpty(Author author) {
        String authorId = author.getId();
        when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));

        AuthorWrapper result = authorService.findByAuthorId(authorId, 0, 5);

        assertEquals(0, result.getBooks().getContent().size());
        assertEquals(0, result.getBooks().getTotalPages());

        verify(authorRepository).findById(authorId);
    }

    private static Stream<Arguments> setOfAuthorBooksWithBooksMissing() {
        List<Author> authors = setOfTestAuthors();

        authors.get(0).setBooks(null);
        authors.get(1).setBooks(new HashSet<>());

        return Stream.of(
                Arguments.of(authors.get(0)),
                Arguments.of(authors.get(1))
        );
    }

    @Test
    void shouldFindByAuthorIdMethodThrowBookStoreServiceExceptionWhenNonTransientDataAccessExceptionOccurs() {
        String authorId = "someAuthorId";
        doThrow(new NonTransientDataAccessException(""){})
                .when(authorRepository).findById(authorId);

        assertThrows(BookStoreServiceException.class,
                ()->authorService.findByAuthorId(authorId, 0, 5));

        verify(authorRepository).findById(authorId);
    }

    @ParameterizedTest
    @MethodSource("setOfExemplaryAuthorIdsWithExistenceStatus")
    void shouldExistByAuthorIdMethodReturnCorrectBooleanValueDependingOnAuthorExistance(String authorId, boolean isExisting) {
        when(authorRepository.existsById(authorId)).thenReturn(isExisting);

        Boolean result = authorService.existByAuthorId(authorId);

        assertEquals(isExisting, result);

        verify(authorRepository).existsById(authorId);
    }

    private static Stream<Arguments> setOfExemplaryAuthorIdsWithExistenceStatus() {
        return Stream.of(
                Arguments.of("existingAuthorId", true),
                Arguments.of("nonExistingAuthorId", false)
        );
    }

    @Test
    void shouldExistByAuthorIdMethodThrowBookStoreServiceExceptionWhenNonTransientDataAccessExceptionOccurs(){
        String authorId = "someAuthorId";
        doThrow(new NonTransientDataAccessException(""){})
                .when(authorRepository).existsById(authorId);

        assertThrows(BookStoreServiceException.class,
                ()->authorService.existByAuthorId(authorId));

        verify(authorRepository).existsById(authorId);
    }

    private static List<Author> setOfTestAuthors() {
        Author author1 = new Author(AuthorDto.builder()
                .firstName("Stanisław")
                .lastName("Grzesiuk")
                .build());

        Author author2 = new Author(AuthorDto.builder()
                .firstName("James")
                .lastName("Clavell")
                .build());
        author1.setId("authorId1");
        author2.setId("authorId2");

        return Arrays.asList(author1, author2);
    }
}
